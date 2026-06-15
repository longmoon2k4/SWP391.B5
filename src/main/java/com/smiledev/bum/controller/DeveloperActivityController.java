package com.smiledev.bum.controller;

import com.smiledev.bum.entity.ActivityLogs;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.ActivityLogRepository;
import com.smiledev.bum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/dashboard/developer/activity")
@PreAuthorize("hasRole('DEVELOPER')")
public class DeveloperActivityController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @GetMapping
    public String activityList(Authentication authentication,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               Model model) {
        Users developer = currentDeveloper(authentication);
        if (developer == null) {
            return "redirect:/login";
        }
        // Lấy thông tin người dùng đăng nhập
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLogs> activities = activityLogRepository.findByUserOrderByCreatedAtDesc(developer, pageable);

        model.addAttribute("activities", activities);
        return "developer-activity";
    }

    private Users currentDeveloper(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Optional<Users> userOpt = userRepository.findByUsername(authentication.getName());
        return userOpt.orElse(null);
    }
}
