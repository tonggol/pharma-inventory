package com.pharma.inventory.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 요청 DTO
 * 만료된 액세스 토큰을 새로운 토큰으로 갱신할 때 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {

    /**
     * 리프레시 토큰
     * 이전 로그인 시 발급받은 리프레시 토큰
     */
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 만료된 액세스 토큰 (선택적)
     * 보안 검증을 위해 이전 액세스 토큰도 함께 전송할 수 있음
     * 일부 시스템에서는 추가 검증용으로 사용
     */
    @JsonProperty("expired_access_token")
    private String expiredAccessToken;

    /**
     * 클라이언트 식별자 (선택적)
     * 멀티 디바이스 환경에서 토큰 발급 디바이스 구분용
     */
    @JsonProperty("client_id")
    private String clientId;

    /**
     * 디바이스 정보 (선택적)
     * 토큰이 발급된 디바이스 추적용
     */
    @JsonProperty("device_info")
    private DeviceInfo deviceInfo;

    /**
     * 디바이스 정보 내부 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        private String deviceType;  // WEB, MOBILE, TABLET
        private String deviceName;  // Chrome, Safari, Android App 등
        private String ipAddress;
        private String userAgent;
    }

    /**
     * 간단한 토큰 갱신 요청 생성
     */
    public static TokenRefreshRequest of(String refreshToken) {
        return TokenRefreshRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 디바이스 정보를 포함한 토큰 갱신 요청 생성
     */
    public static TokenRefreshRequest withDevice(String refreshToken, String clientId, DeviceInfo deviceInfo) {
        return TokenRefreshRequest.builder()
                .refreshToken(refreshToken)
                .clientId(clientId)
                .deviceInfo(deviceInfo)
                .build();
    }
}