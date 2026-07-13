FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY auth-service/src ./auth-service/src
RUN mvn -f auth-service/pom.xml -DskipTests=true package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
