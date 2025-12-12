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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/orders")
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

    @GetMapping
    public String getUserOrders(Model model, Authentication authentication,
                                @RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        Optional<Users> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        if (userOpt.isEmpty()) {
            return "redirect:/error";
        }

        List<Orders> allOrders = ordersRepository.findByUser(userOpt.get());

        // Filter logic
        List<Orders> filteredOrders = allOrders.stream()
            .filter(order -> search == null || search.isEmpty() || String.valueOf(order.getOrderId()).contains(search))
            .filter(order -> status == null || status.isEmpty() || order.getStatus().name().equalsIgnoreCase(status))
            .filter(order -> startDate == null || !order.getCreatedAt().toLocalDate().isBefore(startDate))
            .filter(order -> endDate == null || !order.getCreatedAt().toLocalDate().isAfter(endDate))
            .collect(Collectors.toList());

        model.addAttribute("orders", filteredOrders);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        List<String> allStatuses = Stream.of(Orders.Status.values())
                                          .map(Enum::name)
                                          .collect(Collectors.toList());
        model.addAttribute("allStatuses", allStatuses);

        return "orders";
    }

    @PostMapping("/checkout")
    @ResponseBody
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

    @GetMapping("/details")
    public String showOrderDetails(@RequestParam("orderId") int orderId, Model model, Authentication authentication) {
//        if (authentication == null) return "redirect:/login";
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        else {
            return "redirect:/login";
        }
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy đơn hàng.");
            return "error";
        }

        String username = authentication.getName();
        if (!orderOpt.get().getUser().getUsername().equals(username)) {
            return "redirect:/error";
        }

        Orders order = orderOpt.get();
        List<Licenses> licenses = licensesRepository.findByOrder_OrderId(orderId);

        model.addAttribute("order", order);
        model.addAttribute("licenses", licenses);
        
        if (order.getStatus() == Orders.Status.completed) {
            model.addAttribute("statusMessage", "Thanh toán thành công!");
        } else if (order.getStatus() == Orders.Status.failed) {
            model.addAttribute("statusMessage", "Thanh toán không thành công.");
        } else if (order.getStatus() == Orders.Status.pending) {
            model.addAttribute("statusMessage", "Đơn hàng đang chờ xử lý.");
        } else {
            model.addAttribute("statusMessage", "Trạng thái đơn hàng: " + order.getStatus().name());
        }

        return "order-details";
    }

    @GetMapping("/vnpay-callback")
    public String vnpayCallback(HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String fieldName = paramNames.nextElement();
            String fieldValue = request.getParameter(fieldName);
            vnp_Params.put(fieldName, fieldValue);
        }

        int orderId = orderService.handleVnpayReturn(vnp_Params);

        if (orderId != -1) {
            return "redirect:/orders/details?orderId=" + orderId;
        } else {
            // Invalid signature or error
            return "redirect:/orders?error=payment_failed";
        }
    }

    // Redirect legacy /success endpoint to /details
    @GetMapping("/success")
    public String redirectSuccess(@RequestParam("orderId") int orderId) {
        return "redirect:/orders/details?orderId=" + orderId;
    }

    @GetMapping("/failure")
    public String orderFailure(@RequestParam(value = "orderId", required = false) Integer orderId, Model model) {
        if (orderId != null) {
            return "redirect:/orders/details?orderId=" + orderId;
        }
        model.addAttribute("message", "Thanh toán không thành công hoặc đã bị hủy.");
        return "order-failure";
    }
}
