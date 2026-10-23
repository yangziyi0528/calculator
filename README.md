# CLI Calculator - Maven + JUnit 5 + GitHub Actions

![Calculator CI](https://github.com/yangziyi0528/calculator/actions/workflows/ci-calculator.yml/badge.svg)

A small command-line calculator written in Java and built with Maven. This
project is the continuation of Lab 3 (Maven build) and Lab 4 (unit testing,
debugging and static analysis), and in Lab 5 it is wired to an automated
Continuous Integration pipeline running on GitHub Actions.

## Features

* Basic arithmetic: `+`, `-`, `*`, `/`
* Extended operators: `^` (power) and `%` (modulo)
* Division-by-zero protection (`ArithmeticException`)
* Input validation for blank and non-numeric operands
* 37 JUnit 5 unit tests
* JaCoCo code coverage report
* Checkstyle static code analysis
* GitHub Actions CI workflow (`.github/workflows/ci-calculator.yml`)

## Requirements

* JDK 17 or newer
* Maven 3.8 or newer

## Build and test

```bash
mvn clean compile   # compile the sources
mvn test            # run all JUnit 5 tests
mvn package         # build the executable jar
```

## Run

```bash
java -jar target/calculator-1.0.jar 2 + 2      # -> 4.0
java -jar target/calculator-1.0.jar 10 / 4     # -> 2.5
java -jar target/calculator-1.0.jar 2 ^ 3      # -> 8.0
```

## Continuous Integration

Every push to `main` and every pull request targeting `main` triggers the
`Calculator CI` workflow, which compiles the sources and runs the whole test
suite on JDK 17 and JDK 21 in parallel. The badge at the top of this page
always reflects the status of the most recent run.

## Docker (Lab 6)

The project ships with a multi-stage `Dockerfile`: the build stage compiles
and packages the jar inside the official `maven:3.8-openjdk-17` image, and
the runtime stage copies only the finished jar into the slim
`eclipse-temurin:17-jre-alpine` image.

```bash
docker build -t calculator:v1 .                # build the image
docker run --rm calculator:v1                  # run with the default 2 + 2
docker run --rm calculator:v1 10 / 4           # run a custom expression
docker tag calculator:v1 yangziyi0528/calculator:lab6
docker push yangziyi0528/calculator:lab6       # publish to Docker Hub
```

The CI workflow builds this image (and smoke-tests `+ - * /` inside the
container) after the test matrix passes; on pushes to `main` it also pushes
`<dockerhub-user>/calculator:lab6` to Docker Hub when the
`DOCKERHUB_USERNAME` / `DOCKERHUB_TOKEN` secrets are configured.
