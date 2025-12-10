package com.smiledev.bum.controller;

import com.smiledev.bum.dto.PaymentResponseDTO;
import com.smiledev.bum.entity.Licenses;
import com.smiledev.bum.entity.Orders;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.LicensesRepository;
import com.smiledev.bum.repository.OrdersRepository;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.OrderService;
import com.smiledev.bum.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private LicensesRepository licensesRepository;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/checkout")
    @ResponseBody // Ensure the response is serialized to JSON
    public ResponseEntity<?> checkout(@RequestParam("packageId") int packageId,
                                      Authentication authentication,
                                      HttpServletRequest request) throws UnsupportedEncodingException {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String username = authentication.getName();
        Optional<Users> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Users currentUser = userOpt.get();
        Orders newOrder = orderService.createOrder(packageId, currentUser);

        String paymentUrl = transactionService.createVnpayPaymentUrl(newOrder, request);

        return ResponseEntity.ok(new PaymentResponseDTO(paymentUrl));
    }

    @GetMapping("/success")
    public String orderSuccess(@RequestParam("orderId") int orderId, Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) return "redirect:/error";

        String username = authentication.getName();
        if (!orderOpt.get().getUser().getUsername().equals(username)) {
            return "redirect:/error";
        }

        List<Licenses> licenses = licensesRepository.findByOrder_OrderId(orderId);

        model.addAttribute("order", orderOpt.get());
        model.addAttribute("licenses", licenses);
        return "order-success";
    }

    @GetMapping("/failure")
    public String orderFailure(@RequestParam(value = "orderId", required = false) Integer orderId, Model model) {
        if (orderId != null) {
            ordersRepository.findById(orderId).ifPresent(order -> model.addAttribute("order", order));
        }
        model.addAttribute("message", "Thanh toán không thành công hoặc đã bị hủy.");
        return "order-failure";
    }
}
