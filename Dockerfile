# -------------------------------
#       BUILD STAGE
# -------------------------------
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app

# Copy only pom.xml first (better caching)
COPY pom.xml .

# Download dependencies (optional but speeds rebuilds)
RUN mvn -q dependency:go-offline

# Copy the source code
COPY src ./src

# Package the application (skips tests)
RUN mvn clean package -DskipTests


# -------------------------------
#       RUN STAGE
# -------------------------------
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the Spring Boot port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java","-jar","app.jar"]