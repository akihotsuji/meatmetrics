# Spring Boot用Dockerfile (最適化版)
FROM maven:latest AS build

WORKDIR /app

# 依存関係のキャッシュ
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ソースコードのコピーとビルド
COPY src ./src
RUN mvn clean package -DskipTests -B

# 実行環境
FROM openjdk:17 AS runtime

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# JVM設定の最適化
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
