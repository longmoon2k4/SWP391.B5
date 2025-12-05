package com.smiledev.bum.service;

import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm kiếm Users entity trong database bằng username
        Users userEntity = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username));

        // 2. Chuyển đổi vai trò (role) từ enum của chúng ta thành một GrantedAuthority
        // Spring Security yêu cầu vai trò phải có tiền tố "ROLE_"
        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name().toUpperCase())
        );

        // 3. Trả về một đối tượng UserDetails mà Spring Security có thể sử dụng
        // Đối tượng này chứa username, password đã được mã hóa, và danh sách quyền
        // Độ chế để lấy fullname thay vì lấy username
        return new User(
                userEntity.getFullName(),
                userEntity.getPasswordHash(),
                authorities
        );
    }
}
