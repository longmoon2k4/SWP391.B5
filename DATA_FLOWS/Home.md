# Home.html — Luồng dữ liệu

- **URL:** `/`
- **Controller:** HomeController.home() (file: CODE/bum/src/main/java/com/smiledev/bum/controller/HomeController.java)
- **Bảo mật:** công khai; nếu đăng nhập sẽ nạp `loggedInUser`.

## Dòng chảy
1) Browser `GET /` với optional `page`, `size`, `category`, `search`.
2) Controller lấy user (nếu có) qua UserRepository.findByUsername → model `loggedInUser`.
3) Tạo `PageRequest(page,size)`.
4) Gọi ProductService.getApprovedProducts(categoryId, search, pageable).
   - Repo: ProductsRepository.findApprovedByCategoryIdAndName (JOIN category nếu lọc) → Page<Products>.
   - Repo: ReviewsRepository.findAverageRatingByProductId cho mỗi product.
   - Map sang ProductCardDTO, include packages (ProductPackages) và rating trung bình.
5) Repo: CategoriesRepository.findAll để dựng bộ lọc danh mục.
6) Đưa vào model: `productPage`, `categories`, `selectedCategoryId`, `search`, `loggedInUser`.
7) Render Home.html bằng Thymeleaf.

## Ghi dữ liệu?
- Không. Chỉ đọc DB và tăng view count không xảy ra ở đây.

## Model → View
- `productPage.content[*].name/shortDescription/viewCount/averageRating/packages` dùng để render thẻ sản phẩm.
- `categories` cho dropdown lọc; `search` và `selectedCategoryId` binding vào form.
