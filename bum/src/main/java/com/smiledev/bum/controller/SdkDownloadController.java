package com.smiledev.bum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/dashboard/sdk")
public class SdkDownloadController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Show SDK documentation and download page
     */
    @GetMapping
    @PreAuthorize("hasRole('DEVELOPER')")
    public String sdkDocumentation(Model model, Authentication authentication) {
        Users developer = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (developer == null) {
            return "redirect:/login";
        }

        model.addAttribute("developer", developer);
        model.addAttribute("baseUrl", "https://yoursite.com"); // Change to your actual domain
        model.addAttribute("apiDocsUrl", "/dashboard/sdk");
        
        return "sdk-documentation";
    }

    /**
     * Download Java SDK
     */
    @GetMapping("/download/java")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<String> downloadJavaSdk() throws IOException {
        String sdkCode = generateJavaSdkCode();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"BumApiClient.java\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(sdkCode);
    }

    /**
     * Download JavaScript SDK
     */
    @GetMapping("/download/javascript")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<String> downloadJavaScriptSdk() throws IOException {
        String sdkCode = generateJavaScriptSdkCode();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bum-sdk.js\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(sdkCode);
    }

    /**
     * Download Python SDK
     */
    @GetMapping("/download/python")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<String> downloadPythonSdk() throws IOException {
        String sdkCode = generatePythonSdkCode();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bum_sdk.py\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(sdkCode);
    }

    /**
     * Generate Java SDK code
     */
    private String generateJavaSdkCode() {
        return "package com.example;\n\n" +
                "import java.io.*;\n" +
                "import java.net.HttpURLConnection;\n" +
                "import java.net.URL;\n" +
                "import java.nio.charset.StandardCharsets;\n" +
                "import org.json.JSONObject;\n\n" +
                "public class BumApiClient {\n" +
                "    private String apiKey;\n" +
                "    private String baseUrl;\n" +
                "    private int connectionTimeout = 5000;\n" +
                "    private int readTimeout = 5000;\n\n" +
                "    public BumApiClient(String apiKey, String baseUrl) {\n" +
                "        this.apiKey = apiKey;\n" +
                "        this.baseUrl = baseUrl.replaceAll(\"/$\", \"\");\n" +
                "    }\n\n" +
                "    public BumApiClient(String apiKey) {\n" +
                "        this(apiKey, \"https://yoursite.com\");\n" +
                "    }\n\n" +
                "    public boolean validateApiKey() throws Exception {\n" +
                "        String url = baseUrl + \"/api/v1/validate-key\";\n" +
                "        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();\n" +
                "        conn.setRequestMethod(\"POST\");\n" +
                "        conn.setRequestProperty(\"Authorization\", \"Bearer \" + apiKey);\n" +
                "        conn.setRequestProperty(\"Content-Type\", \"application/json\");\n" +
                "        conn.setConnectTimeout(connectionTimeout);\n" +
                "        conn.setReadTimeout(readTimeout);\n\n" +
                "        int responseCode = conn.getResponseCode();\n" +
                "        String response = readResponse(conn);\n" +
                "        \n" +
                "        if (responseCode == 200) {\n" +
                "            JSONObject json = new JSONObject(response);\n" +
                "            return json.getBoolean(\"valid\");\n" +
                "        }\n" +
                "        return false;\n" +
                "    }\n\n" +
                "    public LicenseResponse validateLicense(String licenseKey) throws Exception {\n" +
                "        return validateLicense(licenseKey, null);\n" +
                "    }\n\n" +
                "    public LicenseResponse validateLicense(String licenseKey, String hardwareId) throws Exception {\n" +
                "        String url = baseUrl + \"/api/v1/licenses/validate\";\n" +
                "        JSONObject payload = new JSONObject();\n" +
                "        payload.put(\"licenseKey\", licenseKey);\n" +
                "        if (hardwareId != null) {\n" +
                "            payload.put(\"hardwareId\", hardwareId);\n" +
                "        }\n\n" +
                "        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();\n" +
                "        conn.setRequestMethod(\"POST\");\n" +
                "        conn.setRequestProperty(\"Authorization\", \"Bearer \" + apiKey);\n" +
                "        conn.setRequestProperty(\"Content-Type\", \"application/json\");\n" +
                "        conn.setConnectTimeout(connectionTimeout);\n" +
                "        conn.setReadTimeout(readTimeout);\n" +
                "        conn.setDoOutput(true);\n\n" +
                "        try (OutputStream os = conn.getOutputStream()) {\n" +
                "            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);\n" +
                "            os.write(input, 0, input.length);\n" +
                "        }\n\n" +
                "        String response = readResponse(conn);\n" +
                "        return LicenseResponse.fromJson(response);\n" +
                "    }\n\n" +
                "    private String readResponse(HttpURLConnection conn) throws IOException {\n" +
                "        BufferedReader br = new BufferedReader(new InputStreamReader(\n" +
                "            conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream()));\n" +
                "        StringBuilder response = new StringBuilder();\n" +
                "        String line;\n" +
                "        while ((line = br.readLine()) != null) {\n" +
                "            response.append(line);\n" +
                "        }\n" +
                "        br.close();\n" +
                "        return response.toString();\n" +
                "    }\n\n" +
                "    public static class LicenseResponse {\n" +
                "        public boolean valid;\n" +
                "        public String status;\n" +
                "        public String message;\n" +
                "        public String productName;\n" +
                "        public String expireDate;\n" +
                "        public String hardwareId;\n" +
                "        public String userId;\n\n" +
                "        public static LicenseResponse fromJson(String json) {\n" +
                "            JSONObject obj = new JSONObject(json);\n" +
                "            LicenseResponse resp = new LicenseResponse();\n" +
                "            resp.valid = obj.optBoolean(\"valid\");\n" +
                "            resp.status = obj.optString(\"status\");\n" +
                "            resp.message = obj.optString(\"message\");\n" +
                "            resp.productName = obj.optString(\"productName\");\n" +
                "            resp.expireDate = obj.optString(\"expireDate\");\n" +
                "            resp.hardwareId = obj.optString(\"hardwareId\");\n" +
                "            resp.userId = obj.optString(\"userId\");\n" +
                "            return resp;\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
    }

    /**
     * Generate JavaScript SDK code
     */
    private String generateJavaScriptSdkCode() {
        return "/**\n" +
                " * Bum Platform License Validation SDK for JavaScript\n" +
                " * Usage: const client = new BumApiClient('your-api-key', 'https://yoursite.com');\n" +
                " */\n\n" +
                "class BumApiClient {\n" +
                "    constructor(apiKey, baseUrl = 'https://yoursite.com') {\n" +
                "        this.apiKey = apiKey;\n" +
                "        this.baseUrl = baseUrl.replace(/\\/$/, '');\n" +
                "        this.timeout = 5000;\n" +
                "    }\n\n" +
                "    async validateApiKey() {\n" +
                "        try {\n" +
                "            const response = await fetch(`${this.baseUrl}/api/v1/validate-key`, {\n" +
                "                method: 'POST',\n" +
                "                headers: {\n" +
                "                    'Authorization': `Bearer ${this.apiKey}`,\n" +
                "                    'Content-Type': 'application/json'\n" +
                "                }\n" +
                "            });\n" +
                "            const data = await response.json();\n" +
                "            return data.valid === true;\n" +
                "        } catch (error) {\n" +
                "            console.error('API Key validation error:', error);\n" +
                "            return false;\n" +
                "        }\n" +
                "    }\n\n" +
                "    async validateLicense(licenseKey, hardwareId = null) {\n" +
                "        try {\n" +
                "            const payload = {\n" +
                "                licenseKey: licenseKey\n" +
                "            };\n" +
                "            if (hardwareId) {\n" +
                "                payload.hardwareId = hardwareId;\n" +
                "            }\n\n" +
                "            const response = await fetch(`${this.baseUrl}/api/v1/licenses/validate`, {\n" +
                "                method: 'POST',\n" +
                "                headers: {\n" +
                "                    'Authorization': `Bearer ${this.apiKey}`,\n" +
                "                    'Content-Type': 'application/json'\n" +
                "                },\n" +
                "                body: JSON.stringify(payload)\n" +
                "            });\n" +
                "            return await response.json();\n" +
                "        } catch (error) {\n" +
                "            console.error('License validation error:', error);\n" +
                "            return {\n" +
                "                valid: false,\n" +
                "                status: 'error',\n" +
                "                message: error.message\n" +
                "            };\n" +
                "        }\n" +
                "    }\n" +
                "}\n\n" +
                "// Export for Node.js\n" +
                "if (typeof module !== 'undefined' && module.exports) {\n" +
                "    module.exports = BumApiClient;\n" +
                "}\n";
    }

    /**
     * Generate Python SDK code
     */
    private String generatePythonSdkCode() {
        return "#!/usr/bin/env python3\n\"\"\"\nBum Platform License Validation SDK for Python\nUsage: client = BumApiClient('your-api-key', 'https://yoursite.com')\n\"\"\"\n\n" +
                "import requests\n" +
                "import json\n\n" +
                "class BumApiClient:\n" +
                "    def __init__(self, api_key, base_url='https://yoursite.com'):\n" +
                "        self.api_key = api_key\n" +
                "        self.base_url = base_url.rstrip('/')\n" +
                "        self.timeout = 5\n" +
                "        self.headers = {\n" +
                "            'Authorization': f'Bearer {api_key}',\n" +
                "            'Content-Type': 'application/json'\n" +
                "        }\n\n" +
                "    def validate_api_key(self):\n" +
                "        \"\"\"Validate if API key is active\"\"\"\n" +
                "        try:\n" +
                "            url = f'{self.base_url}/api/v1/validate-key'\n" +
                "            response = requests.post(url, headers=self.headers, timeout=self.timeout)\n" +
                "            if response.status_code == 200:\n" +
                "                data = response.json()\n" +
                "                return data.get('valid', False)\n" +
                "            return False\n" +
                "        except Exception as e:\n" +
                "            print(f'Error validating API key: {e}')\n" +
                "            return False\n\n" +
                "    def validate_license(self, license_key, hardware_id=None):\n" +
                "        \"\"\"Validate license key\"\"\"\n" +
                "        try:\n" +
                "            url = f'{self.base_url}/api/v1/licenses/validate'\n" +
                "            payload = {'licenseKey': license_key}\n" +
                "            if hardware_id:\n" +
                "                payload['hardwareId'] = hardware_id\n\n" +
                "            response = requests.post(url, headers=self.headers, json=payload, timeout=self.timeout)\n" +
                "            return response.json()\n" +
                "        except Exception as e:\n" +
                "            print(f'Error validating license: {e}')\n" +
                "            return {\n" +
                "                'valid': False,\n" +
                "                'status': 'error',\n" +
                "                'message': str(e)\n" +
                "            }\n\n" +
                "# Example usage:\n" +
                "# if __name__ == '__main__':\n" +
                "#     client = BumApiClient('your-api-key')\n" +
                "#     result = client.validate_license('LICENSE-KEY')\n" +
                "#     if result['valid']:\n" +
                "#         print('License is valid!')\n" +
                "#     else:\n" +
                "#         print(f'License invalid: {result[\"message\"]}')\n";
    }
}
