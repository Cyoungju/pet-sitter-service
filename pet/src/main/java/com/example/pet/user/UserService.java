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
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

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
                    .phoneNumber(request.getPhoneNumber())
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();

            userRepository.save(user);
        }catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }

    @Transactional
    public String login(UserRequest.JoinDTO request) {
        try{
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        //사용자의 이메일과 패스워드를 포함한 인증 토큰을 생성합
        Authentication authentication =  authenticationManager.authenticate(
                usernamePasswordAuthenticationToken
        );
        //사용자가 제공한 이메일과 비밀번호로 사용자를 인증하려고 하는것
        // ** 인증 완료 값을 받아온다.
        CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

        User user = userRepository.findByEmail(customUserDetails.getUser().getEmail()).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정정보입니다."));

        user.setRefreshToken(createRefreshToken(customUserDetails.getUser()));



        UserResponse userResponse = UserResponse.builder()
                .id(customUserDetails.getUser().getId())
                .username(customUserDetails.getUser().getUsername())
                .phoneNumber(customUserDetails.getUser().getPhoneNumber())
                .email(customUserDetails.getUser().getEmail())
                .roles(customUserDetails.getUser().getRoles())
                .token(
                        TokenDto.builder()
                                .access_token(jwtTokenProvider.create(customUserDetails.getUser()))
                                .refresh_token(user.getRefreshToken())
                                .build()
                )
                .build();


            // JWT 토큰 생성 및 반환
            String token = JwtTokenProvider.create(customUserDetails.getUser());

            return token;
        }catch (Exception e){
            // 401 반환.
            throw new Exception401(e.getMessage());
        }
    }



    /**
     * Refresh 토큰을 생성한다.
     * Redis 내부에는
     * refreshToken:userId : tokenValue
     * 형태로 저장한다.
     */
    @Transactional
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

    // 리프레시 토큰 유효성 검사
    @Transactional
    public Token validRefreshToken(User user, String refreshToken) throws Exception {
        // 사용자 id 기반으로 데이터 베이스에 해당 사용자 Token을 조회
        Token token = tokenRepository.findById(user.getId()).orElseThrow(() ->
                new Exception("만료된 계정입니다. 로그인을 다시 시도하세요"));
        // 만약 토큰이 존재 하지 않는다면 예외를 던짐

        // 해당유저의 Refresh 토큰 만료 : Redis에 해당 유저의 토큰이 존재하지 않음
        if (token.getRefresh_token() == null) {
            return null;
        } else {
            // 리프레시 토큰이 존재 하고,
            // 토큰이 같은지 비교 (입력으로 받은 Refresh Token이 실제 해당 사용자의 토큰과 일치 하지 않으면 )
            if(!token.getRefresh_token().equals(refreshToken)) {
                return null;
            } else { // 일치 한다면 현제 토큰 반환
                // 추가된 부분: 만약 Refresh Token의 만료 시간이 0이면 토큰을 만료시키고 로그아웃
                if (token.getExpiration() == 0) {
                    // 로그아웃 또는 토큰을 만료시키는 작업 수행
                    // 예를 들어, 다른 메소드를 호출하여 토큰을 만료시키거나 로그아웃 처리를 할 수 있습니다.
                    expireTokenAndLogout(user);

                    // 만료된 경우 null 반환 (또는 예외를 던질 수도 있음)
                    return null;
                }

                return token;
            }
        }
    }


    private void expireTokenAndLogout(User user) throws Exception {
        Token token = tokenRepository.findById(user.getId()).orElseThrow(() ->
                new Exception("회원정보를 찾을수 없습니다."));
        tokenRepository.delete(token);
    }




    @Transactional
    // TokenDto를 입력받아 사용자의 Access Token을 새로 고침 하고, 새로운 Access Token및  Refresh Token을 담은 TokenDto반환하는 메서드
    public TokenDto refreshAccessToken(TokenDto token) throws Exception {
        // 주어진 Access Token에서 이메일을 추출 함.
        String email = jwtTokenProvider.getEmail(token.getAccess_token());
        // 추출한 이메일을 가지고 디비에서 해당 이메일을 가진 사용자를 조회함.
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new Exception400("잘못된 계정정보입니다."));

        // 조회한 사용자와 입력받은  Refresh Token을 사용하여  Refresh Token의 유효성을 검사함
        Token refreshToken = validRefreshToken(user, token.getRefresh_token());

        //검사한  Refresh Token 이 null이 아닐경우
        if (refreshToken != null) {
            // 새로운 Access Token과 해당 사용자의 현재 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 생성하고 이를 담은 TokenDto를 반환
            return TokenDto.builder()
                    .access_token(jwtTokenProvider.create(user))
                    .refresh_token(refreshToken.getRefresh_token())
                    .build();
        } else {
            throw new Exception("로그인을 해주세요");
        }
    }

}
