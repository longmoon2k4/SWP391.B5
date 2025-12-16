# user-licenses.html — Luồng dữ liệu

- **URL:** `/dashboard/user/licenses`
- **Controller:** DashboardController (CODE/bum/src/main/java/com/smiledev/bum/controller/DashboardController.java)
- **Bảo mật:** ROLE_USER.

## GET /dashboard/user/licenses
1) Auth user → load Users via UserRepository.findByUsername → `loggedInUser`.
2) Đọc query: `search` (optional), `page` (default 0), `size` (default 15).
3) Nếu `search` có giá trị: LicensesRepository.findByUserAndProduct_NameContainingIgnoreCase(user, search, pageable) → `licensesPage`.
4) Nếu không search: LicensesRepository.findByUser(user, pageable) → `licensesPage`.
5) Model add: `licensesPage`, `search`, `page`, `size`.
6) Render user-licenses.html.

## Ghi dữ liệu
- Không ghi DB (chỉ đọc/paginate licenses thuộc user).
