package com.smiledev.bum.sdk;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Java SDK for SmileDev API
 * Usage:
 *   SmileDevClient client = new SmileDevClient("https://yourapi.com", "sk_live_xxxxx");
 *   KeyValidationResult result = client.validateKey();
 */
public class SmileDevClient {
    private String baseUrl;
    private String apiKey;
    private static final int TIMEOUT_MS = 5000;

    public SmileDevClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.replaceAll("/$", ""); // Remove trailing slash
        this.apiKey = apiKey;
    }

    /**
     * Validate API key with remote server
     */
    public KeyValidationResult validateKey() throws Exception {
        String endpoint = this.baseUrl + "/api/v1/validate-key";
        
        HttpURLConnection conn = (HttpURLConnection) new java.net.URI(endpoint).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + this.apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        int statusCode = conn.getResponseCode();
        String responseBody = readInputStream(
            statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
        );

        return parseResponse(responseBody, statusCode);
    }

    /**
     * Parse JSON response
     */
    private KeyValidationResult parseResponse(String json, int statusCode) throws Exception {
        // Simple JSON parsing (or use a library like Gson)
        boolean valid = json.contains("\"valid\":true");
        String status = extractValue(json, "status");
        String message = extractValue(json, "message");
        String productName = extractValue(json, "productName");
        String developerName = extractValue(json, "developerName");

        return new KeyValidationResult(
            valid,
            status,
            message,
            productName,
            developerName,
            statusCode == 200
        );
    }

    /**
     * Simple JSON value extraction
     */
    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : null;
    }

    /**
     * Read input stream to string
     */
    private String readInputStream(InputStream stream) throws Exception {
        Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    /**
     * Result class
     */
    public static class KeyValidationResult {
        public boolean valid;
        public String status;
        public String message;
        public String productName;
        public String developerName;
        public boolean isSuccess;

        public KeyValidationResult(boolean valid, String status, String message, 
                                 String productName, String developerName, boolean isSuccess) {
            this.valid = valid;
            this.status = status;
            this.message = message;
            this.productName = productName;
            this.developerName = developerName;
            this.isSuccess = isSuccess;
        }

        @Override
        public String toString() {
            return "KeyValidationResult{" +
                    "valid=" + valid +
                    ", status='" + status + '\'' +
                    ", message='" + message + '\'' +
                    ", developerName='" + developerName + '\'' +
                    ", isSuccess=" + isSuccess +
                    '}';
        }
    }
}
