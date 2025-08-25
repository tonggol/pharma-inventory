# 제약 재고관리 시스템 (Pharmaceutical Inventory Management System)

## 🎯 프로젝트 개요

중소 제약회사를 위한 웹 기반 의약품 재고관리 시스템입니다. 의약품의 입출고 관리, 유효기간 추적, 재고 현황 모니터링 등 제약 산업에 특화된 재고관리 기능을 제공합니다.

## 🚀 주요 기능

### 핵심 기능
- 📦 **재고 관리**: 의약품별, 로트번호별 재고 추적
- 📊 **입출고 관리**: 입고/출고 트랜잭션 기록 및 이력 관리
- ⏰ **유효기간 관리**: 만료 예정 의약품 자동 알림
- 🔔 **재고 알림**: 최소 재고 수준 이하 시 자동 경고
- 👥 **사용자 관리**: 역할 기반 접근 제어 (RBAC)
- 📈 **리포트**: 재고 현황, 입출고 통계 리포트 생성
- 📱 **반응형 UI**: 모바일/태블릿 지원

### 보안 기능
- JWT 기반 인증
- Spring Security를 통한 권한 관리
- API 접근 제어

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.4.0
- **Language**: Java 21
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA / Hibernate
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Gradle 8.13

### Frontend
- **React Version**: React 18.2 + TypeScript
- **UI Framework**: Material-UI (MUI) v5
- **State Management**: Redux Toolkit
- **Data Fetching**: React Query + Axios
- **Charts**: Chart.js, Recharts
- **Form Handling**: React Hook Form
- **Routing**: React Router v6

### Database
- **Development**: H2 In-Memory Database
- **Production Ready**: MySQL 8.0+ / PostgreSQL 14+

## 📁 프로젝트 구조

```
pharma-inventory/
├── 📂 src/main/java/com/pharma/inventory/
│   ├── 📂 config/              # 설정 클래스 (Security, CORS, etc.)
│   ├── 📂 controller/           # REST API 컨트롤러
│   ├── 📂 dto/                  # 데이터 전송 객체
│   ├── 📂 entity/               # JPA 엔티티
│   │   ├── Medicine.java        # 의약품 정보
│   │   ├── Stock.java          # 재고 현황
│   │   ├── StockTransaction.java # 입출고 이력
│   │   └── User.java           # 사용자 정보
│   ├── 📂 repository/           # JPA 리포지토리
│   ├── 📂 security/             # 보안 관련 (JWT, 인증)
│   ├── 📂 service/              # 비즈니스 로직
│   └── PharmaceuticalInventoryApplication.java
│
├── 📂 src/main/resources/
│   └── application.properties   # 애플리케이션 설정
│
├── 📂 frontend/                 # React 프론트엔드
│   ├── 📂 public/
│   ├── 📂 src/
│   │   ├── 📂 components/      # React 컴포넌트
│   │   │   └── 📂 Layout/      # 레이아웃 컴포넌트
│   │   ├── App.tsx             # 메인 앱 컴포넌트
│   │   └── index.tsx           # 엔트리 포인트
│   ├── package.json            # npm 의존성
│   └── tsconfig.json           # TypeScript 설정
│
├── build.gradle                # Gradle 빌드 설정
├── settings.gradle             # Gradle 프로젝트 설정
└── README.md
```

## 🔧 설치 및 실행

### 사전 요구사항
- JDK 21 이상
- Node.js 18.x 이상
- Gradle 8.x (또는 Gradle Wrapper 사용)
- Git

### 1. 프로젝트 클론
```bash
git clone [repository-url]
cd pharma-inventory
```

### 2. 백엔드 실행

#### H2 데이터베이스 사용 (기본 설정)
```bash
# Gradle Wrapper 사용
./gradlew bootRun

# 또는 Gradle 직접 사용
gradle bootRun
```

#### PostgreSQL 사용 시
1. PostgreSQL 설치 및 데이터베이스 생성
```sql
CREATE DATABASE pharma_inventory;
CREATE USER pharma_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE pharma_inventory TO pharma_user;
```

2. `application.properties` 수정 (아래 PostgreSQL 설정 섹션 참조)

3. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. 프론트엔드 실행
```bash
cd frontend
npm install
npm start
```

## ⚙️ 데이터베이스 설정

### H2 (기본 - 개발용)
```properties
# 현재 설정 (application.properties)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
```

### PostgreSQL 설정 (운영 환경 권장)
```properties
# PostgreSQL 설정으로 변경
spring.datasource.url=jdbc:postgresql://localhost:5432/pharma_inventory
spring.datasource.username=pharma_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA 설정
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# H2 콘솔 비활성화
spring.h2.console.enabled=false
```

build.gradle에 PostgreSQL 의존성 추가:
```gradle
dependencies {
    // ... 기존 의존성
    implementation 'org.postgresql:postgresql:42.6.0'
}
```

### MySQL 설정 (대안)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pharma_inventory?useSSL=false&serverTimezone=UTC
spring.datasource.username=pharma_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## 🌐 접속 정보

### 애플리케이션
- **백엔드 API**: http://localhost:8080
- **프론트엔드**: http://localhost:3000
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console** (개발 모드): http://localhost:8080/h2-console

### 기본 계정
- **Username**: admin
- **Password**: admin123

## 📊 주요 엔티티 구조

### Medicine (의약품)
- 의약품 코드, 이름, 제조사
- 카테고리, 단위, 설명
- 최소 재고 수준

### Stock (재고)
- 의약품 참조
- 로트 번호
- 현재 수량
- 제조일, 유효기간

### StockTransaction (입출고 이력)
- 트랜잭션 타입 (입고/출고)
- 수량, 날짜
- 담당자, 비고

### User (사용자)
- 사용자 정보
- 권한 및 역할
- JWT 토큰 관리

## 🔒 보안 설정

### JWT 설정
```properties
jwt.secret=your-secret-key-here
jwt.access-token-validity-seconds=3600
jwt.refresh-token-validity-seconds=86400
```

### CORS 설정
프론트엔드 개발 서버(3000포트)와의 통신을 위해 CORS 설정이 필요합니다.

## 📈 모니터링

### Actuator 엔드포인트
- Health Check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`

## 🧪 테스트

### 백엔드 테스트
```bash
./gradlew test
```

### 프론트엔드 테스트
```bash
cd frontend
npm test
```

## 📦 빌드 및 배포

### 프로덕션 빌드
```bash
# 백엔드 JAR 생성
./gradlew build

# 프론트엔드 빌드
cd frontend
npm run build
```

### Docker 지원 (예정)
Docker 컨테이너화를 통한 배포 지원 예정

## 🤝 기여 방법

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

이 프로젝트는 포트폴리오 목적으로 개발되었습니다.

## 👨‍💻 개발자

- **이름**: beom
- **목적**: 일본 IT 취업 포트폴리오
- **연락처**: [이메일 또는 GitHub 프로필]

## 🔜 향후 계획

- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인 구축
- [ ] 다국어 지원 (한국어, 일본어, 영어)
- [ ] 바코드/QR코드 스캔 기능
- [ ] 모바일 앱 개발
- [ ] 재고 예측 AI 기능
- [ ] 거래처 관리 모듈
- [ ] 회계 시스템 연동

## 🐛 알려진 이슈

- 현재 H2 인메모리 DB 사용으로 재시작 시 데이터 초기화
- PostgreSQL/MySQL 마이그레이션 스크립트 필요

## 📚 참고 문서

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [Material-UI Documentation](https://mui.com/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Version**: 1.0.0  
**Last Updated**: 2024
