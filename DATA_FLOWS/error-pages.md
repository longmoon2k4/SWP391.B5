# error.html / error/404.html / error/500.html — Luồng dữ liệu

- **URL:** `/error` (Spring Boot error path)
- **Controller:** CustomErrorController (CODE/bum/src/main/java/com/smiledev/bum/controller/CustomErrorController.java)
- **Bảo mật:** Public.

## Xử lý /error
1) Khi xảy ra exception hoặc status code 4xx/5xx, Spring forward tới `/error`.
2) CustomErrorController.handleError đọc RequestDispatcher.ERROR_STATUS_CODE.
3) Nếu status = 404 → trả view `error/404` (template error/404.html).
4) Nếu status = 500 → trả view `error/500` (template error/500.html).
5) Các trường hợp khác → trả view `error` (template error.html tổng quát).

## Model & dữ liệu
- Không thêm model; các template tĩnh hiển thị thông báo lỗi chung.

## Ghi dữ liệu
- Không ghi DB; chỉ phân nhánh view theo status code.
