package com.example.pet.user;


import com.example.pet.core.error.exception.Exception400;
import com.example.pet.core.error.exception.Exception401;
import com.example.pet.core.error.exception.Exception500;
import com.example.pet.core.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Value("${jwt.secret}")
    String secretKey;

    @Transactional
    public void join(UserRequest.JoinDTO requestDTO) {
        checkEmail(requestDTO.getEmail());

        // 비밀번호 인코더
        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
        requestDTO.setPassword(encodedPassword);

        try {
            userRepository.save(requestDTO.toEntity());
        }catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }



    public void checkEmail(String email) {
        // 동일한 이메일이 있는지 확인.
        Optional<User> users = userRepository.findByEmail(email);
        if(users.isPresent()) {
            throw new Exception400("이미 존재하는 이메일 입니다. : " + email);
        }
    }

    public UserResponse login(UserRequest.JoinDTO requestDTO, HttpServletResponse response) {
        // ** 인증 작업.
        try{
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                    = new UsernamePasswordAuthenticationToken(requestDTO.getEmail(), requestDTO.getPassword());
            //사용자의 이메일과 패스워드를 포함한 인증 토큰을 생성합
            Authentication authentication =  authenticationManager.authenticate(
                    usernamePasswordAuthenticationToken
            );
            //사용자가 제공한 이메일과 비밀번호로 사용자를 인증하려고 하는것
            // ** 인증 완료 값을 받아온다.
            CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

            System.out.println(JwtTokenProvider.create(customUserDetails.getUser())+"11111111111111111111111111");

            String jwt = JwtTokenProvider.create(customUserDetails.getUser());


            return UserResponse.builder()
                    .id(customUserDetails.getUser().getId())
                    .username(customUserDetails.getUser().getUsername())
                    .email(customUserDetails.getUser().getEmail())
                    .roles(customUserDetails.getUser().getRoles())
                    .token(TokenDto.builder()
                            .access_token(JwtTokenProvider.create(customUserDetails.getUser()))
                            .refresh_token(createRefreshToken(customUserDetails.getUser()))
                            .build())
                    .build();
        }catch (Exception e){
            // 401 반환.
            throw new Exception401("인증되지 않음."+"11111111111111111111111111");
        }
    }


    public Optional<User> findByMemberEmail(String email) {
        // 멤버 이메일로 멤버를 찾아서 반환하는 로직
        // 예를 들어, 멤버 레포지토리를 이용해 멤버를 찾는다고 가정하면:

        Optional<User> foundMember = userRepository.findByEmail(email); // 멤버 레포지토리를 통해 이메일로 멤버 찾기

        return foundMember;
    }


    // Refresh Token ================

    /**
     * Refresh 토큰을 생성한다.
     * Redis 내부에는
     * refreshToken:memberId : tokenValue
     * 형태로 저장한다.
     */
    public String createRefreshToken(User user) {
        Token token = tokenRepository.save(
                Token.builder()
                        .id(user.getId())
                        .refresh_token(UUID.randomUUID().toString())
                        .expiration(300)
                        .build()
        );
        return token.getRefresh_token();
    }

    public Token validRefreshToken(User user, String refreshToken) throws Exception {
        Token token = tokenRepository.findById(user.getId()).orElseThrow(() -> new Exception("만료된 계정입니다. 로그인을 다시 시도하세요"));
        // 해당유저의 Refresh 토큰 만료 : Redis에 해당 유저의 토큰이 존재하지 않음
        if (token.getRefresh_token() == null) {
            return null;
        } else {
            // 리프레시 토큰 만료일자가 얼마 남지 않았을 때 만료시간 연장..?
            if(token.getExpiration() < 10) {
                token.setExpiration(1000);
                tokenRepository.save(token);
            }

            // 토큰이 같은지 비교
            if(!token.getRefresh_token().equals(refreshToken)) {
                return null;
            } else {
                return token;
            }
        }
    }


    public TokenDto refreshAccessToken(TokenDto token) throws Exception {
        String email = JwtTokenProvider.verify(JwtTokenProvider.TOKEN_PREFIX).getSubject();

        Optional<User> userEmail = findByMemberEmail(email);
        User user = userEmail.get();

        Token refreshToken = validRefreshToken(user, token.getRefresh_token());

        if (refreshToken != null) {
            return TokenDto.builder()
                    .access_token(JwtTokenProvider.create(user))
                    .refresh_token(refreshToken.getRefresh_token())
                    .build();
        } else {
            throw new Exception("로그인을 해주세요");
        }
    }

}
