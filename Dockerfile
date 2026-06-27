FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
RUN chmod +x ./gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
RUN ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
