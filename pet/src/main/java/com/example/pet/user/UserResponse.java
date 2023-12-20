package com.example.pet.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String phoneNumber;
    private String address;
    private List<String> roles = new ArrayList<>();



    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.phoneNumber = user.getPhoneNumber();
        this.address = user.getAddress();
        this.roles = user.getRoles();
    }
}
