FROM maven:3.9.6-eclipse-temurin-22 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:22-jdk
COPY --from=build /target/UniversityWebApi-beta.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","application.jar"]