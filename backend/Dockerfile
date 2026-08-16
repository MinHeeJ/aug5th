FROM maven:3.9.8-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17.0.10_7-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl \
    && addgroup -S commonfoundation \
    && adduser -S -G commonfoundation commonfoundation \
    && mkdir -p /var/lib/common-foundation/attachments \
    && chown -R commonfoundation:commonfoundation /app /var/lib/common-foundation
COPY --from=build --chown=commonfoundation:commonfoundation /workspace/target/*.jar /app/app.jar
EXPOSE 8080
USER commonfoundation
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 CMD curl -fsS http://localhost:8080/api/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
