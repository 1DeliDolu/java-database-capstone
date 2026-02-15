# Step 1: Use Maven with JDK 17 to build the app
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Use lightweight JRE 17 for running the app
FROM eclipse-temurin:17.0.15_6-jre
WORKDIR /app
COPY --from=builder /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar
RUN useradd --system --uid 1001 springuser
USER springuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
