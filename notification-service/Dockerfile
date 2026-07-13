FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY notification-service/src ./notification-service/src
RUN mvn -f notification-service/pom.xml -DskipTests=true package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/notification-service/target/notification-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8086
ENTRYPOINT ["java","-jar","/app/app.jar"]
