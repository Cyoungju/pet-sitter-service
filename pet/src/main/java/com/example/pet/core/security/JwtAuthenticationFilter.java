package com.example.pet.core.security;


import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.pet.user.StringArrayConverter;
import com.example.pet.user.User;
import com.example.pet.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {


    private TokenRepository tokenRepository;
    private UserRepository userRepository;
    private String secretKey;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    // ** Http 요청이 발생할 때마다 호출되는 메서드.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String prefixJwt = request.getHeader(JwtTokenProvider.HEADER);

        // ** 헤더가 없다면 더이상 이 메서드에서 할 일은 없음. 다음으로 넘김.
        if(prefixJwt == null) {
            chain.doFilter(request, response);
            return;
        }

        // ** Bearer 제거.
        String jwt = prefixJwt.replace(JwtTokenProvider.TOKEN_PREFIX, "");

        try {
            log.debug("토근 있음.");

            // ** 토큰 검증
            DecodedJWT decodedJWT = JwtTokenProvider.verify(jwt);

            // ** 사용자 정보 추출.
            Long id = decodedJWT.getClaim("id").asLong();
            String roles = decodedJWT.getClaim("roles").asString();

            // ** 권한 정보를 문자열 리스트로 변환.
            StringArrayConverter stringArrayConverter = new StringArrayConverter();
            List<String> rolesList = stringArrayConverter.convertToEntityAttribute(roles);

            // ** 추출한 정보로 유저를 생성.
            User user = User.builder().id(id).roles(rolesList).build();
            CustomUserDetails customUserDetails = new CustomUserDetails(user);

            // ** Spring Security 가 인증 정보를 관리하는데 사용.
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    customUserDetails.getPassword(),
                    customUserDetails.getAuthorities()
            );

            // ** SecurityContext에 저장.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("인증 객체 생성");
        }
        catch (SignatureVerificationException sve) {
            log.debug("토큰 검증 실패");
        }
        catch (TokenExpiredException tee) {
            log.debug("토큰 사용 만료");

            // redis에 저장되어있는 토큰 정보를 만료된 access token으로 찾아온다.
            Optional<Token> foundTokenInfo = tokenRepository.findByAccessToken(jwt) ;
            Token tokenget = foundTokenInfo.get();

            String refreshToken = tokenget.getRefresh_token();

            // 만약 refresh 토큰도 만료되었다면, ExceptionHandlerFilter에서 처리된다.
            isExpired(refreshToken, secretKey);



        } finally {
            // ** 필터로 응답을 넘긴다.
            chain.doFilter(request, response);
        }

    }
    private static Claims extractClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public static boolean isExpired(String token, String secretKey) {
        Date expiredDate = extractClaims(token, secretKey).getExpiration();
        return expiredDate.before(new Date());
    }



}