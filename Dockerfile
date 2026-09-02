FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /workspace/target/mindbridge-agent-0.1.0.jar /app/mindbridge-agent.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/mindbridge-agent.jar"]
