package com.pharma.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JWT 토큰 응답 DTO
 * 로그인 성공 시 클라이언트에게 전달되는 인증 토큰 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /**
     * 액세스 토큰
     * API 요청 시 Authorization 헤더에 포함되는 토큰
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 리프레시 토큰
     * 액세스 토큰 만료 시 새로운 액세스 토큰 발급에 사용
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 토큰 타입
     * 일반적으로 "Bearer" 사용
     */
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 액세스 토큰 만료 시간 (초 단위)
     * 예: 3600 = 1시간
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * 리프레시 토큰 만료 시간 (초 단위)
     * 일반적으로 액세스 토큰보다 긴 기간 설정
     */
    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;

    /**
     * 토큰 발급 시간
     */
    @JsonProperty("issued_at")
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    /**
     * 사용자 정보 (선택적)
     * 토큰과 함께 기본 사용자 정보를 전달할 때 사용
     */
    @JsonProperty("user_info")
    private UserInfo userInfo;

    /**
     * 토큰에 포함된 사용자 기본 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String fullName;
        private String email;
        private String role;
        private String department;
    }

    /**
     * 간단한 토큰 응답 생성 (사용자 정보 제외)
     */
    public static TokenResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .refreshExpiresIn(expiresIn * 24) // 리프레시 토큰은 24배 더 긴 기간
                .build();
    }

    /**
     * 전체 토큰 응답 생성 (사용자 정보 포함)
     */
    public static TokenResponse withUserInfo(String accessToken, String refreshToken,
                                             Long expiresIn, UserInfo userInfo) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .refreshExpiresIn(expiresIn * 24)
                .userInfo(userInfo)
                .build();
    }
}