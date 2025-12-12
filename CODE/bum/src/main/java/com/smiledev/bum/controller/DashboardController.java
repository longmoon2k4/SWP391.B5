package com.smiledev.bum.controller;

import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {


    @Autowired
    private UserRepository userRepository;


    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        return "admin-dashboard";
    }

    @GetMapping("/developer")
    @PreAuthorize("hasRole('DEVELOPER')")
    public String developerDashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        return "developer-dashboard";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        return "user-dashboard";
    }

    /**
     * Redirects the user to their specific dashboard based on their role.
     * This is useful for generic links like a "Dashboard" button in the header.
     */



    @GetMapping
    public String dashboard(Model model,Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/dashboard/admin";
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"))) {
            return "redirect:/dashboard/developer";
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            return "redirect:/dashboard/user";
        }

        // Fallback for authenticated users with no specific role dashboard
        return "redirect:/";
    }
}
