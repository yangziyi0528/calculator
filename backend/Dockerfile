# ==============================================================
# Dockerfile for the Calculator BACKEND service (Lab 7)
# --------------------------------------------------------------
# Builds the Java HTTP API that wraps the Lab 3 calculator logic.
# The container listens on port 8000 and is reached by the web
# container through the Compose service name "backend".
# Build:  docker build -t calculator-backend:v1 .
# Run:    docker run --rm -p 8000:8000 calculator-backend:v1
# ==============================================================

# ---------- Stage 1: build stage ----------
# Multi-stage build, same pattern as Lab 6: the official
# maven:3.8-openjdk-17 image brings Maven 3.8 and JDK 17, so the
# build toolchain comes from Docker Hub, not from the host.
# "AS build" names the stage so stage 2 can copy the jar out of it.
FROM maven:3.8-openjdk-17 AS build

# WORKDIR sets (and creates) the working directory for all later
# instructions inside the image.
WORKDIR /app

# ARG declares a build-time variable. MAVEN_CENTRAL_MIRROR is empty
# by default; on a network where Maven Central is unstable it can be
# injected with --build-arg (docker-compose passes it via build.args),
# which makes the RUN below write a Maven settings.xml pointing the
# "central" repository at that mirror (see Lab 6, Section 6.3).
ARG MAVEN_CENTRAL_MIRROR=""
RUN if [ -n "$MAVEN_CENTRAL_MIRROR" ]; then \
      mkdir -p /root/.m2 && \
      printf '<settings><mirrors><mirror><id>central-mirror</id><mirrorOf>central</mirrorOf><url>%s</url></mirror></mirrors></settings>' "$MAVEN_CENTRAL_MIRROR" > /root/.m2/settings.xml; \
    fi

# COPY first the build description, then the Java sources. Copying
# pom.xml separately keeps Docker layer caching effective: source
# edits do not invalidate the dependency-download layer.
COPY pom.xml .
COPY src ./src

# RUN compiles the sources and packages the shaded fat jar (all
# dependencies included, Main-Class = com.yangziyi.ApiServer).
# Tests are skipped here; the full JUnit suite already runs in the
# CI test job before the images are built.
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime stage ----------
# Second, much smaller stage: eclipse-temurin:17-jre-alpine contains
# ONLY the Java 17 runtime on Alpine Linux - no Maven, no compiler,
# no source code. This keeps the final image small.
FROM eclipse-temurin:17-jre-alpine

# WORKDIR defines the default working directory of the container.
WORKDIR /app

# COPY --from=build moves ONLY the finished jar from stage 1 into the
# runtime image; the 1.26 GB build toolchain stays behind.
COPY --from=build /app/target/calculator-1.0.jar app.jar

# ENV sets a default environment variable inside the container. The
# ApiServer reads LOG_LEVEL to switch request logging on (INFO) or
# off (OFF). docker-compose "environment:" can override this value.
ENV LOG_LEVEL=INFO

# EXPOSE documents that the container listens on port 8000. It is
# metadata for humans and tools; the actual host mapping is done by
# docker-compose with "ports: 8000:8000".
EXPOSE 8000

# ENTRYPOINT pins the fixed executable: every container start runs
# the API server (the jar's Main-Class is com.yangziyi.ApiServer).
ENTRYPOINT ["java", "-jar", "app.jar"]
