# product.html — Luồng dữ liệu

- **URL:** `/product/{id}`
- **Controller:** ProductController.viewProduct() (CODE/bum/src/main/java/com/smiledev/bum/controller/ProductController.java)
- **Bảo mật:** công khai; nếu đăng nhập nạp `loggedInUser`.

## Dòng chảy
1) Browser `GET /product/{id}`.
2) Controller nạp user (nếu đăng nhập) qua UserRepository.findByUsername → model `loggedInUser`.
3) Side effect: ProductService.incrementViewCount(id) → ProductsRepository.findById → set viewCount+1 → save.
4) Tải sản phẩm chi tiết: ProductService.findProductById(id) → ProductsRepository.findByIdWithDetails (FETCH join developer, category, packages, versions, reviews+user).
5) Nếu không tồn tại → redirect `/`.
6) Model: `product` (đầy đủ versions, packages, reviews), `loggedInUser`.
7) Render product.html: mô tả, video, lịch sử phiên bản, reviews, sidebar mua (packages, giá) với JS checkout.

## Ghi dữ liệu
- Cập nhật `viewCount` của Products.
