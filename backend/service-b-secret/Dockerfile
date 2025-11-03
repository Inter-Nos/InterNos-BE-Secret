# Multi-stage build for Service B Secret Room

# Build stage
FROM gradle:7.6.3-jdk17 AS build
WORKDIR /app

# Copy build configuration files first (for better layer caching)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (cached if build.gradle doesn't change)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds)
RUN gradle build --no-daemon -x test

# Verify JAR file was created
RUN find /app/build/libs -name "*.jar" -not -name "*plain.jar" -exec ls -lh {} \;

# Runtime stage - use distroless
FROM gcr.io/distroless/java17-debian11:nonroot
WORKDIR /app

# Copy built JAR (fat jar, exclude plain jar)
COPY --from=build /app/build/libs/service-b-secret-*.jar app.jar

# Use non-root user (provided by distroless)
USER nonroot:nonroot

# Expose port
EXPOSE 8081

# Java options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Note: Health check is handled externally (Kubernetes liveness/readiness probes)
# or via docker inspect using the /b/v1/health/liveness endpoint

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

