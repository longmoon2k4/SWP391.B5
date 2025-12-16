# developer-activity.html — Luồng dữ liệu

- **URL:** `/dashboard/developer/activity`
- **Controller:** DeveloperActivityController (CODE/bum/src/main/java/com/smiledev/bum/controller/DeveloperActivityController.java)
- **Bảo mật:** ROLE_DEVELOPER.

## Dòng chảy
1) Auth dev → lấy developer qua UserRepository.findByUsername.
2) Nạp `loggedInUser` vào model.
3) Pageable page param (default 0, size 10, sort createdAt desc).
4) ActivityLogRepository.findByUserOrderByCreatedAtDesc(developer, pageable) → Page<ActivityLogs> `activities`.
5) Model add: `activities` → render developer-activity.html (bảng log hành động của dev).

## Ghi dữ liệu
- Trang chỉ đọc. Việc ghi ActivityLogs diễn ra ở các service khác (đăng ký, mua, cập nhật...).
