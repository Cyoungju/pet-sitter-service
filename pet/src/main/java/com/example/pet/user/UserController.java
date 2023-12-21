package com.example.pet.user;

import com.example.pet.core.utils.ApiUtils;
import com.example.pet.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userservice;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserRequest.JoinDTO request, Error error) {
        userservice.join(request);
        return ResponseEntity.ok( ApiUtils.success(null) );
    }


    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestBody @Valid UserRequest.JoinDTO requestDTO, Error error) {
        userservice.checkEmail(requestDTO.getEmail());
        return ResponseEntity.ok( ApiUtils.success(null) );
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> signin(@RequestBody UserRequest.JoinDTO request, HttpServletResponse response, Error error) {
        String jwt = userservice.login(request);

        // "Bearer " 접두사 제거
        jwt = jwt.replace(JwtTokenProvider.TOKEN_PREFIX, "");

        // 쿠키 설정
        Cookie cookie = new Cookie("jwtToken", jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/"); // 모든 경로에서 쿠키 접근 가능
        response.addCookie(cookie);

        return ResponseEntity.ok().header(JwtTokenProvider.HEADER, jwt)
                .body(ApiUtils.success(null));
    }
}
