package com.example.pet.home;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.pet.core.security.CustomUserDetails;
import com.example.pet.core.security.JwtAuthenticationFilter;
import com.example.pet.core.security.JwtTokenProvider;
import com.example.pet.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final HomeService homeService;

    @GetMapping("/")
    public String main(Model model, HttpServletRequest request) {
        homeService.userInfo(model,request);
        return "index";
    }

    @GetMapping("/petsitter")
    public String cartAdd() {
        return "petsitter";
    }

    @GetMapping("/join")
    public String join() {
        return "join"; // "join.html" 파일을 렌더링
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // "login.html" 파일을 렌더링
    }

    @GetMapping("/carts")
    public String cart() {
        return "carts"; // "login.html" 파일을 렌더링
    }

}
