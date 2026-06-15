package com.smiledev.bum.service;

import com.smiledev.bum.entity.ProductVersions;
import com.smiledev.bum.entity.ProductVersions.VirusScanStatus;
import com.smiledev.bum.repository.ProductVersionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.Map;
import java.util.Optional;

@Service
public class VirusScanService {

    @Value("${virustotal.api.key:}")
    private String apiKey;

    @Autowired
    private ProductVersionsRepository productVersionsRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String VIRUSTOTAL_UPLOAD_URL = "https://www.virustotal.com/api/v3/files";
    private static final String VIRUSTOTAL_ANALYSIS_URL = "https://www.virustotal.com/api/v3/analyses/";

    /**
     * Upload file to VirusTotal and initiate scan (async)
     */
    @Async
    public void scanFileAsync(ProductVersions version, String filePath) {
        try {
            // Check if API key is configured
            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("VirusTotal API key not configured. Skipping scan for version: " + version.getVersionId());
                updateScanStatus(version.getVersionId(), VirusScanStatus.pending, "API key not configured");
                return;
            }

            System.out.println("VirusTotal API key loaded (len=" + apiKey.trim().length() + ")");

            // Upload file to VirusTotal
            String analysisId = uploadFileForScan(filePath);
            
            if (analysisId != null) {
                // Update version with analysis ID
                version.setVirusScanAnalysisId(analysisId);
                version.setVirusScanStatus(VirusScanStatus.pending);
                version.setVirusScanDetails("Scan in progress...");
                productVersionsRepository.save(version);
                
                System.out.println("File uploaded to VirusTotal. Analysis ID: " + analysisId);
            } else {
                // Failed to upload
                version.setVirusScanStatus(VirusScanStatus.pending);
                version.setVirusScanDetails("Failed to upload to VirusTotal");
                productVersionsRepository.save(version);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                System.err.println("VirusTotal unauthorized (401). Check API key.");
                version.setVirusScanStatus(VirusScanStatus.pending);
                version.setVirusScanDetails("VirusTotal Unauthorized (401). Check API key configuration.");
                productVersionsRepository.save(version);
                return;
            }
            System.err.println("HTTP error during virus scan: " + e.getStatusCode() + ": " + e.getMessage());
            e.printStackTrace();
            updateScanStatus(version.getVersionId(), VirusScanStatus.pending, "HTTP error: " + e.getStatusCode());
        } catch (Exception e) {
            System.err.println("Error during virus scan: " + e.getMessage());
            e.printStackTrace();
            updateScanStatus(version.getVersionId(), VirusScanStatus.pending, "Error: " + e.getMessage());
        }
    }

    /**
     * Upload file to VirusTotal and get analysis ID
     */
    private String uploadFileForScan(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-apikey", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(VIRUSTOTAL_UPLOAD_URL, requestEntity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    return (String) data.get("id");
                }
            }
        } catch (Exception e) {
            System.err.println("Error uploading file to VirusTotal: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check scan result from VirusTotal using analysis ID
     */
    public void checkScanResult(ProductVersions version) {
        try {
            if (version.getVirusScanAnalysisId() == null) {
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-apikey", apiKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = VIRUSTOTAL_ANALYSIS_URL + version.getVirusScanAnalysisId();
            System.out.println("Checking VirusTotal analysis for version " + version.getVersionId() + ", analysisId=" + version.getVirusScanAnalysisId());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
                    String status = (String) attributes.get("status");
                    System.out.println("VirusTotal analysis status for version " + version.getVersionId() + ": " + status);
                    
                    if ("completed".equals(status)) {
                        Map<String, Object> stats = (Map<String, Object>) attributes.get("stats");
                        Integer malicious = (Integer) stats.get("malicious");
                        Integer suspicious = (Integer) stats.get("suspicious");
                        
                        if (malicious != null && suspicious != null) {
                            if (malicious > 0 || suspicious > 0) {
                                updateScanStatus(version.getVersionId(), VirusScanStatus.infected, 
                                    "Detected: " + malicious + " malicious, " + suspicious + " suspicious");
                                System.out.println("Version " + version.getVersionId() + " marked INFECTED");
                            } else {
                                updateScanStatus(version.getVersionId(), VirusScanStatus.clean, "No threats detected");
                                System.out.println("Version " + version.getVersionId() + " marked CLEAN");
                            }
                        }
                    }
                    // If status is still "queued" or "in-progress", keep it as scanning
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking scan result: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update virus scan status in database
     */
    public void updateScanStatus(int versionId, VirusScanStatus status, String details) {
        Optional<ProductVersions> versionOpt = productVersionsRepository.findById(versionId);
        if (versionOpt.isPresent()) {
            ProductVersions version = versionOpt.get();
            version.setVirusScanStatus(status);
            version.setVirusScanDetails(details);
            productVersionsRepository.save(version);
            System.out.println("Updated scan status for version " + versionId + " to " + status);
        }
    }

    /**
     * Manually trigger rescan for a version
     */
    @Async
    public void rescanVersion(int versionId) {
        Optional<ProductVersions> versionOpt = productVersionsRepository.findById(versionId);
        if (versionOpt.isPresent()) {
            ProductVersions version = versionOpt.get();
            String filePath = version.getBuildFilePath();
            
            // Reset status and analysis ID
            version.setVirusScanStatus(VirusScanStatus.pending);
            version.setVirusScanAnalysisId(null);
            version.setVirusScanDetails(null);
            productVersionsRepository.save(version);
            
            // Trigger new scan
            scanFileAsync(version, filePath);
        }
    }
}
