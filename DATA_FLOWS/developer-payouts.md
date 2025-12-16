# developer-payouts.html — Luồng dữ liệu

- **URL:** `/dashboard/developer/payouts` (+ `/history` alias)
- **Controller:** DeveloperPayoutController (CODE/bum/src/main/java/com/smiledev/bum/controller/DeveloperPayoutController.java)
- **Bảo mật:** ROLE_DEVELOPER.

## GET /dashboard/developer/payouts
1) Auth dev → load developer (UserRepository.findByUsername) & `loggedInUser`.
2) Lấy `walletBalance` (default 0).
3) Pageable page param (size 5, sort createdAt desc).
4) TransactionsRepository.findByUserAndTypeOrderByCreatedAtDesc(developer, withdrawal, pageable) → `withdrawals`.
5) Model add: `walletBalance`, `withdrawals` → render developer-payouts.html.

## POST /dashboard/developer/payouts (request payout)
1) Auth dev → load developer, balance.
2) Validate amount > 0 và balance đủ; nếu lỗi flash `error`, redirect back.
3) Trừ balance: developer.walletBalance -= amount; save user.
4) Tạo Transactions: type=withdrawal, amount=amount, description=note or default; save.
5) ActivityLogService.logActivity(developer, "withdrawal_request", "Transactions", txId, note).
6) Flash `success`, redirect `/dashboard/developer/payouts`.

## Ghi dữ liệu
- Cập nhật Users.walletBalance.
- Thêm Transactions (withdrawal).
- Thêm ActivityLogs (logActivityService).
