FROM gradle:jdk21-corretto AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle --no-daemon build || true

COPY . .
RUN gradle --no-daemon clean build

FROM amazoncorretto:21.0.8
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 1234
ENTRYPOINT ["java", "-jar", "app.jar"]