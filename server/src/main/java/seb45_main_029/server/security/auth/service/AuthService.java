package seb45_main_029.server.security.auth.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import seb45_main_029.server.redis.common.RedisUtil;
import seb45_main_029.server.security.auth.dto.UserLoginResponseDto;
import seb45_main_029.server.security.auth.jwt.JwtTokenizer;
import seb45_main_029.server.user.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtTokenizer jwtTokenizer;
    private final RedisUtil redisUtil;
    private final UserService userService;

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
//      레디스에 저장되어있는 리프레시 토큰값 가져옴
        String refreshTokenInRedis = redisUtil.get("RefreshToken : " + email).toString();

        String subject = email;
//        토큰만료시간
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);
        if (jws.equals(refreshTokenInRedis)) {
            redisUtil.delete("AccessToken : " + email);
            redisUtil.set("AccessToken : " + email, accessToken, 20L);

            response.setHeader("Authorization", "Bearer " + accessToken);

            Gson gson = new Gson();
            UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto(userId, email, "Bearer " + accessToken);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(gson.toJson(userLoginResponseDto));
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //    로그아웃
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // 요청 헤더로부터 액세스 토큰 가져옴
        String jws = request.getHeader("Authorization").replace("Bearer ", "");
//        로그인한 유저의 이메일 확인
        String email = userService.getLoginUser().getEmail();
//        해당 이메일 주소를 키로 갖고 있는 리프레시토큰 삭제
        redisUtil.delete("RefreshToken : " + email);
//        기존 액세스토큰 블랙리스트 등록
        redisUtil.setBlackList("AccessToken : " + email, jws, 20L);

        return new ResponseEntity<>("로그아웃 되었습니다.", HttpStatus.OK);
    }
}
