# ---- Stage 1: build ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ---- Stage 2: final ----
FROM eclipse-temurin:21-jre-alpine AS final
WORKDIR /app

COPY --from=build /app/target/nuevaases-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]