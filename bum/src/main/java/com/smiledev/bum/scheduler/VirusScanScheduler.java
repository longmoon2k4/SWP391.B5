package com.smiledev.bum.scheduler;

import com.smiledev.bum.entity.ProductVersions;
import com.smiledev.bum.entity.ProductVersions.VirusScanStatus;
import com.smiledev.bum.repository.ProductVersionsRepository;
import com.smiledev.bum.service.VirusScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VirusScanScheduler {

    @Autowired
    private ProductVersionsRepository productVersionsRepository;

    @Autowired
    private VirusScanService virusScanService;

    /**
     * Check scan results every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes = 300,000 ms
    public void checkPendingScans() {
        try {
            // Find all versions with pending status that have analysisId (meaning scan is in progress)
            List<ProductVersions> pendingVersions = productVersionsRepository.findByVirusScanStatus(VirusScanStatus.pending);
            
            // Filter versions that have analysis ID (scan in progress)
            List<ProductVersions> scanningVersions = pendingVersions.stream()
                    .filter(v -> v.getVirusScanAnalysisId() != null && !v.getVirusScanAnalysisId().isEmpty())
                    .toList();
            
            if (!scanningVersions.isEmpty()) {
                System.out.println("Checking " + scanningVersions.size() + " pending virus scans...");
                
                for (ProductVersions version : scanningVersions) {
                    virusScanService.checkScanResult(version);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in virus scan scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
