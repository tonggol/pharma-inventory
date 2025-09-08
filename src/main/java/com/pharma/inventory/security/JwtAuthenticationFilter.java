package com.pharma.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 모든 요청에 대해 JWT 토큰을 검증하고 인증 정보를 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 요청에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);

            // 토큰이 존재하고 유효한 경우
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // 토큰에서 사용자명 추출
                String username = jwtTokenProvider.getUsernameFromToken(token);

                // SecurityContext에 인증 정보가 없는 경우에만 처리
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 사용자 정보 로드
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 토큰과 사용자 정보 재검증
                    if (jwtTokenProvider.validateToken(token, userDetails)) {
                        // 인증 객체 생성
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // 요청 세부 정보 설정
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // SecurityContext에 인증 정보 설정
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT 인증 성공 - 사용자: {}, URI: {}", username, request.getRequestURI());
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            // 예외가 발생해도 필터 체인은 계속 진행되어, SecurityConfig의 예외 처리 로직이 담당하게 됨
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 쿼리 파라미터에서도 토큰 확인 (선택적, WebSocket 등에서 사용)
        String paramToken = request.getParameter("token");
        if (StringUtils.hasText(paramToken)) {
            log.debug("쿼리 파라미터에서 토큰 추출");
            return paramToken;
        }

        return null;
    }
}
