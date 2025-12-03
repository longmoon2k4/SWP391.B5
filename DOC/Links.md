# Project Tracking: 
https://docs.google.com/spreadsheets/d/1QFF6tvAzN2z2lss4YIWw7OEChV3pf2xj0r2UiIOoE9Q/edit?usp=sharing

# SRS:
https://docs.google.com/document/d/1F1noNJ9ZER9M6Q_zmb9GFI7SyA7l80It4HvIFEskE4w/edit?usp=sharing



> Database
1 User (developer) - n Products
1 Product - n ProductVersions
1 Product - n ProductPackages (gói/giá)
1 Product - n Licenses
1 Product - n Reviews


1 Category - n Products


1 User - n Orders
1 Order - n Licenses
1 User - n Licenses (sở hữu key)


1 User - n Reviews


1 User - n Transactions (lịch sử ví)


 ActivityLogs: ghi hành động người dùng/hệ thống
 KeyValidationLogs: ghi lịch sử check key từ client (không FK để tối ưu ghi)



        USER
            password --> Nên hash = Bcrypt
            role --> admin, developer, user

        PRODUCT
            Có lịch sử update --> Thêm bảng nữa (ProductVersions)
            Giá của từng gói sản phẩm --> Thêm bảng lưu giá từng gói của từng sản phẩm (Product Packages)

            
