# Multi-stage Dockerfile for Spring Boot application

# Build stage
FROM maven:3.9.4-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy POM files for dependency caching
COPY pom.xml .
COPY shared-kernel/pom.xml shared-kernel/
COPY manufacturing-order/pom.xml manufacturing-order/
COPY customer-order/pom.xml customer-order/
COPY application/pom.xml application/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY shared-kernel/src shared-kernel/src/
COPY manufacturing-order/src manufacturing-order/src/
COPY customer-order/src customer-order/src/
COPY application/src application/src/

# Build application (skip tests in Docker build)
RUN mvn clean package -DskipTests -B

# Development stage (for development with docker-compose.override.yml)
FROM eclipse-temurin:21-jdk-alpine AS development

WORKDIR /app

# Install curl for health checks and debugging tools
RUN apk add --no-cache curl bash

# Copy built JAR from builder
COPY --from=builder /build/application/target/*.jar app.jar

# Create non-root user
RUN addgroup -g 1001 -S appuser && \
    adduser -S appuser -u 1001 -G appuser

# Create log directory
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app/logs

USER appuser

# JVM arguments for development
ENV JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005 -Xmx256m -Xms128m"

EXPOSE 8080 8081 5005

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Production stage
FROM eclipse-temurin:21-jre-alpine AS production

WORKDIR /app

# Install curl for health checks only
RUN apk add --no-cache curl && \
    rm -rf /var/cache/apk/*

# Copy built JAR from builder
COPY --from=builder /build/application/target/*.jar app.jar

# Create non-root user
RUN addgroup -g 1001 -S appuser && \
    adduser -S appuser -u 1001 -G appuser

# Create log directory
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app/logs

USER appuser

# Production JVM arguments
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"

EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Default to production stage
FROM production