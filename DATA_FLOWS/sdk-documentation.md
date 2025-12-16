# sdk-documentation.html — Luồng dữ liệu

- **URL:** `/dashboard/sdk`
- **Controller:** SdkDownloadController (CODE/bum/src/main/java/com/smiledev/bum/controller/SdkDownloadController.java)
- **Bảo mật:** ROLE_DEVELOPER.

## GET /dashboard/sdk
1) Auth developer → UserRepository.findByUsername → `developer`.
2) Model add: `developer`, `baseUrl` (placeholder https://yoursite.com), `apiDocsUrl` (/dashboard/sdk).
3) Render sdk-documentation.html.

## GET /dashboard/sdk/download/java | /javascript | /python
- Quyền: ROLE_DEVELOPER.
- Mỗi endpoint tạo mã SDK tại chỗ (generateJavaSdkCode / generateJavaScriptSdkCode / generatePythonSdkCode).
- Trả ResponseEntity với Content-Disposition attachment và nội dung text/plain.

## Ghi dữ liệu
- Không ghi DB; chỉ đọc Users để xác thực và trả mã nguồn SDK động.
