package com.backend.global.security.provider;

import com.backend.global.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import com.backend.global.security.util.SecurityUtil;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Getter
    private final long ACCESS_TOKEN_EXPIRE_TIME = 100000 * 60 * 1000L; // 100000분
    @Getter
    private final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일

    private Key key;

    // 생성
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // AccessToken, RefreshToken 생성
    public Map<String, String> generateToken(long id, String nickName, Authentication authentication) {
        // 로그인 정보 가져오기
        SecurityUser securityUser = new SecurityUser(
                id,
                (String) authentication.getPrincipal(),
                (String) authentication.getCredentials(),
                nickName
        );
        String authorities = securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenExpireIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities) // 넣고싶은 값 넣기
                .claim("id", id)
                .claim("email", securityUser.getEmail())
                .claim("nickname", nickName)
                .setExpiration(accessTokenExpireIn)
                .signWith(key, SignatureAlgorithm.HS256) // 원하는 방식
                .compact();

        log.info("access token : " + accessToken);

        // 리프레쉬 토큰
        Date refreshTokenExpireIn = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpireIn)
                .signWith(key, SignatureAlgorithm.HS256) // 원하는 방식
                .compact();

        HashMap<String, String> map = new HashMap<>();
        map.put("access", SecurityUtil.getTokenPrefix() + " " + accessToken);
        map.put("refresh", SecurityUtil.getTokenPrefix() + " " + refreshToken);
        return map;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        return claims.get("email", String.class);
    }
}


