package com.pharma.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API 에러 응답 DTO
 * 모든 예외 상황에 대한 표준화된 에러 응답 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드는 JSON에서 제외
public class ErrorResponse {

    /**
     * 에러 추적 ID
     * 로그 추적 및 고객 지원 시 사용
     */
    @JsonProperty("trace_id")
    @Builder.Default
    private String traceId = UUID.randomUUID().toString();

    /**
     * HTTP 상태 코드
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * 에러 코드
     * 애플리케이션 내부에서 정의한 비즈니스 에러 코드
     * 예: MEDICINE_NOT_FOUND, STOCK_INSUFFICIENT
     */
    @JsonProperty("error_code")
    private String errorCode;

    /**
     * 에러 타입
     * 에러의 분류 (VALIDATION, BUSINESS, SYSTEM, SECURITY)
     */
    @JsonProperty("error_type")
    private ErrorType errorType;

    /**
     * 사용자 친화적 에러 메시지
     * 클라이언트에서 사용자에게 직접 표시 가능한 메시지
     */
    @JsonProperty("message")
    private String message;

    /**
     * 상세 에러 설명
     * 개발자를 위한 자세한 에러 정보
     */
    @JsonProperty("detail")
    private String detail;

    /**
     * 에러가 발생한 요청 경로
     */
    @JsonProperty("path")
    private String path;

    /**
     * 에러 발생 시각
     */
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 해결 방법 제안 (선택적)
     * 사용자가 문제를 해결할 수 있는 방법 안내
     */
    @JsonProperty("suggestion")
    private String suggestion;

    /**
     * 추가 메타데이터 (선택적)
     * 에러와 관련된 추가 정보
     */
    @JsonProperty("metadata")
    private Object metadata;

    /**
     * 에러 타입 열거형
     */
    public enum ErrorType {
        VALIDATION("입력값 검증 오류"),
        BUSINESS("비즈니스 로직 오류"),
        AUTHENTICATION("인증 오류"),
        AUTHORIZATION("권한 오류"),
        NOT_FOUND("리소스를 찾을 수 없음"),
        CONFLICT("충돌 발생"),
        SYSTEM("시스템 오류"),
        EXTERNAL("외부 시스템 오류");

        private final String description;

        ErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 간단한 에러 응답 생성
     */
    public static ErrorResponse of(HttpStatus status, String errorCode, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .errorCode(errorCode)
                .errorType(determineErrorType(status))
                .message(message)
                .build();
    }

    /**
     * 상세 에러 응답 생성
     */
    public static ErrorResponse of(HttpStatus status, String errorCode, String message, String detail, String path) {
        return ErrorResponse.builder()
                .status(status.value())
                .errorCode(errorCode)
                .errorType(determineErrorType(status))
                .message(message)
                .detail(detail)
                .path(path)
                .build();
    }

    /**
     * 비즈니스 에러 응답 생성
     */
    public static ErrorResponse businessError(String errorCode, String message, String suggestion) {
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(errorCode)
                .errorType(ErrorType.BUSINESS)
                .message(message)
                .suggestion(suggestion)
                .build();
    }

    /**
     * 인증 에러 응답 생성
     */
    public static ErrorResponse authenticationError(String message) {
        return ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode("AUTHENTICATION_FAILED")
                .errorType(ErrorType.AUTHENTICATION)
                .message(message)
                .suggestion("로그인 정보를 확인하고 다시 시도해주세요.")
                .build();
    }

    /**
     * 권한 에러 응답 생성
     */
    public static ErrorResponse authorizationError(String resource) {
        return ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .errorCode("ACCESS_DENIED")
                .errorType(ErrorType.AUTHORIZATION)
                .message(String.format("%s에 대한 접근 권한이 없습니다.", resource))
                .suggestion("필요한 권한이 있는지 관리자에게 문의하세요.")
                .build();
    }

    /**
     * Not Found 에러 응답 생성
     */
    public static ErrorResponse notFound(String resource, Object id) {
        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode(resource.toUpperCase() + "_NOT_FOUND")
                .errorType(ErrorType.NOT_FOUND)
                .message(String.format("%s를 찾을 수 없습니다. (ID: %s)", resource, id))
                .build();
    }

    /**
     * HTTP 상태 코드로부터 에러 타입 결정
     */
    private static ErrorType determineErrorType(HttpStatus status) {
        if (status.is4xxClientError()) {
            switch (status) {
                case UNAUTHORIZED:
                    return ErrorType.AUTHENTICATION;
                case FORBIDDEN:
                    return ErrorType.AUTHORIZATION;
                case NOT_FOUND:
                    return ErrorType.NOT_FOUND;
                case CONFLICT:
                    return ErrorType.CONFLICT;
                case BAD_REQUEST:
                    return ErrorType.VALIDATION;
                default:
                    return ErrorType.BUSINESS;
            }
        } else if (status.is5xxServerError()) {
            return ErrorType.SYSTEM;
        }
        return ErrorType.SYSTEM;
    }
}