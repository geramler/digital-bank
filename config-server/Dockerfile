FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY config-server/pom.xml ./config-server/pom.xml
COPY config-server/src ./config-server/src
RUN mvn -f config-server/pom.xml -DskipTests=true package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/config-server/target/config-server-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app/app.jar"]
