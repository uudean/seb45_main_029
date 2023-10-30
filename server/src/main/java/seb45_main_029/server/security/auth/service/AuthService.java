package seb45_main_029.server.security.auth.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import seb45_main_029.server.security.auth.dto.UserLoginResponseDto;
import seb45_main_029.server.security.auth.jwt.JwtTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtTokenizer jwtTokenizer;
    private final RedisTemplate<String, String> redisTemplate;

    //    로그인

    //    토큰 재발급
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

//       요청에 포함된 header 에서 refreshToken 가져옴
        String jws = request.getHeader("Authorization");
//       가져온 토큰을 이용해 claims 얻음
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();
//       claims 에 포함된 정보 가져옴
        String email = claims.get("username").toString();
        long userId = Long.parseLong(claims.get("userId").toString());

        String refreshTokenInRedis = redisTemplate.opsForValue().get("RefreshToken : " + email);

        String subject = email;
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);
        if (jws.equals(refreshTokenInRedis)) {
            redisTemplate.delete("AccessToken : " + email);
            redisTemplate.opsForValue().set("AccessToken : " + email, accessToken, 20L, TimeUnit.MINUTES);

            response.setHeader("Authorization", "Bearer " + accessToken);

            Gson gson = new Gson();
            UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto(userId, email, "Bearer " + accessToken);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(gson.toJson(userLoginResponseDto));
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
