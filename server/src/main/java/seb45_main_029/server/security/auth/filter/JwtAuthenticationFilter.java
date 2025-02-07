package seb45_main_029.server.security.auth.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import seb45_main_029.server.redis.common.RedisUtil;
import seb45_main_029.server.security.auth.dto.UserLoginDto;
import seb45_main_029.server.security.auth.dto.UserLoginResponseDto;
import seb45_main_029.server.security.auth.jwt.JwtTokenizer;
import seb45_main_029.server.user.entity.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * userName & Password 기반의 인증처리를 위해
 * UsernamePasswordAuthenticationFilter 확장
 */
@AllArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // userName & Password 인증 여부 판단
    private final AuthenticationManager authenticationManager;

    // 클라이언트가 성공할 경우 JWT 생성
    private final JwtTokenizer jwtTokenizer;
    private final RedisUtil redisUtil;

    /*
     * 메서드 내부에서 인증을 시도하는 로직 구현
     * objectMapper : userName & Password 를 DTO로 역직렬화 하기 위해 인스턴스 생성
     * authenticationToken : userName & Password 포함한 Token 생성
     * UsernamePasswordAuthenticationToken 을 AuthenticationManager 에게 전달
     */
    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) {

        ObjectMapper objectMapper = new ObjectMapper();
        UserLoginDto loginDto = objectMapper.readValue(request.getInputStream(), UserLoginDto.class);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        return authenticationManager.authenticate(authenticationToken);
    }

    /**
     * 클라이언트의 인증 정보를 이용해 인증에 성공할 경우 호출
     * delegateAccessToken(user) : Access Token 생성
     * delegateRefreshToken(user) : Refresh Token 생성
     *
     * @param request
     * @param response
     * @param chain
     * @param authResult the object returned from the <tt>attemptAuthentication</tt>
     *                   method.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        User user = (User) authResult.getPrincipal();
        Gson gson = new Gson();

        String accessToken = delegateAccessToken(user);
        String refreshToken = delegateRefreshToken(user);

        //        인증 성공시 redis에 토큰 저장
        redisUtil.set("AccessToken : "+user.getEmail(),accessToken,20L);
        redisUtil.set("RefreshToken : "+user.getEmail(),refreshToken,10080L);

        // header에 AccessToken 전달
        response.setHeader("Authorization", "Bearer " + accessToken);

        // header에 RefreshToken 전달
        response.setHeader("RefreshToken", refreshToken);

        UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto(user.getUserId(), user.getEmail(), "Bearer "+accessToken);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(gson.toJson(userLoginResponseDto));


        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }

    /*
     * Access Token 생성 메서드
     */
   private String delegateAccessToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("username", user.getEmail());
        claims.put("roles", user.getRoles());
        claims.put("userId", user.getUserId());

        String subject = user.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return accessToken;
    }

    /*
     * Refresh Token 생성 메서드
     */
    private String delegateRefreshToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("username", user.getEmail());
        claims.put("roles", user.getRoles());
        claims.put("userId", user.getUserId());

        String subject = user.getEmail();

        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String refreshToken = jwtTokenizer.generateRefreshToken(claims,subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }
}
