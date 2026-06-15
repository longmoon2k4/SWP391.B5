package com.smiledev.bum.service;

import com.smiledev.bum.config.VNPAYConfig;
import com.smiledev.bum.entity.*;
import com.smiledev.bum.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductPackagesRepository productPackagesRepository;

    @Autowired
    private LicensesRepository licensesRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private VNPAYConfig vnPayConfig;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Orders createOrder(int packageId, Users user) {
        ProductPackages productPackage = productPackagesRepository.findById(packageId)
                .orElseThrow(() -> new NoSuchElementException("Package not found with id: " + packageId));

        Orders order = new Orders();
        order.setUser(user);
        order.setPackageId(packageId); // Store the package ID for later reference
        order.setTotalAmount(productPackage.getPrice());
        order.setStatus(Orders.Status.pending);
        order.setPaymentMethod("VNPAY");

        return ordersRepository.save(order);
    }

    @Transactional
    public int handleVnpayReturn(Map<String, String> vnp_Params) {
        String vnp_TxnRef = vnp_Params.get("vnp_TxnRef");
        int orderId = Integer.parseInt(vnp_TxnRef);
        String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode");
        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");

        // Remove vnp_SecureHash to recalculate and verify
        vnp_Params.remove("vnp_SecureHash");
        String hashData = vnPayConfig.hashAllFields(vnp_Params);
        String mySecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData);

        if (!mySecureHash.equalsIgnoreCase(vnp_SecureHash)) {
            // Invalid signature
            return -1; // Indicate error
        }

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId));

        // Check if order is already completed or failed
        if (order.getStatus() != Orders.Status.pending) {
            return orderId; // Already processed
        }

        if ("00".equals(vnp_ResponseCode)) {
            // --- SUCCESS ---
            order.setStatus(Orders.Status.completed);

            // Find the package associated with this order to create a license
            ProductPackages productPackage = findPackageForOrder(order);
            Products product = productPackage.getProduct();

            // Create License
            createLicenseForOrder(order, productPackage);

            // Create Transactions
            recordTransactions(order, product.getDeveloper());

            // Update totalSales count for the product
            product.setTotalSales(product.getTotalSales() + 1);
            productsRepository.save(product);

            activityLogService.logActivity(order.getUser(), "PURCHASE", "Orders", order.getOrderId(), "Purchased package " + productPackage.getPackageId());
        } else {
            // --- FAILURE ---
            order.setStatus(Orders.Status.failed);
            activityLogService.logActivity(order.getUser(), "PURCHASE_FAILED", "Orders", order.getOrderId(), "Purchase failed");
        }

        ordersRepository.save(order);
        return orderId;
    }

    private ProductPackages findPackageForOrder(Orders order) {
        // Use the packageId stored in the order
        if (order.getPackageId() != null) {
            return productPackagesRepository.findById(order.getPackageId())
                    .orElseThrow(() -> new NoSuchElementException("Package not found with id: " + order.getPackageId()));
        }
        
        // Fallback: find by price (not ideal but for backwards compatibility)
        return productPackagesRepository.findByPrice(order.getTotalAmount()).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not determine package for order " + order.getOrderId()));
    }

    private void createLicenseForOrder(Orders order, ProductPackages productPackage) {
        Licenses license = new Licenses();
        license.setOrder(order);
        license.setProduct(productPackage.getProduct());
        license.setUser(order.getUser());
        license.setProductPackage(productPackage);
        license.setLicenseKey(generateUniqueLicenseKey());
        license.setStatus(Licenses.Status.unused);

        if (productPackage.getDurationDays() != null) {
            // Start counting immediately after purchase
            LocalDateTime start = LocalDateTime.now();
            license.setStartDate(start);
            license.setExpireDate(start.plusDays(productPackage.getDurationDays()));
        } else {
            license.setExpireDate(null); // Represents lifetime
        }

        licensesRepository.save(license);
    }

    private void recordTransactions(Orders order, Users developer) {
        // 1. Debit from buyer's wallet (or just record the purchase)
        Transactions purchaseTransaction = new Transactions();
        purchaseTransaction.setUser(order.getUser());
        purchaseTransaction.setAmount(order.getTotalAmount().negate()); // Negative amount for purchase
        purchaseTransaction.setType(Transactions.Type.purchase);
        purchaseTransaction.setDescription("Thanh toan don hang #" + order.getOrderId());
        transactionsRepository.save(purchaseTransaction);

        // 2. Credit to developer's wallet
        // You might take a commission here
        BigDecimal revenue = order.getTotalAmount(); // Assuming 100% revenue for now
        Transactions revenueTransaction = new Transactions();
        revenueTransaction.setUser(developer);
        revenueTransaction.setAmount(revenue);
        revenueTransaction.setType(Transactions.Type.sale_revenue);
        revenueTransaction.setDescription("Doanh thu tu don hang #" + order.getOrderId());
        transactionsRepository.save(revenueTransaction);

        // 3. Update developer's wallet balance
        developer.setWalletBalance(developer.getWalletBalance().add(revenue));
        userRepository.save(developer); // Must save to persist the wallet balance change
    }

    private String generateUniqueLicenseKey() {
        // Simple UUID-based key generation
        return "BUM-" + UUID.randomUUID().toString().toUpperCase().substring(0, 18);
    }
}
