package com.example.pet.home;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final HomeService homeService;

    @GetMapping("/")
    public String main(Model model, HttpServletRequest request) {
        homeService.userInfo(model, request);



        return "index";
    }

    @GetMapping("/join")
    public String join() {
        return "join"; // "join.html" 파일을 렌더링
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // "login.html" 파일을 렌더링
    }
}
