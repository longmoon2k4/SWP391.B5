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
-- ==========================================================
-- BƯỚC 1: CẤU HÌNH & SỬA LỖI FONT TIẾNG VIỆT CHO TOÀN BỘ BẢNG
-- ==========================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Sửa lỗi #1366 cho TẤT CẢ các bảng có thể chứa tiếng Việt
ALTER TABLE Users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE Products CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE Categories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE ProductPackages CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE Transactions CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE Reviews CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Bổ sung 2 bảng này (lần trước bị thiếu gây lỗi):
ALTER TABLE KeyValidationLogs CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE ActivityLogs CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==========================================================
-- BƯỚC 2: RESET DỮ LIỆU CŨ (TRÁNH TRÙNG LẶP ID)
-- ==========================================================
TRUNCATE TABLE KeyValidationLogs;
TRUNCATE TABLE ActivityLogs;
TRUNCATE TABLE Reviews;
TRUNCATE TABLE Transactions;
TRUNCATE TABLE Licenses;
TRUNCATE TABLE Orders;
TRUNCATE TABLE ProductPackages;
TRUNCATE TABLE ProductVersions;
TRUNCATE TABLE Products;
TRUNCATE TABLE Categories;
TRUNCATE TABLE Users;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- BƯỚC 3: NẠP DỮ LIỆU MẪU (SEED DATA)
-- ==========================================================

-- 1. USERS
INSERT INTO Users (user_id, username, email, password_hash, full_name, role, wallet_balance, is_active) VALUES 
(1, 'admin_root', 'admin@software-store.com', '$2a$10$DUMMY...', 'Quản Trị Viên Hệ Thống', 'admin', 0.00, 1),
(2, 'marketing_master', 'dev.mkt@agency.vn', '$2a$10$DUMMY...', 'Marketing Tools Studio', 'developer', 1500000.00, 1),
(3, 'dark_coder_9x', 'hacker@underground.net', '$2a$10$DUMMY...', 'Dark Coder Team', 'developer', 500000.00, 1),
(4, 'vip_buyer_hanoi', 'hung.bds@gmail.com', '$2a$10$DUMMY...', 'Nguyễn Văn Hùng (BĐS)', 'user', 250000.00, 1),
(5, 'student_it', 'nam.sv@uni.edu.vn', '$2a$10$DUMMY...', 'Trần Nam', 'user', 20000.00, 1),
(6, 'scammer_pro', 'scam@fake.com', '$2a$10$DUMMY...', 'Phạm Lừa Đảo', 'user', 0.00, 0);

-- 2. CATEGORIES
INSERT INTO Categories (category_id, name, description) VALUES 
(1, 'Marketing & SEO', 'Các công cụ tự động hóa Zalo, Facebook, Shopee...'),
(2, 'MMO & Game Tools', 'Auto game, Tool nuôi nick, Fake IP...'),
(3, 'Design & Graphics', 'Plugin, Action, Preset cho Designer'),
(4, 'System Utilities', 'Phần mềm dọn dẹp, tối ưu máy tính');

-- 3. PRODUCTS
INSERT INTO Products (product_id, developer_id, category_id, name, description, short_description, status, total_sales, view_count, rejection_reason) VALUES 
(1, 2, 1, 'Zalo Auto Sender Pro', '<h1>Zalo Auto Sender Pro</h1><p>Tính năng: Tự động kết bạn...</p>', 'Phần mềm Spam Zalo số 1 Việt Nam', 'approved', 150, 5000, NULL),
(2, 3, 2, 'Auto Vo Lam Mobile', '<h1>Auto Vo Lam Mobile</h1><p>Tự động làm nhiệm vụ dã tẩu...</p>', 'Auto VLTK Mobile mượt mà nhẹ máy', 'approved', 89, 2300, NULL),
(3, 3, 4, 'Super Cleaner 2025', '<h1>Super Cleaner 2025</h1><p>Dọn rác máy tính siêu tốc...</p>', 'Dọn dẹp PC chỉ 1 click', 'rejected', 0, 50, 'Phát hiện mã độc đào coin trong file cài đặt.'),
(4, 2, 1, 'Shopee Seo Top 1', '<h1>Shopee Seo Top 1</h1><p>Buff đơn ảo, tăng đánh giá...</p>', 'Tool SEO Shopee mới nhất', 'pending', 0, 10, NULL);

-- 4. VERSIONS & PACKAGES
INSERT INTO ProductVersions (product_id, version_number, source_code_path, build_file_path, virus_scan_status, is_current_version) VALUES 
(1, '2.1.0', 's3://src/zalo_v2.zip', 's3://build/ZaloPro_Setup.exe', 'clean', 1),
(2, '1.5.Beta', 's3://src/vltkm_v15.rar', 's3://build/AutoVL.exe', 'clean', 1),
(3, '1.0.0', 's3://src/cleaner.zip', 's3://build/virus.exe', 'infected', 1);

