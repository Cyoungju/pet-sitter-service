package com.example.pet.home;


import com.example.pet.core.security.JwtTokenProvider;
import com.example.pet.user.User;
import com.example.pet.user.UserRepository;
import com.example.pet.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class HomeService {
    private final UserService userService;

    public void userInfo(Model model, HttpServletRequest request){
        // 쿠키에서 토큰을 가져옵니다.
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    String token = cookie.getValue();
                    // 토큰에서 이메일 정보를 가져옵니다.
                    String email = JwtTokenProvider.verify(token.replace(JwtTokenProvider.TOKEN_PREFIX, "")).getSubject();
                    Optional<User> user = userService.findByMemberEmail(email);
                    if (user.isPresent()) {
                        model.addAttribute("user", user.get());
                    }
                    break;
                }
            }
        }
    }

}