# Project Tracking: 
https://docs.google.com/spreadsheets/d/1vCYinYb8H_im1TxbPRbvjw7-F-7Mb8zHpUWzFmqdIAI/edit?usp=sharing

# SRS:
https://docs.google.com/document/d/1F1noNJ9ZER9M6Q_zmb9GFI7SyA7l80It4HvIFEskE4w/edit?usp=sharing



# Database

+ Sản phẩm
    1 User (developer) - n Products
    1 Product - n ProductVersions
    1 Product - n ProductPackages (gói/giá)
    1 Product - n Licenses
    1 Product - n Reviews

+ Category
    1 Category - n Products

+ Order
    1 User - n Orders
    1 Order - n Licenses
    1 User - n Licenses (sở hữu key)

+ Đánh giá
    1 User - n Reviews

+ Thanh toán
    1 User - n Transactions (lịch sử ví)

+ Logs hệ thống
    ActivityLogs: ghi hành động người dùng/hệ thống
    KeyValidationLogs: ghi lịch sử check key từ client (không FK để tối ưu ghi)





        USER
            password --> Nên hash = Bcrypt
            role --> admin, developer, user

        PRODUCT
            Có lịch sử update --> Thêm bảng nữa (ProductVersions)
            Giá của từng gói sản phẩm --> Thêm bảng lưu giá từng gói của từng sản phẩm (Product Packages)

            
