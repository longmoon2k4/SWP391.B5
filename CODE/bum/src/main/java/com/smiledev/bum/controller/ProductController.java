package com.smiledev.bum.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smiledev.bum.dto.ProductCardDTO;
import com.smiledev.bum.entity.Categories;
import com.smiledev.bum.entity.ProductPackages;
import com.smiledev.bum.entity.ProductVersions;
import com.smiledev.bum.entity.ProductVersions.VirusScanStatus;
import com.smiledev.bum.entity.Products;
import com.smiledev.bum.entity.Products.Status;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.CategoriesRepository;
import com.smiledev.bum.repository.OrdersRepository;
import com.smiledev.bum.repository.ProductPackagesRepository;
import com.smiledev.bum.repository.ProductVersionsRepository;
import com.smiledev.bum.repository.ProductsRepository;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ActivityLogService;
import com.smiledev.bum.service.ProductService;
import com.smiledev.bum.service.VirusScanService;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ProductVersionsRepository productVersionsRepository;

    @Autowired
    private ProductPackagesRepository productPackagesRepository;

    @Autowired
    private VirusScanService virusScanService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private OrdersRepository ordersRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "category", required = false) Integer categoryId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model, Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductCardDTO> productPage = productService.getApprovedProducts(categoryId, search, pageable);
        model.addAttribute("productPage", productPage);

        Iterable<Categories> categories = categoriesRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);

        return "products";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable("id") int id, Model model, Authentication authentication) {
        // Thêm trạng thái đăng nhập để header hiển thị đúng
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        productService.incrementViewCount(id);
        Optional<Products> productOpt = productService.findProductById(id);

        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            // Tăng lượt xem ở đây (sẽ làm sau nếu cần)
            return "product"; // Trả về file product.html
        } else {
            return "redirect:/"; // Nếu không tìm thấy, quay về trang chủ
        }
    }

    @PostMapping("/create")
    public String createProduct(
            @RequestParam("name") String name,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @RequestParam("description") String description,
            @RequestParam(value = "demoVideoUrl", required = false) String demoVideoUrl,
            @RequestParam("exeFile") MultipartFile exeFile,
            @RequestParam("versionNumber") String versionNumber,
            @RequestParam(value = "packageNames", required = false) String[] packageNames,
            @RequestParam(value = "packagePrices", required = false) Double[] packagePrices,
            @RequestParam(value = "packageDurations", required = false) Integer[] packageDurations,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Get current user
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/dashboard/developer";
            }
            Users user = userOpt.get();

            // Validate category
            Optional<Categories> categoryOpt = categoriesRepository.findById(categoryId);
            if (!categoryOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục");
                return "redirect:/dashboard/developer";
            }

            // Validate file
            if (exeFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file .exe");
                return "redirect:/dashboard/developer";
            }

            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file with unique name
            String originalFilename = exeFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(exeFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create Product
            Products product = new Products();
            product.setName(name);
            product.setCategory(categoryOpt.get());
            product.setShortDescription(shortDescription);
            product.setDescription(description);
            product.setDemoVideoUrl(demoVideoUrl);
            product.setDeveloper(user);
            product.setStatus(Status.pending);
            product.setTotalSales(0);
            product.setViewCount(0);
            Products savedProduct = productsRepository.save(product);

            // Create ProductVersion
            ProductVersions version = new ProductVersions();
            version.setProduct(savedProduct);
            version.setVersionNumber(versionNumber);
            version.setBuildFilePath(filePath.toString());
            version.setSourceCodePath(""); // Not required for now
            version.setVirusScanStatus(VirusScanStatus.pending);
            version.setCurrentVersion(true);
            ProductVersions savedVersion = productVersionsRepository.save(version);

            // Trigger virus scan asynchronously
            virusScanService.scanFileAsync(savedVersion, filePath.toString());

            // Create ProductPackages if any
            if (packageNames != null && packageNames.length > 0) {
                for (int i = 0; i < packageNames.length; i++) {
                    if (packageNames[i] != null && !packageNames[i].trim().isEmpty()) {
                        ProductPackages pkg = new ProductPackages();
                        pkg.setProduct(savedProduct);
                        pkg.setName(packageNames[i]);
                        pkg.setPrice(BigDecimal.valueOf(packagePrices[i]));
                        pkg.setDurationDays(packageDurations[i]);
                        productPackagesRepository.save(pkg);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được tạo thành công! Đang chờ admin xét duyệt.");
            return "redirect:/dashboard/developer";

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi upload file: " + e.getMessage());
            return "redirect:/dashboard/developer";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi tạo sản phẩm: " + e.getMessage());
            return "redirect:/dashboard/developer";
        }
    }

    // ===== Developer Product Management =====
    @GetMapping("/developer/manage")
    public String manageDeveloperProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "search", required = false) String search,
            Authentication authentication,
            Model model) {

        // Get current user (developer)
        String username = authentication.getName();
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        Users developer = userOpt.get();
        
        // Lấy thông tin người dùng đăng nhập
        if (authentication != null && authentication.isAuthenticated()) {
            String loggedInUsername = authentication.getName();
            Optional<Users> loggedInUserOpt = userRepository.findByUsername(loggedInUsername);
            loggedInUserOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        // Get products with pagination
        Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Products> productsPage = productsRepository.findByDeveloper(developer, pageable);

        // Filter by status if provided
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            try {
                Products.Status filterStatus = Products.Status.valueOf(status);
                productsPage = productsRepository.findByDeveloperAndStatus(developer, filterStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Invalid status, show all
            }
        }

        // Filter by search if provided
        if (search != null && !search.isEmpty()) {
            productsPage = productsRepository.findByDeveloperAndNameContainingIgnoreCase(developer, search, pageable);
        }

        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("totalElements", productsPage.getTotalElements());
        model.addAttribute("status", status);
        model.addAttribute("search", search);
        model.addAttribute("developer", developer);

        return "developer-manage-products";
    }

    // ===== Edit Product =====
    @GetMapping("/{productId}/edit")
    public String editProductForm(
            @PathVariable("productId") int productId,
            Authentication authentication,
            Model model) {

        // Get current user
        String username = authentication.getName();
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        Users developer = userOpt.get();

        // Get product
        Optional<Products> productOpt = productsRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return "redirect:/product/developer/manage";
        }

        Products product = productOpt.get();

        // Verify developer owns this product
        if (product.getDeveloper().getUserId() != developer.getUserId()) {
            return "redirect:/product/developer/manage";
        }

        // Add logged in user to model
        model.addAttribute("loggedInUser", developer);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoriesRepository.findAll());
        model.addAttribute("packages", productPackagesRepository.findByProduct(product));

        return "product-edit";
    }

    @PostMapping("/{productId}/edit")
    public String updateProduct(
            @PathVariable("productId") int productId,
            @RequestParam("name") String name,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @RequestParam("description") String description,
            @RequestParam(value = "demoVideoUrl", required = false) String demoVideoUrl,
            @RequestParam(value = "packageIds", required = false) Integer[] packageIds,
            @RequestParam(value = "packageNames", required = false) String[] packageNames,
            @RequestParam(value = "packagePrices", required = false) Double[] packagePrices,
            @RequestParam(value = "packageDurations", required = false) Integer[] packageDurations,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Get current user
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/product/developer/manage";
            }
            Users developer = userOpt.get();

            // Get product
            Optional<Products> productOpt = productsRepository.findById(productId);
            if (!productOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
                return "redirect:/product/developer/manage";
            }

            Products product = productOpt.get();

            // Verify developer owns this product
            if (product.getDeveloper().getUserId() != developer.getUserId()) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa sản phẩm này");
                return "redirect:/product/developer/manage";
            }

            // Validate category
            Optional<Categories> categoryOpt = categoriesRepository.findById(categoryId);
            if (!categoryOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục");
                return "redirect:/product/{productId}/edit";
            }

            // Update product fields
            product.setName(name);
            product.setCategory(categoryOpt.get());
            product.setShortDescription(shortDescription);
            product.setDescription(description);
            product.setDemoVideoUrl(demoVideoUrl);
            productsRepository.save(product);

            // Delete removed packages
            var existingPackages = productPackagesRepository.findByProduct(product);
            for (ProductPackages pkg : existingPackages) {
                boolean stillExists = false;
                if (packageIds != null) {
                    for (Integer pkgId : packageIds) {
                        if (pkgId != null && pkgId == pkg.getPackageId()) {
                            stillExists = true;
                            break;
                        }
                    }
                }
                if (!stillExists) {
                    productPackagesRepository.deleteById(pkg.getPackageId());
                }
            }

            // Add/update packages
            if (packageNames != null && packageNames.length > 0) {
                for (int i = 0; i < packageNames.length; i++) {
                    if (packageNames[i] != null && !packageNames[i].trim().isEmpty()) {
                        ProductPackages pkg;
                        if (packageIds != null && i < packageIds.length && packageIds[i] != null) {
                            // Update existing package
                            var pkgOpt = productPackagesRepository.findById(packageIds[i]);
                            pkg = pkgOpt.isPresent() ? pkgOpt.get() : new ProductPackages();
                        } else {
                            // Create new package
                            pkg = new ProductPackages();
                            pkg.setProduct(product);
                        }
                        pkg.setName(packageNames[i]);
                        pkg.setPrice(BigDecimal.valueOf(packagePrices[i]));
                        pkg.setDurationDays(packageDurations[i]);
                        productPackagesRepository.save(pkg);
                    }
                }
            }

            // Log activity
            activityLogService.logActivity(
                    developer,
                    "EDIT_PRODUCT",
                    "Products",
                    productId,
                    "Chỉnh sửa thông tin sản phẩm: " + name
            );

            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được cập nhật thành công");
            return "redirect:/product/developer/manage";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật sản phẩm: " + e.getMessage());
            return "redirect:/product/{productId}/edit";
        }
    }

    // Upgrade to new version
    @PostMapping("/{productId}/upgrade-version")
    public String upgradeVersion(
            @PathVariable("productId") int productId,
            @RequestParam("versionNumber") String versionNumber,
            @RequestParam("exeFile") MultipartFile exeFile,
            @RequestParam(value = "packageNames", required = false) String[] packageNames,
            @RequestParam(value = "packagePrices", required = false) Double[] packagePrices,
            @RequestParam(value = "packageDurations", required = false) Integer[] packageDurations,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Get product
            Optional<Products> productOpt = productsRepository.findById(productId);
            if (!productOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
                return "redirect:/product/developer/manage";
            }

            Products product = productOpt.get();

            // Verify developer owns this product
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent() || userOpt.get().getUserId() != product.getDeveloper().getUserId()) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền quản lý sản phẩm này");
                return "redirect:/product/developer/manage";
            }

            Users developer = userOpt.get();

            // Validate file
            if (exeFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file .exe");
                return "redirect:/product/developer/manage?search=" + product.getName();
            }

            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file with unique name
            String originalFilename = exeFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(exeFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Set old version to not current
            var currentVersionOpt = productVersionsRepository.findByProductAndIsCurrentVersionTrue(product);
            if (currentVersionOpt.isPresent()) {
                ProductVersions oldVersion = currentVersionOpt.get();
                oldVersion.setCurrentVersion(false);
                productVersionsRepository.save(oldVersion);
            }

            // Create new ProductVersion
            ProductVersions newVersion = new ProductVersions();
            newVersion.setProduct(product);
            newVersion.setVersionNumber(versionNumber);
            newVersion.setBuildFilePath(filePath.toString());
            newVersion.setSourceCodePath("");
            newVersion.setVirusScanStatus(VirusScanStatus.pending);
            newVersion.setCurrentVersion(true);
            ProductVersions savedVersion = productVersionsRepository.save(newVersion);

            // Trigger virus scan asynchronously
            virusScanService.scanFileAsync(savedVersion, filePath.toString());

            // Update product status to pending (for admin review)
            product.setStatus(Products.Status.pending);
            productsRepository.save(product);

            // Update packages if provided
            if (packageNames != null && packageNames.length > 0) {
                for (int i = 0; i < packageNames.length; i++) {
                    if (packageNames[i] != null && !packageNames[i].trim().isEmpty()) {
                        ProductPackages pkg = new ProductPackages();
                        pkg.setProduct(product);
                        pkg.setName(packageNames[i]);
                        pkg.setPrice(BigDecimal.valueOf(packagePrices[i]));
                        pkg.setDurationDays(packageDurations[i]);
                        productPackagesRepository.save(pkg);
                    }
                }
            }

            // Log activity
            activityLogService.logActivity(
                    developer,
                    "UPGRADE_VERSION",
                    "Products",
                    productId,
                    "Nâng cấp sản phẩm lên phiên bản " + versionNumber
            );

            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được nâng cấp thành công! Đang chờ admin xét duyệt.");
            return "redirect:/product/developer/manage";

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi upload file: " + e.getMessage());
            return "redirect:/product/developer/manage";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi nâng cấp sản phẩm: " + e.getMessage());
            return "redirect:/product/developer/manage";
        }
    }

    // Delete product
    @PostMapping("/{productId}/delete")
    public String deleteProduct(
            @PathVariable("productId") int productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Get product
            Optional<Products> productOpt = productsRepository.findById(productId);
            if (!productOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
                return "redirect:/product/developer/manage";
            }

            Products product = productOpt.get();

            // Verify developer owns this product
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent() || userOpt.get().getUserId() != product.getDeveloper().getUserId()) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa sản phẩm này");
                return "redirect:/product/developer/manage";
            }

            Users developer = userOpt.get();

            // Check if product has sales (orders)
            long salesCount = ordersRepository.countByProductId(productId);
            if (salesCount > 0) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm đã có bán hàng");
                return "redirect:/product/developer/manage";
            }

            // Only allow delete if status is pending or rejected
            if (product.getStatus() != Products.Status.pending && product.getStatus() != Products.Status.rejected) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể xóa sản phẩm ở trạng thái pending hoặc rejected");
                return "redirect:/product/developer/manage";
            }

            // Log activity
            activityLogService.logActivity(
                    developer,
                    "DELETE_PRODUCT",
                    "Products",
                    productId,
                    "Xóa sản phẩm: " + product.getName()
            );

            // Delete product (cascade will handle versions and packages)
            productsRepository.deleteById(productId);

            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được xóa thành công");
            return "redirect:/product/developer/manage";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi xóa sản phẩm: " + e.getMessage());
            return "redirect:/product/developer/manage";
        }
    }
}
