# orders.html — Luồng dữ liệu

- **URL:** `/orders`
- **Controller:** OrderController.getUserOrders() (CODE/bum/src/main/java/com/smiledev/bum/controller/OrderController.java)
- **Bảo mật:** yêu cầu đăng nhập.

## Dòng chảy
1) Auth check; nếu null/unauthenticated → redirect `/login`.
2) UserRepository.findByUsername(auth.getName()) → user; add model `loggedInUser`.
3) OrdersRepository.findByUser(user) → danh sách tất cả đơn.
4) Bộ lọc trong memory:
   - search: chuỗi chứa orderId
   - status: so khớp order.status
   - startDate/endDate: so sánh createdAt.toLocalDate()
5) Model add: `orders` (đã lọc), `search`, `status`, `startDate`, `endDate`, `allStatuses` (Orders.Status enum list).
6) Render orders.html.

## Ghi dữ liệu
- Không ghi; chỉ đọc.
