# user-dashboard.html — Luồng dữ liệu

- **URL:** `/dashboard/user`
- **Controller:** DashboardController (CODE/bum/src/main/java/com/smiledev/bum/controller/DashboardController.java)
- **Bảo mật:** ROLE_USER.

## GET /dashboard/user
1) Auth user → load Users via UserRepository.findByUsername → `loggedInUser`.
2) Wallet: read Users.walletBalance → `walletBalance`.
3) Orders thống kê: countByUser, countByUserAndStatus (completed|pending|failed), sumTotalAmountByUserAndStatus (completed) → `ordersTotal`, `ordersCompleted`, `ordersPending`, `ordersFailed`, `totalSpent`.
4) Đơn gần nhất: OrdersRepository.findTop5ByUserOrderByCreatedAtDesc → `recentOrders`.
5) Licenses thống kê: countByUser, countByUserAndStatus (active|expired|unused) → `licensesTotal`, `licensesActive`, `licensesExpired`, `licensesUnused`.
6) Licenses gần nhất: LicensesRepository.findTop5ByUserOrderByCreatedAtDesc → `recentLicenses`.
7) Render user-dashboard.html.

## Ghi dữ liệu
- Không ghi DB (chỉ đọc thống kê và danh sách).
