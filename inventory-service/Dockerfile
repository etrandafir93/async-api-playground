# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# First, build and install the contracts module
COPY ./contracts/pom.xml ./contracts/pom.xml
COPY ./contracts/src ./contracts/src
WORKDIR /app/contracts
RUN mvn clean install -DskipTests

# Now build the inventory-service
WORKDIR /app/inventory-service
COPY ./inventory-service/pom.xml .
COPY ./inventory-service/src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/inventory-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
