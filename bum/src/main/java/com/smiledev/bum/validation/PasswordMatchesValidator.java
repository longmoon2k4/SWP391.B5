package com.smiledev.bum.validation;

import com.smiledev.bum.dto.request.RegistrationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegistrationRequest> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // Không cần khởi tạo gì đặc biệt
    }

    @Override
    public boolean isValid(RegistrationRequest request, ConstraintValidatorContext context) {
        // Logic so sánh password và confirmPassword
        return request.getPassword() != null && request.getPassword().equals(request.getConfirmPassword());
    }
}
