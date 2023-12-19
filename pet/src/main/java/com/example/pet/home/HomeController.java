package com.example.pet.home;

import com.example.pet.core.security.CustomUserDetails;
import com.example.pet.core.security.JwtTokenProvider;
import com.example.pet.user.User;
import com.example.pet.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/")
    public String main(CustomUserDetails customUserDetails, Model model){
        System.out.println(customUserDetails.getUser());
        if(customUserDetails.getUser() == null){

        } else {
            String userEmail = customUserDetails.getUser().getEmail();
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            User user = userOptional.get();
            model.addAttribute("user", user);

        }
        return "index";
    }

    @GetMapping("/register")
    public String join() {
        return "join"; // "join.html" 파일을 렌더링
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // "login.html" 파일을 렌더링
    }
}
