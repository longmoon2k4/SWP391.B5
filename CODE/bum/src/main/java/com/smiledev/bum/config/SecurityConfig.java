package com.smiledev.bum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity // Kích hoạt tính năng bảo mật web của Spring Security
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Cấu hình quyền truy cập cho các request
                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép tất cả mọi người truy cập các đường dẫn này
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/product/**").permitAll()
                        // Tất cả các request khác đều cần phải được xác thực (đăng nhập)
                        .anyRequest().authenticated()
                )
                // 2. Cấu hình trang đăng nhập tùy chỉnh
                .formLogin(form -> form
                        .loginPage("/login") // Đường dẫn đến trang đăng nhập của chúng ta
                        .loginProcessingUrl("/login") // URL mà form sẽ POST đến để xử lý đăng nhập
                        .defaultSuccessUrl("/", true) // Chuyển hướng đến trang chủ sau khi đăng nhập thành công
                        .permitAll() // Cho phép tất cả mọi người truy cập trang đăng nhập
                )
                // 3. Cấu hình đăng xuất
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL để kích hoạt đăng xuất
                        .logoutSuccessUrl("/login?logout") // Chuyển hướng về trang login với một tham số để thông báo
                        .permitAll()
                )
                // 4. Xử lý lỗi xác thực cho API requests
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getHeader("Accept") != null && 
                                         request.getHeader("Accept").contains(MediaType.APPLICATION_JSON_VALUE)
                        )
                );

        return http.build();
    }
}