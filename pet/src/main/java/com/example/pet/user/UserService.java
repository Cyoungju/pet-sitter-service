package com.example.pet.user;

import com.example.pet.core.error.exception.Exception400;
import com.example.pet.core.error.exception.Exception500;
import com.example.pet.core.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;



    public void checkEmail(String email) {
        // 동일한 이메일이 있는지 확인.
        Optional<User> users = userRepository.findByEmail(email);
        if(users.isPresent()) {
            throw new Exception400("이미 존재하는 이메일 입니다. : " + email);
        }
    }

    @Transactional
    public void join(UserRequest.JoinDTO request) {
        checkEmail(request.getEmail());
        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .username(request.getUsername())
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();

            userRepository.save(user);
        }catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }


    public UserResponse login(UserRequest.JoinDTO request) throws Exception {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        //사용자의 이메일과 패스워드를 포함한 인증 토큰을 생성합
        Authentication authentication =  authenticationManager.authenticate(
                usernamePasswordAuthenticationToken
        );
        //사용자가 제공한 이메일과 비밀번호로 사용자를 인증하려고 하는것
        // ** 인증 완료 값을 받아온다.
        CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();


        return UserResponse.builder()
                .id(customUserDetails.getUser().getId())
                .username(customUserDetails.getUser().getUsername())
                .email(customUserDetails.getUser().getEmail())
                .roles(customUserDetails.getUser().getRoles())
                .token(
                        TokenDto.builder()
                                .access_token(jwtProvider.createToken(customUserDetails.getUser().getEmail(), customUserDetails.getUser().getRoles()))
                                .refresh_token(createRefreshToken(customUserDetails.getUser()))
                                .build()
                )
                .build();
    }



    /**
     * Refresh 토큰을 생성한다.
     * Redis 내부에는
     * refreshToken:userId : tokenValue
     * 형태로 저장한다.
     */
    public String createRefreshToken(User user) {
        Token token = tokenRepository.save(
                Token.builder()
                        .id(user.getId())
                        .refresh_token(UUID.randomUUID().toString())
                        .expiration(120)
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
        String email = jwtProvider.getEmail(token.getAccess_token());
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new Exception400("잘못된 계정정보입니다."));
        Token refreshToken = validRefreshToken(user, token.getRefresh_token());

        if (refreshToken != null) {
            return TokenDto.builder()
                    .access_token(jwtProvider.createToken(email, user.getRoles()))
                    .refresh_token(refreshToken.getRefresh_token())
                    .build();
        } else {
            throw new Exception("로그인을 해주세요");
        }
    }
}
