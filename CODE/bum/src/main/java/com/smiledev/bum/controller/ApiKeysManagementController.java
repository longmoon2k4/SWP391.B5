package com.smiledev.bum.controller;

import com.smiledev.bum.dto.ApiKeyRequestDTO;
import com.smiledev.bum.dto.ApiKeyResponseDTO;
import com.smiledev.bum.entity.ApiKeys;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.ApiKeysRepository;
import com.smiledev.bum.repository.ApiUsageLogsRepository;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard/api-keys")
public class ApiKeysManagementController {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeysRepository apiKeysRepository;

    @Autowired
    private ApiUsageLogsRepository apiUsageLogsRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * List all API keys for developer
     */
    @GetMapping
    @PreAuthorize("hasRole('DEVELOPER')")
    public String listApiKeys(
            Model model,
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        Optional<Users> developerOpt = userRepository.findByUsername(authentication.getName());
        if (developerOpt.isEmpty()) {
            return "redirect:/dashboard/developer";
        }
        // Lấy thông tin người dùng đăng nhập
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        Users developer = developerOpt.get();
        
        // Get all API keys
        List<ApiKeys> allKeys = apiKeysRepository.findByDeveloper(developer);
        List<ApiKeyResponseDTO> keyDTOs = allKeys.stream()
                .map(key -> ApiKeyResponseDTO.fromEntity(key, false))
                .collect(Collectors.toList());

        model.addAttribute("apiKeys", keyDTOs);
        model.addAttribute("developerName", developer.getFullName());
        model.addAttribute("totalKeys", allKeys.size());
        model.addAttribute("activeKeys", allKeys.stream().filter(k -> k.getStatus() == ApiKeys.Status.active).count());

        return "developer-api-keys";
    }

    /**
     * Generate new API key
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('DEVELOPER')")
    public String generateKey(
            @RequestParam String keyName,
            @RequestParam(required = false) Integer rateLimit,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Users> developerOpt = userRepository.findByUsername(authentication.getName());
            if (developerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Developer not found");
                return "redirect:/dashboard/api-keys";
            }

            ApiKeyRequestDTO request = new ApiKeyRequestDTO(keyName, rateLimit);
            ApiKeyResponseDTO newKey = apiKeyService.generateApiKey(developerOpt.get(), request);

            redirectAttributes.addFlashAttribute("successMessage", "API key generated successfully!");
            redirectAttributes.addFlashAttribute("newApiKey", newKey.getApiKey()); // Show only once
            redirectAttributes.addFlashAttribute("newKeyName", newKey.getKeyName());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error generating key: " + e.getMessage());
        }

        return "redirect:/dashboard/api-keys";
    }

    /**
     * Revoke API key
     */
    @PostMapping("/{keyId}/revoke")
    @PreAuthorize("hasRole('DEVELOPER')")
    public String revokeKey(
            @PathVariable Integer keyId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Users> developerOpt = userRepository.findByUsername(authentication.getName());
            Optional<ApiKeys> keyOpt = apiKeysRepository.findById(keyId);

            if (developerOpt.isEmpty() || keyOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Key not found");
                return "redirect:/dashboard/api-keys";
            }

            ApiKeys key = keyOpt.get();
            if (key.getDeveloper().getUserId() != developerOpt.get().getUserId()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized");
                return "redirect:/dashboard/api-keys";
            }

            apiKeyService.revokeApiKey(keyId);
            redirectAttributes.addFlashAttribute("successMessage", "API key revoked successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error revoking key: " + e.getMessage());
        }

        return "redirect:/dashboard/api-keys";
    }

    /**
     * Delete API key
     */
    @PostMapping("/{keyId}/delete")
    @PreAuthorize("hasRole('DEVELOPER')")
    public String deleteKey(
            @PathVariable Integer keyId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Users> developerOpt = userRepository.findByUsername(authentication.getName());
            Optional<ApiKeys> keyOpt = apiKeysRepository.findById(keyId);

            if (developerOpt.isEmpty() || keyOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Key not found");
                return "redirect:/dashboard/api-keys";
            }

            ApiKeys key = keyOpt.get();
            if (key.getDeveloper().getUserId() != developerOpt.get().getUserId()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized");
                return "redirect:/dashboard/api-keys";
            }

            apiKeyService.deleteApiKey(keyId);
            redirectAttributes.addFlashAttribute("successMessage", "API key deleted successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting key: " + e.getMessage());
        }

        return "redirect:/dashboard/api-keys";
    }

    /**
     * View API key usage stats
     */
    @GetMapping("/{keyId}/usage")
    @PreAuthorize("hasRole('DEVELOPER')")
    public String viewKeyUsage(
            @PathVariable Integer keyId,
            Model model,
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        Optional<Users> developerOpt = userRepository.findByUsername(authentication.getName());
        Optional<ApiKeys> keyOpt = apiKeysRepository.findById(keyId);

        if (developerOpt.isEmpty() || keyOpt.isEmpty()) {
            return "redirect:/dashboard/api-keys";
        }

        ApiKeys key = keyOpt.get();
        if (key.getDeveloper().getUserId() != developerOpt.get().getUserId()) {
            return "redirect:/dashboard/api-keys";
        }

        // Get usage logs (paginated)
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "requestTime"));
        var usageLogs = apiUsageLogsRepository.findByApiKeyKeyId(keyId, pageable);

        // Calculate stats
        Double avgResponseTime = apiUsageLogsRepository.getAverageResponseTime(keyId);

        model.addAttribute("apiKey", ApiKeyResponseDTO.fromEntity(key, false));
        model.addAttribute("usageLogs", usageLogs.getContent());
        model.addAttribute("totalLogs", usageLogs.getTotalElements());
        model.addAttribute("avgResponseTime", avgResponseTime != null ? Math.round(avgResponseTime) : 0);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", usageLogs.getTotalPages());

        return "developer-api-key-usage";
    }
}
