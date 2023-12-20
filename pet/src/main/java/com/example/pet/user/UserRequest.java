package com.example.pet.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collections;

public class UserRequest {

    @Getter
    @Setter
    public static class JoinDTO {

        @NotEmpty
        @Pattern(regexp = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "이메일 형식으로 작성해주세요")
        private String email;


        @NotEmpty
        @Size(min = 8, max = 20, message = "8자 이상 20자 이내로 작성 가능합니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&+=!~`<>,./?;:'\"\\[\\]{}\\\\()|_-])\\S*$", message = "영문, 숫자, 특수문자가 포함되어야하고 공백이 포함될 수 없습니다.")
        private String password;


        //@NotEmpty
        private String username;

        private String address;


        //@NotEmpty
        @Pattern(regexp = "^[0-9]{10,11}$", message = "휴대폰 번호는 숫자 10~11자리만 가능합니다.")
        private String phoneNumber;


        @Builder
        public User toEntity() {
            return User.builder()
                    .email(email)
                    .password(password)
                    .username(username)
                    .phoneNumber(phoneNumber)
                    .address(address)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();
        }
    }
}
