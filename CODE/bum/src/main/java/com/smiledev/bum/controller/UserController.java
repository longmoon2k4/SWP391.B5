package com.smiledev.bum.controller;

import com.smiledev.bum.dto.request.DepositRequest;
import com.smiledev.bum.dto.request.UpdateProfileRequest;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ActivityLogService;
import com.smiledev.bum.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/profile")
    public String showProfilePage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found while loading profile"));

        model.addAttribute("loggedInUser", user);

        if (!model.containsAttribute("updateProfileRequest")) {
            UpdateProfileRequest updateRequest = new UpdateProfileRequest();
            updateRequest.setFullName(user.getFullName());
            updateRequest.setEmail(user.getEmail());
            model.addAttribute("updateProfileRequest", updateRequest);
        }
        if (!model.containsAttribute("depositRequest")) {
            model.addAttribute("depositRequest", new DepositRequest());
        }

        return "profile";
    }

    @PostMapping("/profile/update")
    public String handleProfileUpdate(@Valid @ModelAttribute("updateProfileRequest") UpdateProfileRequest request,
                                      BindingResult bindingResult,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateProfileRequest", bindingResult);
            redirectAttributes.addFlashAttribute("updateProfileRequest", request);
            redirectAttributes.addFlashAttribute("errorTab", "profile"); // Đánh dấu tab có lỗi
            return "redirect:/profile";
        }

        String username = authentication.getName();
        try {
            Users updatedUser = userService.updateProfile(username, request);
            String description = String.format("User updated profile. New fullName: '%s', new email: '%s'", request.getFullName(), request.getEmail());
            activityLogService.logActivity(updatedUser, "UPDATE_PROFILE", "Users", updatedUser.getUserId(), description);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorTab", "profile"); // Đánh dấu tab có lỗi
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/deposit")
    public String handleDeposit(@Valid @ModelAttribute("depositRequest") DepositRequest request,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.depositRequest", bindingResult);
            redirectAttributes.addFlashAttribute("depositRequest", request);
            redirectAttributes.addFlashAttribute("errorTab", "deposit"); // Đánh dấu tab có lỗi
            return "redirect:/profile";
        }

        String username = authentication.getName();
        try {
            Users updatedUser = userService.depositToWallet(username, request.getAmount());
            String description = String.format("User deposited %s into wallet. New balance: %s", request.getAmount(), updatedUser.getWalletBalance());
            activityLogService.logActivity(updatedUser, "DEPOSIT", "Users", updatedUser.getUserId(), description);
            redirectAttributes.addFlashAttribute("successMessage", "Nạp tiền thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nạp tiền: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorTab", "deposit"); // Đánh dấu tab có lỗi
        }
        return "redirect:/profile";
    }
}
