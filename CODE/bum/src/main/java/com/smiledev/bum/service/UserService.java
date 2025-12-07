package com.smiledev.bum.service;

import com.smiledev.bum.dto.request.RegistrationRequest;
import com.smiledev.bum.dto.request.UpdateProfileRequest;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Users registerNewUser(RegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Error: Email is already in use!");
        }

        Users newUser = new Users();
        newUser.setUsername(request.getUsername());
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        try {
            Users.Role role = Users.Role.valueOf(request.getRole().toLowerCase());
            newUser.setRole(role);
        } catch (IllegalArgumentException e) {
            newUser.setRole(Users.Role.user);
        }

        newUser.setWalletBalance(BigDecimal.ZERO);
        newUser.setActive(true);

        return userRepository.save(newUser);
    }

    @Transactional
    public Users updateProfile(String username, UpdateProfileRequest request) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Kiểm tra xem email mới có bị trùng với người dùng khác không
        Optional<Users> userWithNewEmail = userRepository.findByEmail(request.getEmail());
        if (userWithNewEmail.isPresent() && userWithNewEmail.get().getUserId() != user.getUserId()) {
            throw new IllegalStateException("Email đã được sử dụng bởi một tài khoản khác.");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        
        return userRepository.save(user);
    }

    @Transactional
    public Users depositToWallet(String username, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        BigDecimal currentBalance = user.getWalletBalance();
        user.setWalletBalance(currentBalance.add(amount));
        
        return userRepository.save(user);
    }
}
