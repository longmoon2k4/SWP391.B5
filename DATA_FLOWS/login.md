# login.html — Luồng dữ liệu (Đăng nhập/Đăng ký)

- **URL:** `/login`, `/register` (GET/POST)
- **Controller:** AuthController (CODE/bum/src/main/java/com/smiledev/bum/controller/AuthController.java)
- **Bảo mật:** Ẩn trang nếu đã đăng nhập (redirect `/`).

## GET /login
1) Nếu Authenticated → redirect `/`.
2) Nếu model chưa có `registrationRequest` → tạo mới RegistrationRequest DTO.
3) Render login.html với form login & register (tabs), binding `registrationRequest`.

## POST /register
1) Nhận RegistrationRequest (username/fullName/email/password/confirmPassword/role) + Bean Validation + PasswordMatches.
2) Nếu lỗi validation → trả lại login.html với errors.
3) UserService.registerNewUser():
   - Check trùng username/email (UserRepository.existsByUsername/Email).
   - Encode password (BCrypt), map role (mặc định user nếu role không hợp lệ), set walletBalance=0, active=true.
   - Save Users.
4) ActivityLogService.logActivity(newUser, "REGISTER", "Users", userId, "New user registered: <username>").
5) Flash `successMessage`, redirect `/login`.

## Đăng nhập (Spring Security formLogin)
- form `action="/login"` → Spring Security authenticates via CustomUserDetailsService (UserRepository.findByUsername, load role to ROLE_*). Password checked bằng BCryptPasswordEncoder.
- Thành công: redirect `/` (config SecurityConfig.defaultSuccessUrl("/", true)).

## Model → View
- `registrationRequest` cho form đăng ký; lỗi binding hiển thị trong template.
