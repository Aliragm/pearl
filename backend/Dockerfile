# Multi-stage Dockerfile for Pearl.gg Backend (Ktor)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Install bash and dos2unix to ensure compatibility and normalize line endings
RUN apk add --no-cache bash dos2unix

# Copy Gradle wrapper and project configuration
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY backend/build.gradle.kts ./backend/

# Normalize line endings (fixes CRLF issues from Windows checkouts) and set permissions
RUN dos2unix gradlew && chmod +x gradlew

# Download backend dependencies
RUN ./gradlew :backend:dependencies --no-daemon

# Copy backend source code
COPY backend/src ./backend/src

# Build backend distribution without running unit tests
RUN ./gradlew :backend:installDist --no-daemon -x test

# Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/backend/build/install/backend /app

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["/app/bin/backend"]
