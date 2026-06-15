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
        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name().toUpperCase())
        );

        // 3. Trả về một đối tượng UserDetails với đầy đủ thông tin trạng thái tài khoản
        return new User(
                userEntity.getUsername(), // Phải là username để xác thực
                userEntity.getPasswordHash(),
                userEntity.isActive(), // enabled: true nếu active, false nếu không
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}
