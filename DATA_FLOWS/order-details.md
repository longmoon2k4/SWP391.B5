# order-details.html — Luồng dữ liệu

- **URL:** `/orders/details?orderId=`
- **Controller:** OrderController.showOrderDetails() (CODE/bum/src/main/java/com/smiledev/bum/controller/OrderController.java)
- **Bảo mật:** yêu cầu đăng nhập và phải là chủ đơn.

## Dòng chảy
1) Nếu chưa đăng nhập → redirect `/login`.
2) Nạp user (UserRepository.findByUsername) → model `loggedInUser`.
3) OrdersRepository.findById(orderId); nếu không có → model `message` rồi view error.
4) Kiểm tra chủ sở hữu: order.user.username == auth.getName(), nếu không → redirect `/error`.
5) LicensesRepository.findByOrder_OrderId(orderId) → licenses.
6) Model add: `order`, `licenses`, `statusMessage` (tùy status: completed/failed/pending/other).
7) Render order-details.html.

## Ghi dữ liệu
- Không ghi; chỉ đọc.
