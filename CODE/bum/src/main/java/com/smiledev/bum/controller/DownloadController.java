package com.smiledev.bum.controller;

import com.smiledev.bum.entity.ProductVersions;
import com.smiledev.bum.entity.Products;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.entity.ProductVersions.VirusScanStatus;
import com.smiledev.bum.repository.LicensesRepository;
import com.smiledev.bum.repository.ProductVersionsRepository;
import com.smiledev.bum.repository.ProductsRepository;
import com.smiledev.bum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
public class DownloadController {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ProductVersionsRepository productVersionsRepository;

    @Autowired
    private LicensesRepository licensesRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/download/{productId}")
    public ResponseEntity<Resource> downloadProduct(@PathVariable("productId") int productId,
                                                    Authentication authentication) {
        // Require login
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/login"))
                    .build();
        }

        // Resolve user
        Optional<Users> userOpt = userRepository.findByUsername(authentication.getName());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Users currentUser = userOpt.get();

        // Resolve product
        Optional<Products> productOpt = productsRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Products product = productOpt.get();

        // Authorization: owner or buyer with license
        boolean isDeveloper = product.getDeveloper() != null && product.getDeveloper().getUserId() == currentUser.getUserId();
        boolean hasLicense = licensesRepository.existsByUserAndProduct(currentUser, product);
        if (!isDeveloper && !hasLicense) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get current version
        Optional<ProductVersions> versionOpt = productVersionsRepository.findByProductAndIsCurrentVersionTrue(product);
        if (!versionOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ProductVersions version = versionOpt.get();

        // Block if infected
        if (version.getVirusScanStatus() == VirusScanStatus.infected) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }

        Path filePath = Paths.get(version.getBuildFilePath());
        FileSystemResource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String filename = filePath.getFileName().toString();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
