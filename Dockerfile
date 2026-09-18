# =========================================================================
# DevOpsPulse - Dockerfile
# Build the JAR locally with Gradle, then package it into a minimal JRE image
# =========================================================================

FROM eclipse-temurin:21-jre-alpine

# Install curl for HEALTHCHECK
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S devopspulse && adduser -S devopspulse -G devopspulse

WORKDIR /app

# Copy the already-built Spring Boot JAR
COPY build/libs/devopspulse.jar /app/devopspulse.jar

# Give application user ownership
RUN chown -R devopspulse:devopspulse /app

USER devopspulse

# Spring Boot is configured to run on 8081
EXPOSE 8081

# Container health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/devopspulse.jar"]