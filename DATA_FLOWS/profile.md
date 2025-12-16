# profile.html — Luồng dữ liệu

- **URL:** `/profile`
- **Controller:** UserController (CODE/bum/src/main/java/com/smiledev/bum/controller/UserController.java)
- **Bảo mật:** yêu cầu đăng nhập.

## GET /profile
1) Check auth; nếu null → redirect `/login`.
2) UserRepository.findByUsername(auth.getName()) → `user`.
3) Model add: `loggedInUser` = user.
4) Nếu model chưa có `updateProfileRequest` → fill với fullName/email hiện tại.
5) Nếu model chưa có `depositRequest` → new DepositRequest.
6) Render profile.html.

## POST /profile/update
1) Validate UpdateProfileRequest (fullName/email).
2) Nếu lỗi → flash BindingResult + request + `errorTab=profile`, redirect `/profile`.
3) UserService.updateProfile(username, request):
   - Tìm user; check email trùng user khác (UserRepository.findByEmail).
   - Cập nhật fullName, email; save.
4) ActivityLogService.logActivity(user, "UPDATE_PROFILE", "Users", userId, mô tả).
5) Flash successMessage, redirect `/profile`.

## POST /profile/deposit
1) Validate DepositRequest (amount > 0).
2) Nếu lỗi → flash BindingResult + `errorTab=deposit`, redirect `/profile`.
3) UserService.depositToWallet(username, amount):
   - Check amount > 0; load user; cộng walletBalance; save.
4) ActivityLogService.logActivity(user, "DEPOSIT", "Users", userId, mô tả).
5) Flash successMessage, redirect `/profile`.

## Model → View
- `loggedInUser` hiển thị thông tin ví, email, fullname.
- `updateProfileRequest`, `depositRequest` bind vào form; lỗi validation hiển thị.
