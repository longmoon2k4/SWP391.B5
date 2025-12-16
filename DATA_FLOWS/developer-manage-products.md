# developer-manage-products.html — Luồng dữ liệu

- **URL chính:** hiển thị trong Developer Dashboard (tab quản lý sản phẩm), dùng form gửi tới `/product/create`.
- **Controller xử lý submit:** ProductController.createProduct() (CODE/bum/src/main/java/com/smiledev/bum/controller/ProductController.java)
- **Bảo mật:** ROLE_DEVELOPER.

## GET view
- Trang được render từ developer-dashboard/developer-manage-products.html với dữ liệu sản phẩm/dev (được DashboardController cung cấp). Nội dung hiển thị danh sách sản phẩm dev (ProductsRepository.findByDeveloper...) và form tạo mới (category list từ CategoriesRepository).

## POST /product/create (từ form trên trang)
1) Auth dev → UserRepository.findByUsername.
2) Validate categoryId tồn tại (CategoriesRepository.findById).
3) Validate file upload: phải có exeFile.
4) Tạo thư mục upload `app.upload.dir` nếu chưa có.
5) Lưu file: đặt tên `UUID+ext`, copy vào uploads/products.
6) Tạo Products:
   - Set name/category/shortDescription/description/demoVideoUrl/developer/status=pending/totalSales=0/viewCount=0.
   - Save (ProductsRepository.save).
7) Tạo ProductVersions:
   - versionNumber, buildFilePath(filePath), sourceCodePath="", virusScanStatus=pending, product=product.
   - Save (ProductVersionsRepository.save).
8) Tạo ProductPackages (mảng packageNames/prices/durations): loop tạo, set product, save via ProductPackagesRepository.saveAll (implicit per entity save in loop).
9) Virus scan: VirusScanService.scanFileAsync(version, filePath)
   - Nếu có API key VirusTotal: upload, set analysisId, status pending, details "Scan in progress..."; save ProductVersions.
   - Nếu không: giữ pending + details lỗi.
10) Flash success/error qua redirect `/dashboard/developer` (hoặc trang tương ứng). (Trong code hiện tại redirect `/dashboard/developer` khi lỗi / thành công).

## Ghi dữ liệu
- Tables: Products, ProductVersions, ProductPackages.
- Virus scan updates ProductVersions.
- (ActivityLog: chưa gọi trong createProduct, có thể bổ sung nếu cần.)
