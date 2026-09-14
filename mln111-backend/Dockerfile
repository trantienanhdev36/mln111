# Bước 1: Build ứng dụng bằng Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# Bước 2: Tạo container gọn nhẹ để chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render sẽ tự cấp cổng ngẫu nhiên qua biến môi trường PORT
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
