-- =============================================
-- PHẦN I: CÁC BẢNG NGHIỆP VỤ CHÍNH (CORE)
-- =============================================

-- 2. Bảng Users: Quản lý cả Admin, Dev và User mua hàng
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- Lưu mật khẩu đã mã hóa (Bcrypt/Argon2)
    full_name NVARCHAR(100),
    role ENUM('admin', 'developer', 'user') DEFAULT 'user',
    wallet_balance DECIMAL(15, 2) DEFAULT 0.00, -- Ví tiền
    is_active BOOLEAN DEFAULT TRUE, -- Dùng để Ban user
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. Bảng Categories: Danh mục phần mềm
CREATE TABLE Categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255)
);

-- 4. Bảng Products: Sản phẩm (Chứa thông tin chung)
CREATE TABLE Products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    developer_id INT NOT NULL,
    category_id INT,
    name NVARCHAR(150) NOT NULL,
    description TEXT, -- Hỗ trợ HTML/Markdown
    short_description NVARCHAR(255),
    demo_video_url VARCHAR(255),
    
    -- Trạng thái duyệt của Admin
    status ENUM('pending', 'approved', 'rejected', 'hidden') DEFAULT 'pending',
    rejection_reason TEXT NULL,
    
    total_sales INT DEFAULT 0, -- Tăng lên khi có người mua
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (developer_id) REFERENCES Users(user_id),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

-- 5. Bảng ProductVersions: Quản lý File và Source Code (Versioning)
CREATE TABLE ProductVersions (
    version_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    version_number VARCHAR(20) NOT NULL, -- VD: 1.0.0, 1.0.1
    
    -- Đường dẫn file (Lưu trên Server/S3)
    source_code_path VARCHAR(255) NOT NULL, -- Dành cho Admin check
    build_file_path VARCHAR(255) NOT NULL,  -- Dành cho User tải
    
    -- Kết quả quét Virus tự động
    virus_scan_status ENUM('pending', 'clean', 'infected') DEFAULT 'pending',
    virus_total_report_link VARCHAR(255),
    
    is_current_version BOOLEAN DEFAULT FALSE, -- Phiên bản hiện hành để bán
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- 6. Bảng ProductPackages: Cấu hình giá (Duration Base)
CREATE TABLE ProductPackages (
    package_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    name NVARCHAR(50) NOT NULL, -- VD: "Gói 1 tháng", "Lifetime"
    duration_days INT NULL, -- NULL = Vĩnh viễn. Số nguyên = Số ngày.
    price DECIMAL(10, 2) NOT NULL,
    
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- 7. Bảng Orders: Đơn hàng
CREATE TABLE Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL, -- Người mua
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    payment_method VARCHAR(50), -- VD: 'MOMO', 'VNPAY', 'PAYPAL'
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- 8. Bảng Licenses: (CORE) Quản lý Key bản quyền
CREATE TABLE Licenses (
    license_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    user_id INT NOT NULL, -- Chủ sở hữu Key
    package_id INT NOT NULL, -- Gói đã mua
    
    license_key VARCHAR(100) NOT NULL UNIQUE, -- Key chuỗi (GUID/UUID)
    
    start_date DATETIME NULL, -- Ngày kích hoạt đầu tiên
    expire_date DATETIME NULL, -- Ngày hết hạn (NULL = Vĩnh viễn)
    
    -- Cơ chế chống dùng chung
    hardware_id VARCHAR(255) NULL, -- Mã máy (HWID)
    
    status ENUM('active', 'expired', 'banned', 'unused') DEFAULT 'unused',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (package_id) REFERENCES ProductPackages(package_id)
);

-- 9. Bảng Transactions: Lịch sử dòng tiền
CREATE TABLE Transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL, -- (+) Cộng tiền, (-) Trừ tiền
    type ENUM('deposit', 'purchase', 'sale_revenue', 'withdrawal', 'refund') NOT NULL,
    description NVARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- 10. Bảng Reviews: Đánh giá
CREATE TABLE Reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    rating TINYINT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- =============================================
-- PHẦN II: CÁC BẢNG LOGGING (SYSTEM AUDIT)
-- =============================================

-- 11. Bảng ActivityLogs: Lưu hành động của Admin/Dev
CREATE TABLE ActivityLogs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT, -- Có thể NULL nếu là System Job
    action_type VARCHAR(50) NOT NULL, -- 'LOGIN', 'APPROVE_PRODUCT', 'CHANGE_CONFIG'
    target_table VARCHAR(50), -- Bảng bị tác động
    target_id INT, -- ID bản ghi bị tác động
    description TEXT, -- Chi tiết thay đổi (JSON hoặc Text)
    ip_address VARCHAR(45),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- 12. Bảng KeyValidationLogs: Lưu lịch sử check key từ Client App
CREATE TABLE KeyValidationLogs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    license_key VARCHAR(100) NOT NULL,
    hardware_id VARCHAR(255), -- Mã máy gửi lên
    ip_address VARCHAR(45), -- IP client
    status ENUM('success', 'failed') NOT NULL, -- Kết quả trả về cho App
    fail_reason VARCHAR(255), -- 'Expired', 'Wrong HWID', 'Locked'
    request_time DATETIME DEFAULT CURRENT_TIMESTAMP
    
    -- Không dùng Foreign Key để tối ưu tốc độ Write Log
);

-- =============================================
-- PHẦN III: DỮ LIỆU MẪU (SEED DATA) - CHẠY TEST
-- =============================================

-- Thêm User (Pass: 123456 - demo hash)
INSERT INTO Users (username, email, password_hash, role, wallet_balance) VALUES 
('admin_main', 'admin@devstore.com', 'hash_string_here', 'admin', 0),
('dev_tuan', 'tuan@dev.com', 'hash_string_here', 'developer', 500.00),
('user_hieu', 'hieu@gmail.com', 'hash_string_here', 'user', 100.00);

-- Thêm Category
INSERT INTO Categories (name, description) VALUES 
('System Tools', 'Phần mềm hệ thống, tối ưu máy tính'),
('Game Assets', 'Tài nguyên làm game');

-- Thêm Product
INSERT INTO Products (developer_id, category_id, name, description, status) VALUES 
(2, 1, 'Super Cleaner Pro', 'Phần mềm dọn rác máy tính siêu tốc', 'approved');

-- Thêm Product Version
INSERT INTO ProductVersions (product_id, version_number, source_code_path, build_file_path, virus_scan_status, is_current_version) VALUES 
(1, '1.0.0', '/uploads/src/v1.zip', '/uploads/build/cleaner_setup.exe', 'clean', TRUE);

-- Thêm Gói giá
INSERT INTO ProductPackages (product_id, name, duration_days, price) VALUES 
(1, 'Monthly Pass', 30, 5.00),
(1, 'Lifetime License', NULL, 50.00);

-- Thêm Đơn hàng mẫu
INSERT INTO Orders (user_id, total_amount, status, payment_method) VALUES 
(3, 50.00, 'completed', 'VNPAY');

-- Thêm License mẫu
INSERT INTO Licenses (order_id, product_id, user_id, package_id, license_key, status) VALUES 
(1, 1, 3, 2, 'XXXX-YYYY-ZZZZ-AAAA', 'unused');