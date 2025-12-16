# products.html — Luồng dữ liệu

- **URL:** `/product`
- **Controller:** ProductController.listProducts() (CODE/bum/src/main/java/com/smiledev/bum/controller/ProductController.java)
- **Bảo mật:** công khai; nếu đăng nhập nạp `loggedInUser`.

## Dòng chảy
1) Browser `GET /product` với `category?`, `search?`, `page`, `size`.
2) Controller nạp user (nếu đăng nhập) qua UserRepository.findByUsername → model `loggedInUser`.
3) Tạo `PageRequest(page,size)`.
4) Gọi ProductService.getApprovedProducts(categoryId, search, pageable).
   - Repo: ProductsRepository.findApprovedByCategoryIdAndName → Page<Products>.
   - Repo: ReviewsRepository.findAverageRatingByProductId cho mỗi product.
   - Map sang ProductCardDTO (id, name, shortDescription, demoVideoUrl, viewCount, averageRating, packages).
5) Repo: CategoriesRepository.findAll → `categories` + `selectedCategoryId` + `search` add model.
6) Render products.html (lưới sản phẩm, bộ lọc, phân trang).

## Ghi dữ liệu?
- Không ghi.
