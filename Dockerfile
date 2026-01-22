# Set the base-image for build stage
FROM maven:3.9.11-eclipse-temurin-17-alpine AS build
# Set up working directory
RUN mkdir -p /usr/app
COPY . /usr/app
WORKDIR /usr/app
# Build the application
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests
# Build the application specific JRE
RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 17 \
    --print-module-deps \
    --class-path 'target/dependencies/*' \
    target/*.jar > modules.info
# Add 'jdk.management' module for JDK-specific management interfaces for the JVM while building application specific JRE
RUN jlink --add-modules jdk.management,jdk.management.agent,$(cat modules.info) \
    --no-header-files \
    --no-man-pages \
    --output /app-jre

# Set the base-image for final stage
FROM alpine:latest
# Set JAVA_HOME using application specific JRE from build-stage
ENV JAVA_HOME /usr/lib/java/jre
ENV PATH $JAVA_HOME/bin:$PATH
COPY --from=build /app-jre $JAVA_HOME
# Copy the artifact from build-stage
RUN mkdir -p /usr/webapp
COPY --from=build /usr/app/target/*.jar /usr/webapp/app.jar
WORKDIR /usr/webapp
# Define environment variables for java-options and application-arguments
ENV JAVA_OPTS=""
ENV APP_ARGS=""
# Build the application start-up script
RUN echo 'java ${JAVA_OPTS} -jar app.jar ${APP_ARGS}' > ./start-app.sh
# Set a non-root user: Add a system group 'appgroup' and a system user 'appuser' in this group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /usr/webapp
RUN chmod +x /usr/webapp/start-app.sh
USER appuser
# Expose the application port
EXPOSE 8080
# Run using start-up script
ENTRYPOINT ["sh", "-c", "./start-app.sh"]
