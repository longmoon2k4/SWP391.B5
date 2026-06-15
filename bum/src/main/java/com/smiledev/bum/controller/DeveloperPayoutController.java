package com.smiledev.bum.controller;

import java.math.BigDecimal;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smiledev.bum.entity.Transactions;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.TransactionsRepository;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ActivityLogService;

@Controller
@RequestMapping("/dashboard/developer/payouts")
@PreAuthorize("hasRole('DEVELOPER')")
public class DeveloperPayoutController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping
    public String showPayoutForm(Authentication authentication, Model model,
                                 @RequestParam(name = "page", defaultValue = "0") int page) {
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
        BigDecimal walletBalance = developer.getWalletBalance() != null ? developer.getWalletBalance() : BigDecimal.ZERO;
        Pageable pageable = PageRequest.of(Math.max(page, 0), 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transactions> withdrawals = transactionsRepository.findByUserAndTypeOrderByCreatedAtDesc(
                developer, Transactions.Type.withdrawal, pageable);

        model.addAttribute("walletBalance", walletBalance);
        model.addAttribute("withdrawals", withdrawals);
        return "developer-payouts";
    }

    @GetMapping("/history")
    public String payoutHistory(Authentication authentication,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                Model model) {
        return showPayoutForm(authentication, model, page);
    }

    @PostMapping
    public String requestPayout(@RequestParam("amount") BigDecimal amount,
                                @RequestParam(value = "note", required = false) String note,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        Users developer = currentDeveloper(authentication);
        if (developer == null) {
            return "redirect:/login";
        }

        BigDecimal currentBalance = developer.getWalletBalance() != null ? developer.getWalletBalance() : BigDecimal.ZERO;

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("error", "Số tiền rút phải lớn hơn 0");
            return "redirect:/dashboard/developer/payouts";
        }

        if (currentBalance.compareTo(amount) < 0) {
            redirectAttributes.addFlashAttribute("error", "Số dư không đủ để rút tiền");
            return "redirect:/dashboard/developer/payouts";
        }

        // Deduct balance and save transaction
        developer.setWalletBalance(currentBalance.subtract(amount));
        userRepository.save(developer);

        Transactions tx = new Transactions();
        tx.setUser(developer);
        tx.setAmount(amount);
        tx.setType(Transactions.Type.withdrawal);
        tx.setDescription(note != null ? note : "Yêu cầu rút tiền");
        transactionsRepository.save(tx);

        activityLogService.logActivity(developer, "withdrawal_request", "Transactions", tx.getTransactionId(), tx.getDescription());

        redirectAttributes.addFlashAttribute("success", "Tạo yêu cầu rút tiền thành công");
        return "redirect:/dashboard/developer/payouts";
    }

    private Users currentDeveloper(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        String username = authentication.getName();
        Optional<Users> userOpt = userRepository.findByUsername(username);
        return userOpt.orElse(null);
    }
}
