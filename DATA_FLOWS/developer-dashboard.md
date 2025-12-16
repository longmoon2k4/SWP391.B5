# developer-dashboard.html — Luồng dữ liệu

- **URL:** `/dashboard/developer` và các tab thống kê dev
- **Controller:** DashboardController (CODE/bum/src/main/java/com/smiledev/bum/controller/DashboardController.java)
- **Bảo mật:** ROLE_DEVELOPER.

## Dòng chảy chung
1) Auth developer → load Users (UserRepository.findByUsername) → model `loggedInUser`.
2) DashboardController lấy thống kê bằng các repo:
   - ProductsRepository (sản phẩm của dev, đếm theo status, top cập nhật gần đây).
   - OrdersRepository (đếm đơn liên quan sản phẩm dev?).
   - LicensesRepository (license đã bán).
   - TransactionsRepository (doanh thu ví developer, withdrawal...)
   - ProductVersionsRepository (status scan).
3) Model đưa các số liệu, danh sách bảng (đơn gần đây, license, phiên bản, doanh thu) → render developer-dashboard.html.

## Hành động ghi (trong DashboardController)
- Một số endpoint (không hiển thị đầy đủ ở template) có thể cập nhật status sản phẩm, trigger rescan, v.v.; khi ghi sẽ log qua ActivityLogService.

## Lưu ý
- Template hiển thị nhiều card thống kê, bảng orders/licenses/transactions. Dữ liệu đến từ các repository ở trên, đọc-only trên trang.
