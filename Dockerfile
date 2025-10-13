# Java 21 JRE를 기반으로 하는 공식 이미지를 사용합니다.
FROM eclipse-temurin:21-jre-jammy

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# 빌드된 JAR 파일을 컨테이너의 /app 디렉토리로 복사합니다.
# 'build/libs/*.jar'는 생성된 JAR 파일 이름과 일치해야 합니다.
COPY build/libs/*.jar app.jar

# 애플리케이션이 8080 포트에서 실행됨을 명시합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 JAR 파일을 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
