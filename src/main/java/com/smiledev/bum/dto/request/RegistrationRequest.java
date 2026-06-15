package com.smiledev.bum.dto.request;

import com.smiledev.bum.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatches // Annotation tùy chỉnh để so sánh password
public class RegistrationRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 6, max = 50, message = "Tên đăng nhập phải từ 6 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_.]*$", message = "Tên đăng nhập chỉ được chứa chữ cái, số, dấu gạch dưới và dấu chấm")
    private String username;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "Mật khẩu phải chứa ít nhất một chữ số, một chữ hoa và một chữ thường")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    private String role;
}
