package com.example.pet.core.security;

import com.example.pet.core.error.exception.Exception401;
import com.example.pet.core.error.exception.Exception403;
import com.example.pet.core.utils.FilterResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 환경 설정을 구성하기 위한 클래스
 * 웹 서비스가 로드 될때 Spring Container 의해 관리가 되는 클래스
 * 사용자에 대한 '인증', '인가'에 대한 구성을 Bean 메서드로 주입함
 *
 */


@Slf4j
@RequiredArgsConstructor
@Configuration // ** 현재 클래스를 (설정 클래스)로 설정
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
        /* BCrypt : 기본으로 사용. 가장 많이 사용되는 알고리즘.
         * SCrypt : 개발자가 직접 필요에 따라 변경 가능.
         * Argon2
         * PBKDF2
         * MD5
         * SHA-1, SHA-256 등
         */
    }

    public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity>{

        @Override
        public void configure(HttpSecurity httpSecurity) throws Exception {

            AuthenticationManager authenticationManager = httpSecurity.getSharedObject(
                    AuthenticationManager.class
            );

            httpSecurity.addFilter(new JwtAuthenticationFilter(authenticationManager));

            super.configure(httpSecurity);
        }
    }

    /*
     * HTTP에 대해서 '인증'과 '인가'를 담당하는 메서드
     * 필터를 통해 인증 방식과 인증 절차에 대해서 등록하며 설정을 담당하는 메서드
     * */

    @Bean // 스프링 빈으로 등록
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1. CSRF 해제 - 서버에 인증정보를 저장하지 않기때문에
        http.csrf().disable(); // postman 접근해야 함!! - CSR 할때!!

        // 2. iframe 거부 설정
        http.headers().frameOptions().sameOrigin();

        // 3. cors 재설정
        http.cors().configurationSource(configurationSource());

        // 4. jSessionId 사용 거부
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션 정책

        // 5. form 로긴 해제 (UsernamePasswordAuthenticationFilter 비활성화) (폼 로그인 비활성화)
        http.formLogin().disable();

        // 6. 로그인 인증창이 뜨지 않게 비활성화(기본 인증 비활성화)
        http.httpBasic().disable(); //bearer 방식으로

        // 7. 커스텀 필터 적용 (시큐리티 필터 교환) 커스텀 필터 적용
        http.apply(new CustomSecurityFilterManager());

        // 8. 인증 실패 처리
        http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
            log.warn("인증되지 않은 사용자가 자원에 접근하려 합니다 : " + authException.getMessage());
            FilterResponseUtils.unAuthorized(response, new Exception401("인증되지 않았습니다"));
        });

        // 9. 권한 실패 처리
        http.exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
            log.warn("권한이 없는 사용자가 자원에 접근하려 합니다 : " + accessDeniedException.getMessage());
            FilterResponseUtils.forbidden(response, new Exception403("권한이 없습니다"));
        });

        // 10. 인증, 권한 필터 설정 - 경로에 대한 인증 설정
        http.authorizeRequests(
                authorize -> authorize
                        .antMatchers("/petsitter/**","/carts/**", "/options/**", "/orders/**")
                        .authenticated()

                        .antMatchers("/admin/**")
                        .access("hasRole('ADMIN')")
                        // ("/admin/**")에 대한 요청은 ADMIN권한을 가진 회원만 승인한다.
                        // 회원 권한을 설정할때 , 반드시 "ROLE_"을 붙여야만 Security가 인식함
                        .anyRequest().permitAll() //다른 주소는 모두 허용
                // 모든 요청에 대해 인증을 요구 하지 않는다
                // 위에서 설정한 특정 경로에 대한 권한 인증 확인만 하고 다른 경로는 확인하지 않는다
        );
        // 11. 로그인 관련 설정 (이 부분 추가)
//        http.formLogin()
//            .loginPage("/login") // 로그인 페이지 지정
//            .permitAll();
//
//        // 12. 로그아웃 관련 설정 (이 부분 추가)
//        http.logout()
//            .permitAll();

        return http.build();
    }

    // ** 규칙: 헤더(Authorization), 메서드, IP 주소, 클라이언트으 쿠키 요청을 허용
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration corsConfigurationSource = new CorsConfiguration();
        corsConfigurationSource.addAllowedHeader("*"); // 모든 헤더를 허용
        corsConfigurationSource.addAllowedMethod("*"); // GET, POST, PUT, DELETE 등의 모든 메서드를 허용
        corsConfigurationSource.addAllowedOriginPattern("*"); // 모든 IP주소를 허용
        corsConfigurationSource.setAllowCredentials(true); // 클라이언트 쿠키 요청 허용
        corsConfigurationSource.addExposedHeader("Authorization"); // 헤더

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource
                = new UrlBasedCorsConfigurationSource();

        // ** (/) 들어오는 모든 유형의 URL 패턴을 허용.
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfigurationSource);
        return urlBasedCorsConfigurationSource;
    }
}












