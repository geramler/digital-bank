FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY transaction-service/pom.xml ./transaction-service/pom.xml
COPY transaction-service/src ./transaction-service/src
RUN mvn -f transaction-service/pom.xml -DskipTests=true package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/transaction-service/target/transaction-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
