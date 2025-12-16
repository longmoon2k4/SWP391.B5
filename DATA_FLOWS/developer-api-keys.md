# developer-api-keys.html — Luồng dữ liệu

- **URL:** `/dashboard/api-keys`
- **Controller:** ApiKeysManagementController (CODE/bum/src/main/java/com/smiledev/bum/controller/ApiKeysManagementController.java)
- **Bảo mật:** ROLE_DEVELOPER.

## GET /dashboard/api-keys
1) Auth dev → load developer via UserRepository.findByUsername.
2) Nạp `loggedInUser` vào model (dùng header).
3) ApiKeysRepository.findByDeveloper(developer) → list ApiKeys.
4) Map mỗi key → ApiKeyResponseDTO.fromEntity(key, showSecret=false) (masked secret).
5) Model add: `apiKeys`, `developerName`, `totalKeys`, `activeKeys`.
6) Render developer-api-keys.html.

## POST /dashboard/api-keys/generate
1) Auth dev.
2) Tạo ApiKeyRequestDTO(keyName, rateLimit).
3) ApiKeyService.generateApiKey(developer, request):
   - Sinh `sk_live_<uuid>`, hash keySecret (PasswordEncoder), set rateLimit (default 1000), status active, createdAt now.
   - Save ApiKeys.
   - Return ApiKeyResponseDTO(showSecret=true) chứa apiKey chỉ lần này.
4) Flash `successMessage`, `newApiKey`, `newKeyName`; redirect `/dashboard/api-keys`.

## POST /dashboard/api-keys/{keyId}/revoke
- Verify ownership; apiKeyService.revokeApiKey(keyId) → set status=revoked; redirect with flash.

## POST /dashboard/api-keys/{keyId}/delete
- Verify ownership; apiKeyService.deleteApiKey(keyId); redirect with flash.

## GET /dashboard/api-keys/{keyId}/usage
- Verify ownership; dùng ApiUsageLogsRepository (paginate) để lấy log (controller not shown full here). Render usage tab in same template.

## Ghi dữ liệu
- Tạo key, revoke, delete: ghi bảng ApiKeys và ApiUsageLogs (khi validate ở API khác). No ActivityLog in this controller.
