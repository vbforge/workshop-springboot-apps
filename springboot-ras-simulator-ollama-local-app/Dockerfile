# ── Stage 1: Build ────────────────────────────────────────────────────────────
# JUNIOR NOTE — Multi-stage build:
# We use TWO FROM instructions. The first stage (builder) compiles the JAR.
# The second stage (runtime) only copies the compiled JAR — no Maven, no source code.
# Result: the final image is ~200MB instead of ~600MB.
# This is standard practice for production Java Docker images.

FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml first — Docker caches layers.
# If pom.xml hasn't changed, Maven dependencies won't be re-downloaded.
# This is the most important Docker optimization for Java projects.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user — running as root inside Docker is a security risk
RUN addgroup -S rasgroup && adduser -S rasuser -G rasgroup

# Copy only the JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to our non-root user
RUN chown rasuser:rasgroup app.jar

USER rasuser

EXPOSE 8080

# Use exec form (array syntax) — not shell form.
# Exec form: Java process is PID 1, receives signals correctly (graceful shutdown).
# Shell form: wraps in /bin/sh -c, Java is a child process, signals may not propagate.
ENTRYPOINT ["java", "-jar", "app.jar"]
