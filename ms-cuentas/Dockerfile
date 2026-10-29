# Build context: the repository root (docker compose already sets it).
#   docker build -f ms-cuentas/Dockerfile -t ms-cuentas .

# ---- Stage 1: build the executable jar with the Gradle wrapper ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Build scripts first, so the dependency download is cached until they change
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY ms-clientes/build.gradle.kts ms-clientes/
COPY ms-cuentas/build.gradle.kts ms-cuentas/
RUN chmod +x gradlew && ./gradlew --no-daemon :ms-cuentas:dependencies --configuration runtimeClasspath > /dev/null

COPY ms-cuentas/src ms-cuentas/src
RUN ./gradlew --no-daemon :ms-cuentas:bootJar -x test

# ---- Stage 2: minimal runtime image (JRE only, non-root user) ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/ms-cuentas/build/libs/app.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
