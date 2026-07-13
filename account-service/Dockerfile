FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY account-service/pom.xml ./account-service/pom.xml
COPY account-service/src ./account-service/src
RUN mvn -f account-service/pom.xml -DskipTests=true package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/account-service/target/account-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
