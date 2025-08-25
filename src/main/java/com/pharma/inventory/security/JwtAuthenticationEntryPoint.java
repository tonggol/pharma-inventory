package com.pharma.inventory.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharma.inventory.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * JWT 인증 실패 처리
 * 인증되지 않은 사용자의 접근 시 401 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.error("인증 실패 - URI: {}, 메시지: {}",
                request.getRequestURI(), authException.getMessage());

        // 에러 응답 생성
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode("AUTHENTICATION_REQUIRED")
                .errorType(ErrorResponse.ErrorType.AUTHENTICATION)
                .message("인증이 필요합니다")
                .detail(getDetailMessage(request, authException))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .suggestion("로그인 후 다시 시도해주세요")
                .build();

        // 응답 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // JSON 응답 작성
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 상세 에러 메시지 생성
     */
    private String getDetailMessage(HttpServletRequest request, AuthenticationException authException) {
        // 요청 속성에서 추가 정보 확인
        Object expiredToken = request.getAttribute("expired_token");
        if (expiredToken != null && (Boolean) expiredToken) {
            return "토큰이 만료되었습니다. 다시 로그인해주세요.";
        }

        Object invalidToken = request.getAttribute("invalid_token");
        if (invalidToken != null && (Boolean) invalidToken) {
            return "유효하지 않은 토큰입니다.";
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "Authorization 헤더가 없거나 올바르지 않습니다.";
        }

        return authException.getMessage();
    }
}