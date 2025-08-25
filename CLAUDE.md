# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요
중소 제약회사를 위한 의약품 재고관리 시스템입니다. Spring Boot 기반 웹 애플리케이션으로 Thymeleaf 템플릿과 Bootstrap UI를 사용하며, 일본 IT 취업을 위한 포트폴리오 프로젝트입니다.

## 개발 명령어

### 빌드 및 실행
- **애플리케이션 실행**: `./gradlew bootRun` 또는 `gradlew.bat bootRun` (윈도우)
- **프로젝트 빌드**: `./gradlew build`
- **테스트 실행**: `./gradlew test`
- **클린 빌드**: `./gradlew clean build`

### 데이터베이스 접속
- **H2 콘솔**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 사용자명: `sa`
  - 비밀번호: (공백)

### API 문서
- **Swagger UI**: http://localhost:8080/swagger-ui.html (실행 중일 때)

## 아키텍처

### 핵심 계층
- **Entity**: Lombok 어노테이션을 사용한 JPA 엔티티
- **Repository**: 데이터 접근을 위한 Spring Data JPA 리포지토리
- **Service**: 트랜잭션 관리가 포함된 비즈니스 로직 계층
- **DTO**: `dto/request`와 `dto/response` 하위의 별도 요청/응답 객체
- **Config**: 보안, 데이터 초기화, 웹 설정

### 주요 엔티티
1. **Medicine**: 의약품명, 제조사, 제형이 포함된 의약품 마스터 데이터
2. **Stock**: 유효기간이 포함된 로트번호별 재고 추적
3. **StockTransaction**: 입출고 거래 이력
4. **User**: 역할 기반 접근 권한을 가진 시스템 사용자

### 보안 설정
- 커스텀 SecurityConfig를 사용하는 Spring Security
- 기본 관리자 계정: admin/admin123
- 역할 기반 권한 부여 구현

### 비즈니스 로직
- 저재고 임계값: 10개 (application.properties에서 설정 가능)
- 만료 경고: 만료 30일 전
- 로트번호 기반 재고 추적
- 거래 감사 추적

## 기술 스택
- Java 17, Spring Boot 3.2.0
- Spring Data JPA with H2 (개발) / MySQL (운영)
- Spring Security 인증
- Thymeleaf + Bootstrap 5 UI
- SpringDoc OpenAPI API 문서화
- Apache POI Excel 내보내기 기능

## 개발 참고사항
- 한국어 주석 및 문서화 사용 (일본 시장 포트폴리오용)
- H2 데이터베이스는 재시작할 때마다 재생성됨 (create-drop)
- Actuator 엔드포인트 활성화: /actuator/health, /actuator/info, /actuator/metrics
- 애플리케이션 패키지에 대한 디버그 로깅 활성화
- 파일 업로드 최대 크기: 10MB