# admin-dashboard.html — Luồng dữ liệu

- **URL:** `/dashboard/admin` và các action con
- **Controller:** DashboardController (CODE/bum/src/main/java/com/smiledev/bum/controller/DashboardController.java)
- **Bảo mật:** ROLE_ADMIN.

## Tabs & Luồng chính
### 1) Users tab `/dashboard/admin/users`
1) Auth (admin) → Pageable page param `userPage`.
2) UserRepository.findAll(pageable sorted createdAt desc) → model `users`, `userPage`, `userTotalPages`, `activeTab="users"`.
3) Render admin-dashboard.html (tab users).

**Ban user** `/dashboard/admin/users/{userId}/ban` (POST)
- Load admin (UserRepository.findByUsername) & target user by id.
- Set target.isActive = false; save.
- ActivityLogService.logActivity(admin, "ban", "Users", userId, ...).
- Flash message → redirect `/dashboard/admin?tab=users`.

**Unban user**: tương tự, set isActive=true.

**Edit user** `/dashboard/admin/users/{userId}/edit`
- Update email, fullName; save; log "edit".

### 2) Products tab `/dashboard/admin/products`
1) Params: `productPage`, optional `status`.
2) Pageable size 10, sort createdAt desc.
3) Nếu status null → productsRepository.findAll(pageable) else findByStatus(status,pageable) + count.
4) Model: `products`, `productPage`, `productTotalPages`, `filterStatus`, `activeTab="products"`.
5) Render admin-dashboard.html tab products.

**Update product status** `/dashboard/admin/products/{productId}/status` (POST)
- Load admin & product; set product.status = status param; save.
- Log: activityLogService.logActivity(admin, "update_status", "Products", productId, "Set status to ...").

### 3) Other data (activity/logs/virus scans)
- DashboardController còn dùng ActivityLogRepository, KeyValidationLogsRepository, ProductVersionsRepository để hiển thị log/scans (trong các phần khác của file template nếu có). Dữ liệu đọc-only.

## Model chung
- `loggedInUser` được set trong header bởi Security (qua layout) khi admin đăng nhập.

## Ghi dữ liệu
- Ghi khi ban/unban/edit user, cập nhật trạng thái sản phẩm; ghi ActivityLogs tương ứng.
