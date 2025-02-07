package seb45_main_029.server.security.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import seb45_main_029.server.redis.common.RedisUtil;
import seb45_main_029.server.security.auth.filter.JwtAuthenticationFilter;
import seb45_main_029.server.security.auth.filter.JwtVerificationFilter;
import seb45_main_029.server.security.auth.handler.*;
import seb45_main_029.server.security.auth.jwt.JwtTokenizer;
import seb45_main_029.server.security.auth.service.OAuth2UserService;
import seb45_main_029.server.security.auth.utils.CustomAuthorityUtils;
import seb45_main_029.server.user.service.UserService;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final OAuth2UserService oAuth2UserService;
    private final RedisUtil redisUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .cors(Customizer.withDefaults())
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 생성하지 않도록 설정
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling()    // 예외 처리
                .authenticationEntryPoint(new UserAuthenticationEntryPointImp())
                .accessDeniedHandler(new UserAccessDeniedHandlerIpl())
                .and()
                .apply(new CustomFilterConfigurer())    // CustomFilterConfigurer 인스턴스 생성
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers(HttpMethod.POST, "/users/signup").permitAll()         // 회원가입 전체 접근 가능
                        .antMatchers(HttpMethod.PATCH, "/users/mypage/edit/**").hasRole("USER")  // 마이페이지 수정 -> 해당 user만
                        .antMatchers(HttpMethod.GET, "/users").permitAll()     // userinfo (전체 회원 조회) -> 전체 접근 가능
                        .antMatchers(HttpMethod.GET, "/users/mypage/**").hasAnyRole("USER", "ADMIN")  // mypage 역할 가진 사용자
                        .antMatchers(HttpMethod.DELETE, "/users/**").hasRole("USER")  // user 삭제 page -> 해당 user 만

                        .antMatchers(HttpMethod.POST, "/questions/**").hasAnyRole("USER", "ADMIN") // user/ask 역할 가진 사용자
                        .antMatchers(HttpMethod.PATCH, "/questions/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "/questions").permitAll()
                        .antMatchers(HttpMethod.GET, "/guestions/**").permitAll()   // 질문 조회 -> 전체 접근 가능
                        .antMatchers(HttpMethod.DELETE, "/questions/delete/**").hasAnyRole("USER", "ADMIN")

                        .antMatchers(HttpMethod.POST, "/answer/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/answer/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/answer/**").hasAnyRole("USER", "ADMIN")
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint()
                        .userService(oAuth2UserService)
                        .and()
                        .successHandler(new OAuth2AuthenticationSuccessHandler(jwtTokenizer, authorityUtils))
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:5173", "http://127.0.0.1:5173", "http://seb45main029.s3-website.ap-northeast-2.amazonaws.com"));
        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 자격증명 (예: 쿠키, 인증 헤더 등)을 허용
        configuration.setAllowCredentials(true);
        // 허용할 출처 패턴 설정 -> 이전 버전으로 setAllowCredentials과 사용 가능
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // 클라이언트에 노출할 헤더 설정
        configuration.setExposedHeaders(Arrays.asList("*"));
        // 지정한 HTTPMethod에 대한 통신 허용
        // "OPTIONS" : 프리플라이트 요청
        configuration.setAllowedMethods(Arrays.asList("*"));   // 지정한 HTTPMethod에 대한 통신 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 엔드포인트에 구성한 CORS 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /*
     * JwtAuthenticationFilter 등록하는 CustomFilterConfigurer 클래스
     *
     */
    private class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {

        @Override
        public void configure(HttpSecurity builder) throws Exception {

            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer,redisUtil);
            jwtAuthenticationFilter.setFilterProcessesUrl("/auth/login");   // ⏹️  request URL 체크
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new UserAuthenticationSuccessHandler());
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new UserAuthenticationFailureHandler());

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils,redisUtil);

            builder
                    .addFilter(jwtAuthenticationFilter) // Spring Security Filter Chain에 추가
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);  // JwtAuthenticationFilter 뒤에 jwtVerificationFilter 보내겠다
        }
    }
}
