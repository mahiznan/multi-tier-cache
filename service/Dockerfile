# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# copy pom first to leverage cache for dependencies
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# now copy source and build
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /build/target/service-*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
