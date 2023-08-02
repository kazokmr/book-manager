FROM openjdk:20-slim-bullseye as build
WORKDIR /app
COPY . /app
RUN ./gradlew clean bootJar


FROM openjdk:20-slim-bullseye
WORKDIR /app
VOLUME /tmp
COPY --from=build /app/build/libs/book-manager-0.0.1-SNAPSHOT.jar /app/book-manager.jar
ENV TZ="Asia/Tokyo"
ENTRYPOINT ["java", "-jar", "/app/book-manager.jar", "--spring.profiles.active=docker"]
