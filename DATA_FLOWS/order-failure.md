# order-failure.html — Luồng dữ liệu

- **URL:** `/orders/failure` (optionally với `orderId`)
- **Controller:** OrderController.orderFailure()
- **Flow:**
  - Nếu có `orderId` → redirect `/orders/details?orderId=...` để xem trạng thái cụ thể.
  - Nếu không có → model `message` = "Thanh toán không thành công hoặc đã bị hủy." rồi render order-failure.html.
- **Ghi dữ liệu:** Không ghi; chỉ hiển thị thông báo.
