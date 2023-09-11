package seb45_main_029.server.security.config;


import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import seb45_main_029.server.security.auth.filter.JwtAuthenticationFilter;
import seb45_main_029.server.security.auth.filter.JwtVerificationFilter;
import seb45_main_029.server.security.auth.handler.UserAccessDeniedHandlerIpl;
import seb45_main_029.server.security.auth.handler.UserAuthenticationEntryPointImp;
import seb45_main_029.server.security.auth.handler.UserAuthenticationFailureHandler;
import seb45_main_029.server.security.auth.handler.UserAuthenticationSuccessHandler;
import seb45_main_029.server.security.auth.jwt.JwtTokenizer;
import seb45_main_029.server.security.auth.utils.CustomAuthorityUtils;

import java.util.Arrays;

@Configuration
@AllArgsConstructor
public class SecurityConfiguration {

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http

                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
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

                        .antMatchers(HttpMethod.POST, "/questions/ask").hasAnyRole("USER", "ADMIN") // user/ask 역할 가진 사용자
                        .antMatchers(HttpMethod.PATCH, "/questions/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "/questions").permitAll()
                        .antMatchers(HttpMethod.GET, "/guestions/**").permitAll()   // 질문 조회 -> 전체 접근 가능
                        .antMatchers(HttpMethod.DELETE, "/questions/delete/**").hasAnyRole("USER", "ADMIN")

                        .antMatchers(HttpMethod.POST, "/questions/**/answers").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/questions/**/answers/**").hasAnyRole("USER","ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/questions/**/answers/**").hasAnyRole("USER","ADMIN")
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
        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 자격증명 (예: 쿠키, 인증 헤더 등)을 허용
        configuration.setAllowCredentials(true);
        // 허용할 출처 패턴 설정 -> 이전 버전으로 setAllowCredentials과 사용 가능
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // 클라이언트에 노출할 헤더 설정
        configuration.addExposedHeader("*");
        // 지정한 HTTPMethod에 대한 통신 허용
        // "OPTIONS" : 프리플라이트 요청
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE" , "OPTION"));   // 지정한 HTTPMethod에 대한 통신 허용

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

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer);
            jwtAuthenticationFilter.setFilterProcessesUrl("/users/login");   // ⏹️  request URL 체크
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new UserAuthenticationSuccessHandler());
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new UserAuthenticationFailureHandler());

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils);

            builder
                    .addFilter(jwtAuthenticationFilter) // Spring Security Filter Chain에 추가
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);  // JwtAuthenticationFilter 뒤에 jwtVerificationFilter 보내겠다
        }
    }
}
