# =============================================================
# Dockerfile for the Java CLI Calculator (Lab 6)
# -------------------------------------------------------------
# Multi-stage build:
#   Stage 1 "build"  : Maven + JDK 17 image -> compile & package
#   Stage 2 (runtime): slim JRE 17 image    -> run the jar only
# Build:  docker build -t calculator:v1 .
# Run:    docker run --rm calculator:v1 10 / 4
# =============================================================

# ---------- Stage 1: build stage ----------
# FROM specifies the base image that a stage starts from. The official
# maven:3.8-openjdk-17 image already contains Maven 3.8 and JDK 17, so
# the whole build toolchain comes from Docker Hub instead of the host
# machine - this is what makes the project reproducible everywhere.
# "AS build" names this stage so that stage 2 can copy files out of it.
FROM maven:3.8-openjdk-17 AS build

# WORKDIR sets the current working directory inside the image and
# creates it if it does not exist. Every later instruction runs there.
WORKDIR /app

# ARG declares a variable that can be set at build time with --build-arg.
# MAVEN_CENTRAL_MIRROR is empty by default, but on a network where Maven
# Central is restricted or unstable a mirror can be injected, e.g.
#   --build-arg MAVEN_CENTRAL_MIRROR=https://maven.aliyun.com/repository/public
# The conditional RUN then writes a Maven settings.xml that redirects the
# "central" repository to that mirror while the image is being built.
ARG MAVEN_CENTRAL_MIRROR=""
RUN if [ -n "$MAVEN_CENTRAL_MIRROR" ]; then \
      mkdir -p /root/.m2 && \
      printf '<settings><mirrors><mirror><id>central-mirror</id><mirrorOf>central</mirrorOf><url>%s</url></mirror></mirrors></settings>' "$MAVEN_CENTRAL_MIRROR" > /root/.m2/settings.xml; \
    fi

# COPY brings the project files into the build stage: first the pom.xml,
# then the Java source tree under src/.
COPY pom.xml .
COPY src ./src

# RUN compiles the sources and packages the fat (shaded) jar with all
# dependencies included. Tests are skipped here because the full JUnit
# suite already runs in the GitHub Actions CI job before this build.
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime stage ----------
# A second FROM starts a new, much smaller stage. eclipse-temurin:17-jre-alpine
# contains ONLY the Java 17 runtime (no Maven, no compiler, no source code) on
# top of the tiny Alpine Linux base, which keeps the final image small and its
# attack surface low. (The originally suggested openjdk:17-jre-slim image has
# been retired from Docker Hub, so the maintained Temurin build is used.)
FROM eclipse-temurin:17-jre-alpine

# WORKDIR defines the default working directory of the runtime container.
WORKDIR /app

# COPY --from=build copies only the finished jar out of the first
# stage. None of the Maven cache, sources or JDK is carried over -
# this is the key benefit of a multi-stage build.
COPY --from=build /app/target/calculator-1.0.jar app.jar

# ENTRYPOINT pins the fixed executable of the container: every container
# start runs "java -jar app.jar", and any extra arguments given to
# "docker run" are appended to it, e.g. docker run --rm calculator:v1 10 / 4
ENTRYPOINT ["java", "-jar", "app.jar"]

# CMD provides DEFAULT arguments for the entrypoint. Running the image
# without arguments evaluates the demo expression "2 + 2"; passing
# arguments on "docker run" (e.g. 10 / 4) replaces CMD completely.
CMD ["2", "+", "2"]
