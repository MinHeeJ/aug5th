FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17.0.12_7-jre-alpine AS runtime

WORKDIR /app
RUN apk add --no-cache wget && addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/*.jar /app/app.jar
USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=12 CMD wget -qO- http://127.0.0.1:8080/api/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
