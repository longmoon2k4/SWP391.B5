# order-success.html — Luồng dữ liệu

- **URL:** `/orders/success?orderId=` (redirect từ callback)
- **Controller:** OrderController.redirectSuccess() → redirect `/orders/details?orderId=...`
- **Render:** order-success.html hiển thị kết quả thanh toán nếu truy cập trực tiếp, nhưng luồng chuẩn là xem ở order-details.

## Ghi chú
- Dữ liệu thực tế lấy trong order-details flow (order + licenses). File này chủ yếu để hiển thị thông báo thành công khi được truy cập riêng.
