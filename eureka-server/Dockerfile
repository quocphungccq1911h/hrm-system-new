# GIAI ĐOẠN 1: BUILDER - Biên dịch ứng dụng Spring Boot
FROM maven:3.9.11-amazoncorretto-17 AS builder

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Copy tệp pom.xml để tải dependencies trước
# Giúp tận dụng cache của Docker nếu pom.xml không thay đổi
COPY pom.xml .

# Tải dependencies (chạy một lệnh mvn rỗng)
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn
COPY src ./src

# Đóng gói ứng dụng thành tệp JAR
RUN mvn clean package -DskipTests

# GIAI ĐOẠN 2: FINAL - Tạo Image nhẹ để chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine

# Thiết lập đối số (Tên tệp JAR đã được tạo)
ARG JAR_FILE=target/*.jar

# Sao chép tệp JAR từ giai đoạn builder
# Tên tệp JAR sẽ là tên artifactId của bạn, ví dụ: eureka-server-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/${JAR_FILE} app.jar

# Khai báo cổng mà ứng dụng sẽ chạy (Cổng 8761)
EXPOSE 8761

# Lệnh khởi động ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]