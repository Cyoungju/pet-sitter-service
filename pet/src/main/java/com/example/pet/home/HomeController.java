package com.example.pet.home;

import com.example.pet.core.security.CustomUserDetails;
import com.example.pet.core.security.JwtAuthenticationFilter;
import com.example.pet.core.security.JwtTokenProvider;
import com.example.pet.user.User;
import com.example.pet.user.UserRepository;
import com.example.pet.user.UserRequest;
import com.example.pet.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class HomeController {

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("user", new UserRequest.JoinDTO());
        return "index";
    }

    @GetMapping("/petsitter")
    public String cartAdd() {
        return "petsitter";
    }

    @GetMapping("/register")
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