INSERT INTO ProductPackages (package_id, product_id, name, duration_days, price) VALUES 
(1, 1, '1 Tháng Trải Nghiệm', 30, 50000.00),
(2, 1, '1 Năm Tiết Kiệm', 365, 450000.00),
(3, 1, 'Vĩnh Viễn (Lifetime)', NULL, 1200000.00),
(4, 2, 'Thuê theo giờ (24h)', 1, 5000.00),
(5, 2, 'Gói Tháng', 30, 100000.00);

-- 5. TRANSACTIONS & ORDERS
-- Kịch bản 1: User 4
INSERT INTO Transactions (transaction_id, user_id, amount, type, description, created_at) VALUES 
(1, 4, 2000000.00, 'deposit', 'Nạp tiền qua Vietcombank - GD: VCB123456', NOW() - INTERVAL 5 DAY);

INSERT INTO Orders (order_id, user_id, total_amount, status, payment_method, created_at) VALUES 
(1, 4, 1200000.00, 'completed', 'WALLET', NOW() - INTERVAL 5 DAY);

INSERT INTO Transactions (transaction_id, user_id, amount, type, description, created_at) VALUES 
(2, 4, -1200000.00, 'purchase', 'Thanh toán đơn hàng #1', NOW() - INTERVAL 5 DAY),
(3, 2, 1200000.00, 'sale_revenue', 'Doanh thu bán hàng từ đơn #1 (Cộng tiền cho Dev)', NOW() - INTERVAL 5 DAY);

-- Kịch bản 2: User 5
INSERT INTO Transactions (transaction_id, user_id, amount, type, description, created_at) VALUES 
(4, 5, 50000.00, 'deposit', 'Nạp tiền Momo', NOW() - INTERVAL 2 DAY);

INSERT INTO Orders (order_id, user_id, total_amount, status, payment_method, created_at) VALUES 
(2, 5, 10000.00, 'completed', 'WALLET', NOW() - INTERVAL 2 DAY);

INSERT INTO Transactions (transaction_id, user_id, amount, type, description, created_at) VALUES 
(5, 5, -10000.00, 'purchase', 'Thanh toán đơn hàng #2', NOW() - INTERVAL 2 DAY),
(6, 3, 10000.00, 'sale_revenue', 'Doanh thu bán hàng từ đơn #2', NOW() - INTERVAL 2 DAY);

-- 6. LICENSES
INSERT INTO Licenses (license_id, order_id, product_id, user_id, package_id, license_key, start_date, expire_date, hardware_id, status) VALUES 
(1, 1, 1, 4, 3, 'ZALO-LIFE-8888-9999-AAAA', NOW() - INTERVAL 4 DAY, NULL, 'HWID-PC-GAMING-INTEL-I9', 'active'),
(2, 2, 2, 5, 4, 'AUTO-GAME-1111-2222-BBBB', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, 'HWID-LAPTOP-DELL-OLD', 'expired'),
(3, 1, 1, 4, 1, 'ZALO-GIFT-5555-6666-CCCC', NULL, NULL, NULL, 'unused'),
(4, 2, 2, 6, 5, 'AUTO-SCAM-0000-XXXX-YYYY', NOW() - INTERVAL 10 DAY, NOW() + INTERVAL 20 DAY, 'HWID-NET-QUAN-A', 'banned');

-- 7. REVIEWS & LOGS
INSERT INTO Reviews (product_id, user_id, rating, comment, created_at) VALUES 
(1, 4, 5, 'Tool chạy rất mượt, support nhiệt tình. Đáng tiền!', NOW() - INTERVAL 3 DAY),
(2, 5, 4, 'Tool ngon nhưng thi thoảng bị văng game.', NOW() - INTERVAL 1 DAY);

-- Insert Log kiểm tra key (Sẽ không còn lỗi font chữ)
INSERT INTO KeyValidationLogs (license_key, hardware_id, ip_address, status, fail_reason, request_time) VALUES 
('ZALO-LIFE-8888-9999-AAAA', 'HWID-PC-GAMING-INTEL-I9', '113.190.20.1', 'success', NULL, NOW() - INTERVAL 1 HOUR),
('AUTO-GAME-1111-2222-BBBB', 'HWID-LAPTOP-DELL-OLD', '14.162.10.5', 'failed', 'Expired (Key đã hết hạn)', NOW() - INTERVAL 5 MINUTE);