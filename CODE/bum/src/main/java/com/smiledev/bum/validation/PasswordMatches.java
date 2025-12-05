package com.smiledev.bum.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE) // Annotation này sẽ được dùng ở cấp độ class
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class) // Chỉ định lớp xử lý logic
@Documented
public @interface PasswordMatches {
    String message() default "Mật khẩu xác nhận không khớp";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
