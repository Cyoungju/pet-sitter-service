package com.example.pet.user;

import com.example.pet.core.error.exception.Exception400;
import com.example.pet.core.error.exception.Exception401;
import com.example.pet.core.error.exception.Exception500;
import com.example.pet.core.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public void checkEmail(String email) {
        // 동일한 이메일이 있는지 확인.
        Optional<User> users = userRepository.findByEmail(email);
        if(users.isPresent()) {
            throw new Exception400("이미 존재하는 이메일 입니다. : " + email);
        }
    }

    @Transactional
    public void join(UserRequest.JoinDTO requestDTO) {
        checkEmail(requestDTO.getEmail());
        String encodedPassword = passwordEncoder.encode( requestDTO.getPassword());
        requestDTO.setPassword(encodedPassword);
        try {
            userRepository.save(requestDTO.toEntity());
        }catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    @Transactional
    public String login(UserRequest.JoinDTO requestDTO) {
        // ** 인증 작업.
        try{
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                    = new UsernamePasswordAuthenticationToken(requestDTO.getEmail(), requestDTO.getPassword());
            //사용자의 이메일과 패스워드를 포함한 인증 토큰을 생성합
            Authentication authentication =  authenticationManager.authenticate(
                    usernamePasswordAuthenticationToken
            );
            // CustomUserDetailsService에 loadUserByUsername가 실행됨
            // authentication여기에 내 로그인한 정보가 담김

            //사용자가 제공한 이메일과 비밀번호로 사용자를 인증하려고 하는것
            // ** 인증 완료 값을 받아온다.
            CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

            // ** 토큰 발급 - 이 JWT 토큰은 사용자 인증을 통해 확인된 사용자의 정보를 포함
            return JwtTokenProvider.create(customUserDetails.getUser());
        }catch (Exception e){
            // 401 반환.
            throw new Exception401("인증되지 않음.");
        }
    }


    public Optional<User> findByEmail(String email) {
        Optional<User> foundMember = userRepository.findByEmail(email);
        return foundMember;
    }
}
