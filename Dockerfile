FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml -q -DskipTests dependency:go-offline

COPY backend backend
RUN mvn -f backend/pom.xml -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV SERVER_PORT=8080

COPY --from=build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
