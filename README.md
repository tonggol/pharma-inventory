# 제약 재고관리 시스템 (Pharmaceutical Inventory System)

## 📋 프로젝트 개요
중소 제약회사를 위한 웹 기반 의약품 재고관리 시스템입니다.

### 주요 기능
- 의약품 마스터 데이터 관리
- 로트번호별 재고 추적
- 입고/출고 트랜잭션 관리
- 유효기간 만료 알림
- 재고 부족 경고
- 사용자 권한 관리
- 재고 리포트 생성

## 🛠 기술 스택
- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: H2 (개발), MySQL (운영)
- **Frontend**: Thymeleaf, Bootstrap 5
- **Security**: Spring Security
- **Build Tool**: Gradle
- **Documentation**: SpringDoc OpenAPI (Swagger)

## 📁 프로젝트 구조
```
pharma-inventory/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/pharma/inventory/
│   │   │       ├── entity/          # JPA 엔티티
│   │   │       ├── repository/      # 데이터 접근 계층
│   │   │       ├── service/         # 비즈니스 로직
│   │   │       ├── controller/      # 웹 컨트롤러
│   │   │       ├── dto/            # 데이터 전송 객체
│   │   │       ├── config/         # 설정 클래스
│   │   │       └── exception/      # 예외 처리
│   │   └── resources/
│   │       ├── templates/          # Thymeleaf 템플릿
│   │       ├── static/             # 정적 리소스
│   │       └── application.properties
│   └── test/
├── build.gradle
└── README.md
```

## 🚀 시작하기

### 필요 사항
- JDK 17 이상
- Gradle 7.0 이상

### 실행 방법
1. 프로젝트 클론
```bash
git clone [repository-url]
cd pharma-inventory
```

2. 애플리케이션 실행
```bash
./gradlew bootRun
```

3. 브라우저에서 접속
```
http://localhost:8080
```

### H2 Console 접속 (개발용)
```
http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: (비워두기)
```

## 📊 주요 엔티티
1. **Medicine** - 의약품 정보
2. **Stock** - 재고 현황 (로트번호별)
3. **StockTransaction** - 입출고 이력
4. **User** - 시스템 사용자

## 🔐 기본 계정
- Username: admin
- Password: admin123

## 📝 API 문서
애플리케이션 실행 후:
```
http://localhost:8080/swagger-ui.html
```

## 👤 개발자
- 이름: beom
- 목적: 일본 IT 취업 포트폴리오

## 📄 라이센스
This project is for portfolio purposes.
