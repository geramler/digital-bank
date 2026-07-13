FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY transfer-service/pom.xml ./transfer-service/pom.xml
COPY transfer-service/src ./transfer-service/src
RUN mvn -f transfer-service/pom.xml -DskipTests=true package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/transfer-service/target/transfer-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
