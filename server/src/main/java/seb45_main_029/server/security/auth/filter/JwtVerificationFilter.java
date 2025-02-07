package seb45_main_029.server.security.auth.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import seb45_main_029.server.exception.BusinessLogicException;
import seb45_main_029.server.exception.ExceptionCode;
import seb45_main_029.server.redis.common.RedisUtil;
import seb45_main_029.server.security.auth.jwt.JwtTokenizer;
import seb45_main_029.server.security.auth.utils.CustomAuthorityUtils;
import seb45_main_029.server.user.entity.User;
import seb45_main_029.server.user.service.UserService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
 * JWT 검증 및 인증 필터
 *
 * 요청당 한 번 실행
 * 인증 헤더의 JWT 토큰을 검증하고 유효한 경우 사용자의 권한을 추출
 * 보안 컨텍스트에 인증 정보 설정
 */
@AllArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final RedisUtil redisUtil;

    @Override

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // JWT 토큰 검증, 권한 정보 추출
            Map<String, Object> claims = verifyJws(request);
            // 추출 권한 정보를 보안 컨텍스트에 인증 정보로 설정
            setAuthenticationToContext(claims);
        } catch (SignatureException se) {
            request.setAttribute("exception", se);
        } catch (ExpiredJwtException ee) {
            request.setAttribute("exception", ee);
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        // 요청 헤더에 Authorization 헤더가 있는지 확인하하고 값이 Bearer 로 시작하는지 확인
        String authorization = request.getHeader("Authorization");
        return authorization == null || !authorization.startsWith("Bearer");
    }

    /*
     * JWT 토큰 검증, 권한 정보 추출
     */
    private Map<String, Object> verifyJws(HttpServletRequest request) throws Exception {

        // 헤더 Authorization 부분 불러와서 "Bearer "부분 지우기
        String jws = request.getHeader("Authorization").replace("Bearer ", "");

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();
        String email = claims.get("username").toString();
        String accessToken = redisUtil.get("AccessToken : "+email).toString();

//        redis 에 저장되어있는 액세스토큰과 일치하면 반환
        if (accessToken.equals(jws)) {
            return claims;
        } else throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_USER);
    }

    private void setAuthenticationToContext(Map<String, Object> claims) {

        // 사용자명, 역할 정보를 추출
        String username = (String) claims.get("username");

        // 사용자명과 역할 정보 추출
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((List<String>) claims.get("roles"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
