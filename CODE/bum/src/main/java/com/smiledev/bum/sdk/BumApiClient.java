package com.smiledev.bum.sdk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Bum Platform License Validation SDK
 * For Java applications to validate licenses
 */
public class BumApiClient {
    private String apiKey;
    private String baseUrl;
    private int connectionTimeout = 5000;
    private int readTimeout = 5000;

    /**
     * Constructor
     * @param apiKey Your Bum API key from dashboard
     * @param baseUrl Base URL of Bum server (e.g., https://yoursite.com)
     */
    public BumApiClient(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.replaceAll("/$", ""); // Remove trailing slash
    }

    /**
     * Constructor with default baseUrl
     * @param apiKey Your Bum API key from dashboard
     */
    public BumApiClient(String apiKey) {
        this(apiKey, "https://bum.yoursite.com");
    }

    /**
     * Validate API key
     * @return true if API key is valid and active
     */
    public boolean validateApiKey() throws Exception {
        String url = baseUrl + "/api/v1/validate-key";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(connectionTimeout);
        conn.setReadTimeout(readTimeout);

        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        
        if (responseCode == 200) {
            return response.contains("\"valid\":true");
        }
        return false;
    }

    /**
     * Validate license key
     * @param licenseKey The license key to validate
     * @return LicenseValidationResponse with validation details
     */
    public LicenseValidationResponse validateLicense(String licenseKey) throws Exception {
        return validateLicense(licenseKey, null);
    }

    /**
     * Validate license key with hardware ID (for hardware-locked licenses)
     * @param licenseKey The license key to validate
     * @param hardwareId Optional hardware ID for hardware-locked licenses
     * @return LicenseValidationResponse with validation details
     */
    public LicenseValidationResponse validateLicense(String licenseKey, String hardwareId) throws Exception {
        String url = baseUrl + "/api/v1/licenses/validate";
        
        String payload = buildJsonPayload(licenseKey, hardwareId);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(connectionTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setDoOutput(true);

        // Send request body
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);

        return LicenseValidationResponse.fromJson(response);
    }

    /**
     * Build JSON payload without external dependencies
     */
    private String buildJsonPayload(String licenseKey, String hardwareId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"licenseKey\":\"").append(escapeJson(licenseKey)).append("\"");
        if (hardwareId != null) {
            sb.append(",\"hardwareId\":\"").append(escapeJson(hardwareId)).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Escape JSON strings
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Read HTTP response
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader br;
        if (conn.getResponseCode() >= 400) {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }
        
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        br.close();
        return response.toString();
    }

    /**
     * License Validation Response DTO
     */
    public static class LicenseValidationResponse {
        public boolean valid;
        public String status;
        public String message;
        public String productName;
        public String expireDate;
        public String hardwareId;
        public String userId;

        public static LicenseValidationResponse fromJson(String json) {
            LicenseValidationResponse response = new LicenseValidationResponse();
            response.valid = extractBoolean(json, "valid");
            response.status = extractString(json, "status");
            response.message = extractString(json, "message");
            response.productName = extractString(json, "productName");
            response.expireDate = extractString(json, "expireDate");
            response.hardwareId = extractString(json, "hardwareId");
            response.userId = extractString(json, "userId");
            return response;
        }

        private static String extractString(String json, String key) {
            String pattern = "\"" + key + "\":\"";
            int index = json.indexOf(pattern);
            if (index == -1) {
                // Check for null value
                pattern = "\"" + key + "\":null";
                return json.contains(pattern) ? null : "";
            }
            int start = index + pattern.length();
            int end = json.indexOf("\"", start);
            return end > start ? json.substring(start, end) : "";
        }

        private static boolean extractBoolean(String json, String key) {
            String pattern = "\"" + key + "\":true";
            return json.contains(pattern);
        }

        @Override
        public String toString() {
            return "LicenseValidationResponse{" +
                    "valid=" + valid +
                    ", status='" + status + '\'' +
                    ", message='" + message + '\'' +
                    ", productName='" + productName + '\'' +
                    ", expireDate='" + expireDate + '\'' +
                    ", hardwareId='" + hardwareId + '\'' +
                    ", userId='" + userId + '\'' +
                    '}';
        }
    }

    public void setConnectionTimeout(int timeoutMs) {
        this.connectionTimeout = timeoutMs;
    }

    public void setReadTimeout(int timeoutMs) {
        this.readTimeout = timeoutMs;
    }
}
