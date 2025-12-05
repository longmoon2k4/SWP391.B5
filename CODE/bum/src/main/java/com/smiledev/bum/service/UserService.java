package com.smiledev.bum.service;

import com.smiledev.bum.dto.request.RegistrationRequest;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerNewUser(RegistrationRequest request) {
        // 1. Kiểm tra username hoặc email đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Error: Email is already in use!");
        }

        // 2. Tạo đối tượng Users mới
        Users newUser = new Users();
        newUser.setUsername(request.getUsername());
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());

        // 3. Mã hóa mật khẩu
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 4. Thiết lập vai trò (role)
        try {
            // Chuyển chuỗi thành enum (viết thường)
            Users.Role role = Users.Role.valueOf(request.getRole().toLowerCase());
            newUser.setRole(role);
        } catch (IllegalArgumentException e) {
            // Mặc định là 'user' nếu giá trị role không hợp lệ
            newUser.setRole(Users.Role.user);
        }

        // 5. Mặc định các giá trị khác
        // Sử dụng BigDecimal.ZERO cho kiểu BigDecimal
        newUser.setWalletBalance(BigDecimal.ZERO);
        // Sử dụng đúng tên setter là setActive
        newUser.setActive(true);

        // 6. Lưu vào database
        userRepository.save(newUser);
    }
}
