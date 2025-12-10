package com.smiledev.bum.controller;

import com.smiledev.bum.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String fieldName = paramNames.nextElement();
            String fieldValue = request.getParameter(fieldName);
            vnp_Params.put(fieldName, fieldValue);
        }

        int orderId = orderService.handleVnpayReturn(vnp_Params);
        String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode");

        if (orderId != -1 && "00".equals(vnp_ResponseCode)) {
            // Success
            return "redirect:/api/v1/orders/success?orderId=" + orderId;
        } else {
            // Failure or invalid signature
            return "redirect:/api/v1/orders/failure?orderId=" + orderId;
        }
    }
}
