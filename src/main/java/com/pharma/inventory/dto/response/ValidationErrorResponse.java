package com.pharma.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 입력값 검증 에러 응답 DTO
 * @Valid 검증 실패 시 필드별 상세 에러 정보 제공
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {

    /**
     * 에러 추적 ID
     */
    @JsonProperty("trace_id")
    @Builder.Default
    private String traceId = UUID.randomUUID().toString();

    /**
     * HTTP 상태 코드 (일반적으로 400)
     */
    @JsonProperty("status")
    @Builder.Default
    private Integer status = HttpStatus.BAD_REQUEST.value();

    /**
     * 에러 코드
     */
    @JsonProperty("error_code")
    @Builder.Default
    private String errorCode = "VALIDATION_FAILED";

    /**
     * 전체 에러 메시지
     */
    @JsonProperty("message")
    @Builder.Default
    private String message = "입력값 검증에 실패했습니다.";

    /**
     * 필드별 에러 정보
     * key: 필드명, value: 에러 메시지 리스트
     */
    @JsonProperty("field_errors")
    @Builder.Default
    private Map<String, List<String>> fieldErrors = new HashMap<>();

    /**
     * 전역 에러 메시지 (특정 필드와 관련 없는 에러)
     */
    @JsonProperty("global_errors")
    @Builder.Default
    private List<String> globalErrors = new ArrayList<>();

    /**
     * 검증 실패한 필드 개수
     */
    @JsonProperty("error_count")
    private Integer errorCount;

    /**
     * 요청 경로
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
     * 검증 규칙 정보 (디버깅용, 선택적)
     */
    @JsonProperty("validation_rules")
    private Map<String, ValidationRule> validationRules;

    /**
     * 검증 규칙 정보 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationRule {
        private String constraint;  // 검증 어노테이션 이름
        private Object expectedValue;  // 예상 값
        private Object actualValue;  // 실제 입력 값
        private String rule;  // 검증 규칙 설명
    }

    /**
     * 필드 에러 추가
     */
    public void addFieldError(String field, String message) {
        fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }

    /**
     * 전역 에러 추가
     */
    public void addGlobalError(String message) {
        globalErrors.add(message);
    }

    /**
     * Spring의 FieldError 리스트로부터 응답 생성
     */
    public static ValidationErrorResponse fromFieldErrors(List<FieldError> fieldErrors) {
        ValidationErrorResponse response = new ValidationErrorResponse();

        for (FieldError error : fieldErrors) {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();

            // 필드별로 에러 메시지 그룹화
            response.addFieldError(fieldName, errorMessage);

            // 검증 규칙 정보 추가 (선택적)
            if (response.getValidationRules() == null) {
                response.setValidationRules(new HashMap<>());
            }

            response.getValidationRules().put(fieldName,
                    ValidationRule.builder()
                            .constraint(error.getCode())
                            .actualValue(error.getRejectedValue())
                            .rule(errorMessage)
                            .build()
            );
        }

        response.setErrorCount(response.getFieldErrors().size());
        return response;
    }

    /**
     * Spring의 모든 에러(FieldError + ObjectError)로부터 응답 생성
     */
    public static ValidationErrorResponse fromAllErrors(List<ObjectError> errors) {
        ValidationErrorResponse response = new ValidationErrorResponse();

        for (ObjectError error : errors) {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                response.addFieldError(fieldError.getField(), error.getDefaultMessage());
            } else {
                // 객체 레벨 에러는 전역 에러로 처리
                response.addGlobalError(error.getDefaultMessage());
            }
        }

        response.setErrorCount(
                response.getFieldErrors().size() + response.getGlobalErrors().size()
        );
        return response;
    }

    /**
     * 단일 필드 에러 응답 생성
     */
    public static ValidationErrorResponse singleFieldError(String field, String message) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.addFieldError(field, message);
        response.setErrorCount(1);
        return response;
    }

    /**
     * 커스텀 메시지와 함께 응답 생성
     */
    public static ValidationErrorResponse withMessage(String message, Map<String, List<String>> fieldErrors) {
        return ValidationErrorResponse.builder()
                .message(message)
                .fieldErrors(fieldErrors)
                .errorCount(fieldErrors.size())
                .build();
    }

    /**
     * 모든 에러 메시지를 단일 문자열로 반환
     */
    public String getAllErrorMessages() {
        List<String> allMessages = new ArrayList<>();

        // 필드 에러 추가
        fieldErrors.forEach((field, messages) -> {
            messages.forEach(msg -> allMessages.add(field + ": " + msg));
        });

        // 전역 에러 추가
        allMessages.addAll(globalErrors);

        return String.join(", ", allMessages);
    }

    /**
     * 특정 필드의 첫 번째 에러 메시지 반환
     */
    public String getFirstFieldError(String field) {
        List<String> errors = fieldErrors.get(field);
        return (errors != null && !errors.isEmpty()) ? errors.get(0) : null;
    }
}