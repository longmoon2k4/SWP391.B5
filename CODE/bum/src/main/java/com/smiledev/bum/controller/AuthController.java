package com.smiledev.bum.controller;

import com.smiledev.bum.dto.request.RegistrationRequest;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.service.ActivityLogService;
import com.smiledev.bum.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final ActivityLogService activityLogService; // Tiêm ActivityLogService

    @Autowired
    public AuthController(UserService userService, ActivityLogService activityLogService) {
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest());
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String handleRegistration(@Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            // Lấy về đối tượng Users sau khi đăng ký
            Users newUser = userService.registerNewUser(request);

            // Ghi log hành động đăng ký
            // Người dùng mới chưa đăng nhập nên user trong log có thể là null hoặc chính newUser
            activityLogService.logActivity(newUser, "REGISTER", "Users", newUser.getUserId(), "New user registered: " + newUser.getUsername());

            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login";
        }
    }
}
