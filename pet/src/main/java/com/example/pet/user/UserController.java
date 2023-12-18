package com.example.pet.user;

import com.example.pet.core.security.TokenDto;
import com.example.pet.core.utils.ApiUtils;
import com.example.pet.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userservice;

    @PostMapping("/register")
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
    public ResponseEntity<?> signin(@RequestBody UserRequest.JoinDTO request) throws Exception {
        return new ResponseEntity<>(userservice.login(request), HttpStatus.OK);
    }
    @GetMapping("/refresh")
    public ResponseEntity<TokenDto> refresh(@RequestBody TokenDto token) throws Exception {
        return new ResponseEntity<>( userservice.refreshAccessToken(token), HttpStatus.OK);
    }


}
