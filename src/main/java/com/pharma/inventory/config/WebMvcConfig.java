package com.pharma.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.format.Formatter;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring MVC 설정 클래스
 * - 뷰 리졸버 설정
 * - 정적 리소스 핸들링
 * - CORS 설정
 * - 날짜/시간 포맷터 등록
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 뷰 컨트롤러 설정
     * URL을 직접 뷰에 매핑 (템플릿이 생성되면 활성화)
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 기본 페이지를 대시보드로 리다이렉트
        registry.addViewController("/").setViewName("redirect:/dashboard");

        // 다음 템플릿 생성 후 주석 해제
        // registry.addViewController("/login").setViewName("auth/login");
        // registry.addViewController("/error/403").setViewName("error/403");
        // registry.addViewController("/error/404").setViewName("error/404");
        // registry.addViewController("/error/500").setViewName("error/500");
    }

    /**
     * 정적 리소스 핸들러 설정
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // CSS, JS, 이미지 등 정적 파일
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // 1시간 캐시

        // 파비콘
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/images/favicon.ico");

        // 업로드된 파일들 (의약품 이미지 등)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(86400); // 24시간 캐시
    }

    /**
     * CORS 설정
     * API 엔드포인트에 대한 교차 출처 요청 허용
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 인터셉터 설정
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로깅 인터셉터 (요청/응답 로그)
        registry.addInterceptor(new LoggingInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/favicon.ico", "/error/**");

        // API 요청 제한 인터셉터 (Rate Limiting)
        registry.addInterceptor(new RateLimitInterceptor())
                .addPathPatterns("/api/**");
    }

    /**
     * 커스텀 포맷터 등록
     * 날짜/시간 변환을 위한 포맷터
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // LocalDate 포맷터
        registry.addFormatter(new Formatter<LocalDate>() {
            @Override
            @NonNull
            public LocalDate parse(@NonNull String text, @NonNull Locale locale) {
                try {
                    return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid date format: " + text, e);
                }
            }

            @Override
            @NonNull
            public String print(@NonNull LocalDate object, @NonNull Locale locale) {
                return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(object);
            }
        });

        // LocalDateTime 포맷터
        registry.addFormatter(new Formatter<LocalDateTime>() {
            @Override
            @NonNull
            public LocalDateTime parse(@NonNull String text, @NonNull Locale locale) {
                try {
                    return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid datetime format: " + text, e);
                }
            }

            @Override
            @NonNull
            public String print(@NonNull LocalDateTime object, @NonNull Locale locale) {
                return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(object);
            }
        });
    }

    /**
     * 콘텐츠 협상 설정
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)  // URL 파라미터로 포맷 지정 비활성화
                .defaultContentType(org.springframework.http.MediaType.TEXT_HTML)
                .mediaType("html", org.springframework.http.MediaType.TEXT_HTML)
                .mediaType("json", org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("xml", org.springframework.http.MediaType.APPLICATION_XML);
    }

    /**
     * 기본 서블릿 핸들러 설정
     * 정적 리소스를 기본 서블릿으로 처리
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /**
     * 요청/응답 로깅 인터셉터
     */
    public static class LoggingInterceptor implements HandlerInterceptor {
        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoggingInterceptor.class);

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();

            String logMessage = String.format("[%s] %s", method, uri);
            if (queryString != null) {
                logMessage += "?" + queryString;
            }

            logger.info("Request: {}", logMessage);

            // 요청 시작 시간 저장
            request.setAttribute("startTime", System.currentTimeMillis());
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                    Object handler, Exception ex) {
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Response: {} {} ({}ms)",
                        response.getStatus(),
                        request.getRequestURI(),
                        duration);
            }

            if (ex != null) {
                logger.error("Request failed: {}", ex.getMessage());
            }
        }
    }

    /**
     * API 요청 제한 인터셉터
     * 간단한 Rate Limiting 구현
     */
    public static class RateLimitInterceptor implements HandlerInterceptor {
        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RateLimitInterceptor.class);

        // 실제 운영환경에서는 Redis 등 외부 저장소 사용 권장
        private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
        private final Map<String, Long> lastResetTime = new ConcurrentHashMap<>();

        private static final int MAX_REQUESTS_PER_MINUTE = 100;
        private static final long RESET_INTERVAL = 60000; // 1분

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {

            String clientIP = getClientIP(request);
            long currentTime = System.currentTimeMillis();

            // 카운터 초기화 확인
            Long lastReset = lastResetTime.get(clientIP);
            if (lastReset == null || (currentTime - lastReset) > RESET_INTERVAL) {
                requestCounts.put(clientIP, new AtomicInteger(0));
                lastResetTime.put(clientIP, currentTime);
            }

            // 요청 수 증가
            AtomicInteger count = requestCounts.computeIfAbsent(clientIP, k -> new AtomicInteger(0));
            int currentCount = count.incrementAndGet();

            // 제한 확인
            if (currentCount > MAX_REQUESTS_PER_MINUTE) {
                logger.warn("Rate limit exceeded for IP: {} ({})", clientIP, currentCount);
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                return false;
            }

            // 헤더에 현재 요청 수 정보 추가
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE - currentCount)));

            return true;
        }

        private String getClientIP(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        }
    }
}