# Multi-stage build for Intel/AMD64 architecture
# Stage 1: Build stage
FROM --platform=linux/amd64 gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy gradle files first for caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application
RUN gradle clean bootJar --no-daemon

# Stage 2: Runtime stage
FROM --platform=linux/amd64 ubuntu:22.04

# Install Java 17 and Tesseract OCR
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-17-jre-headless \
    tesseract-ocr \
    tesseract-ocr-eng \
    tesseract-ocr-kor \
    libtesseract-dev \
    libleptonica-dev \
    wget \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create data directory
RUN mkdir -p /app/data

# Environment variables
ENV JAVA_OPTS="-Xms512m -Xmx2048m" \
    SPRING_PROFILES_ACTIVE="prod" \
    TESSERACT_LIB_PATH="/usr/lib/x86_64-linux-gnu" \
    TESSDATA_PATH="/usr/share/tesseract-ocr/4.00/tessdata"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/datahub/board/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
