package com.pharma.inventory.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT 토큰 제공자
 * JWT 토큰의 생성, 검증, 파싱을 담당
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-seconds:3600}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-seconds:86400}") long refreshTokenValidityInSeconds,
            @Value("${jwt.issuer:PharmaInventory}") String issuer) {

        // Base64로 인코딩된 시크릿 키를 디코딩하여 SecretKey 생성
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
        this.issuer = issuer;

        log.info("JWT Provider 초기화 완료 - 발급자: {}", issuer);
    }

    /**
     * 액세스 토큰 생성
     */
    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenValidityInMilliseconds, "ACCESS");
    }

    /**
     * 리프레시 토큰 생성
     */
    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, refreshTokenValidityInMilliseconds, "REFRESH");
    }

    /**
     * 토큰 생성 (공통)
     */
    private String createToken(Authentication authentication, long validityInMilliseconds, String tokenType) {
        String username = authentication.getName();

        // 권한 정보 추출
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        // 추가 클레임
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType);
        claims.put("authorities", authorities);

        // UserDetails에서 추가 정보 추출 (선택적)
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            claims.put("enabled", userDetails.isEnabled());
        }

        return Jwts.builder()
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .setIssuer(issuer)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * 토큰에서 권한 정보 추출
     */
    public String getAuthoritiesFromToken(String token) {
        Claims claims = getClaims(token);
        return (String) claims.get("authorities");
    }

    /**
     * 토큰 타입 추출
     */
    public String getTokenType(String token) {
        Claims claims = getClaims(token);
        return (String) claims.get("type");
    }

    /**
     * 토큰 만료 시간 추출
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration();
    }

    /**
     * 토큰 발급 시간 추출
     */
    public Date getIssuedAtFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getIssuedAt();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);

            // 만료 시간 확인
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.debug("토큰이 만료되었습니다");
                return false;
            }

            // 발급자 확인
            String tokenIssuer = claims.getIssuer();
            if (!issuer.equals(tokenIssuer)) {
                log.debug("잘못된 발급자입니다. 예상: {}, 실제: {}", issuer, tokenIssuer);
                return false;
            }

            return true;

        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 오류 발생: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 토큰 유효성 검증 (사용자명 포함)
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && validateToken(token));
    }

    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 토큰 남은 유효 시간 (초)
     */
    public long getTokenValidityInSeconds(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            long now = System.currentTimeMillis();
            long expirationTime = expiration.getTime();

            if (expirationTime > now) {
                return (expirationTime - now) / 1000;
            }
            return 0;

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 토큰 갱신 가능 여부 확인
     * 리프레시 토큰만 갱신 가능
     */
    public boolean canTokenBeRefreshed(String token) {
        try {
            String tokenType = getTokenType(token);
            return "REFRESH".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Claims 추출 (내부 사용)
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰에서 Claims 안전하게 추출
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return getClaims(token);
        } catch (Exception e) {
            log.error("토큰에서 Claims 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}