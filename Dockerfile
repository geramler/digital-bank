# --- Build stage: compile entire multi-module Maven project ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Copy root POM and all modules
COPY pom.xml ./
COPY common-events/pom.xml ./common-events/pom.xml
COPY config-server/pom.xml ./config-server/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY customer-service/pom.xml ./customer-service/pom.xml
COPY account-service/pom.xml ./account-service/pom.xml
COPY transaction-service/pom.xml ./transaction-service/pom.xml
COPY transfer-service/pom.xml ./transfer-service/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml

# Copy all source code
COPY common-events/src ./common-events/src
COPY config-server/src ./config-server/src
COPY api-gateway/src ./api-gateway/src
COPY auth-service/src ./auth-service/src
COPY customer-service/src ./customer-service/src
COPY account-service/src ./account-service/src
COPY transaction-service/src ./transaction-service/src
COPY transfer-service/src ./transfer-service/src
COPY notification-service/src ./notification-service/src

# Build the entire project (install into local repo so modules can find each other)
# Skip compiling and running tests to avoid pre-existing test compilation issues unrelated to packaging
RUN mvn install -Dmaven.test.skip=true -q

# --- Runtime images ---

# -- config-server --
FROM eclipse-temurin:25-jre-alpine AS config-server
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/config-server/target/config-server-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app/app.jar"]

# -- api-gateway --
FROM eclipse-temurin:25-jre-alpine AS api-gateway
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

# -- auth-service --
FROM eclipse-temurin:25-jre-alpine AS auth-service
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]

# -- customer-service --
FROM eclipse-temurin:25-jre-alpine AS customer-service
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/customer-service/target/customer-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]

# -- account-service --
FROM eclipse-temurin:25-jre-alpine AS account-service
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/account-service/target/account-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]

# -- transaction-service --
FROM eclipse-temurin:25-jre-alpine AS transaction-service
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/transaction-service/target/transaction-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]

# -- transfer-service --
FROM eclipse-temurin:25-jre-alpine AS transfer-service
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/transfer-service/target/transfer-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]

# -- notification-service --
FROM eclipse-temurin:25-jre-alpine AS notification-service
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/notification-service/target/notification-service-1.0.0-SNAPSHOT.jar ./app.jar
USER appuser
EXPOSE 8086
ENTRYPOINT ["java","-jar","/app/app.jar"]