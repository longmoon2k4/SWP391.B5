package com.smiledev.bum.controller;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smiledev.bum.entity.KeyValidationLogs;
import com.smiledev.bum.entity.Licenses;
import com.smiledev.bum.entity.Products;
import com.smiledev.bum.entity.Transactions;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.ActivityLogRepository;
import com.smiledev.bum.repository.KeyValidationLogsRepository;
import com.smiledev.bum.repository.LicensesRepository;
import com.smiledev.bum.repository.OrdersRepository;
import com.smiledev.bum.repository.ProductsRepository;
import com.smiledev.bum.repository.TransactionsRepository;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ActivityLogService;
import com.smiledev.bum.service.ProductReviewService;

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
    private ActivityLogRepository activityLogRepository;


    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ProductReviewService productReviewService;

    // ===== Users Management =====
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String usersTab(Model model,
                           @RequestParam(name = "userPage", defaultValue = "0") int userPage) {
        Pageable pageable = PageRequest.of(Math.max(userPage, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        var usersPage = userRepository.findAll(pageable);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("userPage", userPage);
        model.addAttribute("userTotalPages", Math.max(1, usersPage.getTotalPages()));
        model.addAttribute("activeTab", "users");
        return "admin-dashboard";
    }

    @PostMapping("/admin/users/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public String banUser(@PathVariable int userId,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        Optional<Users> adminOpt = userRepository.findByUsername(authentication.getName());
        Optional<Users> targetOpt = userRepository.findById(userId);
        if (adminOpt.isEmpty() || targetOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/dashboard/admin?tab=users";
        }
        Users target = targetOpt.get();
        target.setActive(false);
        userRepository.save(target);
        activityLogService.logActivity(adminOpt.get(), "ban", "Users", target.getUserId(), "Banned user " + target.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "User banned successfully");
        return "redirect:/dashboard/admin?tab=users";
    }

    @PostMapping("/admin/users/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public String unbanUser(@PathVariable int userId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        Optional<Users> adminOpt = userRepository.findByUsername(authentication.getName());
        Optional<Users> targetOpt = userRepository.findById(userId);
        if (adminOpt.isEmpty() || targetOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/dashboard/admin?tab=users";
        }
        Users target = targetOpt.get();
        target.setActive(true);
        userRepository.save(target);
        activityLogService.logActivity(adminOpt.get(), "unban", "Users", target.getUserId(), "Unbanned user " + target.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "User unbanned successfully");
        return "redirect:/dashboard/admin?tab=users";
    }

    @PostMapping("/admin/users/{userId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUser(@PathVariable int userId,
                           @RequestParam String email,
                           @RequestParam String fullName,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        Optional<Users> adminOpt = userRepository.findByUsername(authentication.getName());
        Optional<Users> targetOpt = userRepository.findById(userId);
        if (adminOpt.isEmpty() || targetOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/dashboard/admin?tab=users";
        }
        Users target = targetOpt.get();
        target.setEmail(email);
        target.setFullName(fullName);
        userRepository.save(target);
        activityLogService.logActivity(adminOpt.get(), "edit", "Users", target.getUserId(), "Edited user profile");
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
        return "redirect:/dashboard/admin?tab=users";
    }

    // ===== Products Management =====
    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public String productsTab(Model model,
                              @RequestParam(name = "productPage", defaultValue = "0") int productPage,
                              @RequestParam(name = "status", required = false) Products.Status status) {
        Pageable pageable = PageRequest.of(Math.max(productPage, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status == null) {
            var page = productsRepository.findAll(pageable);
            model.addAttribute("products", page.getContent());
            model.addAttribute("productTotalPages", Math.max(1, page.getTotalPages()));
        } else {
            var products = productsRepository.findByStatus(status, pageable);
            model.addAttribute("products", products);
            long total = productsRepository.countByStatus(status);
            int totalPages = (int) Math.max(1, Math.ceil(total / 10.0));
            model.addAttribute("productTotalPages", totalPages);
        }
        model.addAttribute("productPage", productPage);
        model.addAttribute("filterStatus", status != null ? status.name() : "ALL");
        model.addAttribute("activeTab", "products");
        return "admin-dashboard";
    }

    @PostMapping("/admin/products/{productId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProductStatus(@PathVariable int productId,
                                      @RequestParam Products.Status status,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        Optional<Users> adminOpt = userRepository.findByUsername(authentication.getName());
        Optional<Products> productOpt = productsRepository.findById(productId);
        if (adminOpt.isEmpty() || productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found");
            return "redirect:/dashboard/admin?tab=products";
        }
        Products p = productOpt.get();
        p.setStatus(status);
        productsRepository.save(p);
        activityLogService.logActivity(adminOpt.get(), "update_status", "Products", p.getProductId(), "Set status to " + status.name());
        redirectAttributes.addFlashAttribute("successMessage", "Product status updated");
        return "redirect:/dashboard/admin?tab=products";
    }

    /**
     * Admin Dashboard - Main View
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
        public String adminDashboard(
            Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "overview") String tab,
            @RequestParam(name = "pendingPage", defaultValue = "0") int pendingPage,
            @RequestParam(name = "logsPage", defaultValue = "0") int logsPage,
            @RequestParam(name = "systemLogsPage", defaultValue = "0") int systemLogsPage,
            @RequestParam(name = "userPage", defaultValue = "0") int userPage,
            @RequestParam(name = "productPage", defaultValue = "0") int productPage,
            @RequestParam(name = "status", required = false) String status) {
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        // Statistics
        long totalUsersCount = userRepository.count();
        BigDecimal totalRevenue = transactionsRepository.calculateTotalRevenue(Transactions.Type.sale_revenue);
        BigDecimal totalWithdrawal = transactionsRepository.calculateTotalRevenue(Transactions.Type.withdrawal);
        long pendingProductsCount = productsRepository.countByStatus(Products.Status.pending);
        long activeLicensesCount = licensesRepository.countByStatus(Licenses.Status.active);
        long approvedProductsCount = productsRepository.countByStatus(Products.Status.approved);
        long rejectedProductsCount = productsRepository.countByStatus(Products.Status.rejected);

        model.addAttribute("totalUsers", totalUsersCount);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("totalWithdrawal", totalWithdrawal != null ? totalWithdrawal : BigDecimal.ZERO);
        model.addAttribute("pendingProductsCount", pendingProductsCount);
        model.addAttribute("activeLicensesCount", activeLicensesCount);
        model.addAttribute("approvedProductsCount", approvedProductsCount);
        model.addAttribute("rejectedProductsCount", rejectedProductsCount);
        model.addAttribute("totalOrders", ordersRepository.count());

        // Pending products (paged, 5 per page) - with eager loading to avoid lazy loading issues
        Pageable pendingPageable = PageRequest.of(Math.max(pendingPage, 0), 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Products> pendingProducts = productsRepository.findByStatusWithDetails(Products.Status.pending, pendingPageable);
        model.addAttribute("pendingProducts", pendingProducts);
        model.addAttribute("pendingPage", pendingPage);
        long totalPending = pendingProductsCount;
        int pendingTotalPages = (int) Math.max(1, Math.ceil(totalPending / 5.0));
        model.addAttribute("pendingTotalPages", pendingTotalPages);

        // Recent key validation logs (paged, 5 per page)
        Pageable logsPageable = PageRequest.of(Math.max(logsPage, 0), 5, Sort.by(Sort.Direction.DESC, "requestTime"));
        List<KeyValidationLogs> recentLogs = keyValidationLogsRepository.findAll(logsPageable).getContent();
        long totalKeyLogs = keyValidationLogsRepository.count();
        int keyLogsTotalPages = (int) Math.max(1, Math.ceil(totalKeyLogs / 5.0));
        model.addAttribute("keyValidationLogs", recentLogs);
        model.addAttribute("logsPage", logsPage);
        model.addAttribute("keyLogsTotalPages", keyLogsTotalPages);

        // System Activity Logs (paged, 5 per page)
        Pageable systemLogsPageable = PageRequest.of(Math.max(systemLogsPage, 0), 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        var systemLogs = activityLogRepository.findAll(systemLogsPageable);
        model.addAttribute("systemLogs", systemLogs.getContent());
        model.addAttribute("systemLogsPage", systemLogsPage);
        model.addAttribute("systemLogsTotalPages", Math.max(1, systemLogs.getTotalPages()));

        // Users tab data (10 per page)
        if ("users".equalsIgnoreCase(tab)) {
            Pageable pageable = PageRequest.of(Math.max(userPage, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            var usersPage = userRepository.findAll(pageable);
            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("userPage", userPage);
            model.addAttribute("userTotalPages", Math.max(1, usersPage.getTotalPages()));
        }

        // Products tab data (10 per page)
        if ("products".equalsIgnoreCase(tab)) {
            Pageable pageable = PageRequest.of(Math.max(productPage, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            Products.Status statusEnum = null;
            if (status != null && !status.isBlank()) {
                try {
                    statusEnum = Products.Status.valueOf(status);
                } catch (IllegalArgumentException ex) {
                    statusEnum = null;
                }
            }
            if (statusEnum == null) {
                var page = productsRepository.findAll(pageable);
                model.addAttribute("products", page.getContent());
                model.addAttribute("productTotalPages", Math.max(1, page.getTotalPages()));
            } else {
                var products = productsRepository.findByStatus(statusEnum, pageable);
                model.addAttribute("products", products);
                long total = productsRepository.countByStatus(statusEnum);
                int totalPages = (int) Math.max(1, Math.ceil(total / 10.0));
                model.addAttribute("productTotalPages", totalPages);
            }
            model.addAttribute("productPage", productPage);
            model.addAttribute("filterStatus", statusEnum != null ? statusEnum.name() : "ALL");
        }

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
