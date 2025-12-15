package com.smiledev.bum.controller;

import com.smiledev.bum.entity.*;
import com.smiledev.bum.repository.*;
import com.smiledev.bum.service.ActivityLogService;
import com.smiledev.bum.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private LicensesRepository licensesRepository;

    @Autowired
    private KeyValidationLogsRepository keyValidationLogsRepository;

    @Autowired
    private ReviewsRepository reviewsRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ProductReviewService productReviewService;

    /**
     * Admin Dashboard - Main View
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(
            Model model, 
            Authentication authentication,
            @RequestParam(defaultValue = "overview") String tab) {
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        // Statistics
        long totalUsersCount = userRepository.count();
        BigDecimal totalRevenue = transactionsRepository.calculateTotalRevenue(Transactions.Type.sale_revenue);
        long pendingProductsCount = productsRepository.countByStatus(Products.Status.pending);
        long activeLicensesCount = licensesRepository.countByStatus(Licenses.Status.active);
        long approvedProductsCount = productsRepository.countByStatus(Products.Status.approved);
        long rejectedProductsCount = productsRepository.countByStatus(Products.Status.rejected);

        model.addAttribute("totalUsers", totalUsersCount);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("pendingProductsCount", pendingProductsCount);
        model.addAttribute("activeLicensesCount", activeLicensesCount);
        model.addAttribute("approvedProductsCount", approvedProductsCount);
        model.addAttribute("rejectedProductsCount", rejectedProductsCount);
        model.addAttribute("totalOrders", ordersRepository.count());

        // Pending products for review tab
        Pageable pendingPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Products> pendingProducts = productsRepository.findByStatus(Products.Status.pending, pendingPageable);
        model.addAttribute("pendingProducts", pendingProducts);

        // Recent key validation logs
        List<KeyValidationLogs> recentLogs = keyValidationLogsRepository.findTop10ByOrderByRequestTimeDesc();
        model.addAttribute("keyValidationLogs", recentLogs);

        // Active tab
        model.addAttribute("activeTab", tab);

        return "admin-dashboard";
    }

    /**
     * Approve product via form submission
     */
    @PostMapping("/admin/product/{productId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveProduct(
            @PathVariable int productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<Users> adminOpt = userRepository.findByUsername(authentication.getName());
            if (adminOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Admin not found");
                return "redirect:/dashboard/admin?tab=review";
            }

            productReviewService.approveProduct(productId, adminOpt.get());
            redirectAttributes.addFlashAttribute("successMessage", "Product approved successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/admin?tab=review";
    }

    /**
     * Reject product via form submission
     */
    @PostMapping("/admin/product/{productId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectProduct(
            @PathVariable int productId,
            @RequestParam String rejectionReason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<Users> adminOpt = userRepository.findByUsername(authentication.getName());
            if (adminOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Admin not found");
                return "redirect:/dashboard/admin?tab=review";
            }

            productReviewService.rejectProduct(productId, rejectionReason, adminOpt.get());
            redirectAttributes.addFlashAttribute("successMessage", "Product rejected successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard/admin?tab=review";
    }


    /**
     * Developer Dashboard
     */
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

    /**
     * User Dashboard
     */
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
     * Redirects the user to their specific dashboard based on their role
     */
    @GetMapping
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/dashboard/admin";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"))) {
                return "redirect:/dashboard/developer";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
                return "redirect:/dashboard/user";
            }
        }

        return "redirect:/";
    }
}
