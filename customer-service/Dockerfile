FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY customer-service/pom.xml ./customer-service/pom.xml
COPY customer-service/src ./customer-service/src
RUN mvn -f customer-service/pom.xml -DskipTests=true package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/customer-service/target/customer-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
