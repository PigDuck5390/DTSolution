package com.dtsolution.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

public class AuthDto {

    @Getter @Setter
    public static class LoginRequest {
        @NotBlank(message = "사번을 입력해주세요.")
        private String empNo;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }

    @Getter @Setter
    public static class SignupRequest {

        @NotBlank(message = "사번을 입력해주세요.")
        @Size(min = 5, max = 20, message = "사번은 5~20자 이내로 입력해주세요.")
        private String empNo;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "영문, 숫자, 특수문자를 각 1개 이상 포함 (8자 이상)"
        )
        private String password;

        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        private String passwordConfirm;

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 50)
        private String name;

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;
    }
}