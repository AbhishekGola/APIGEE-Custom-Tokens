# ---- Build Stage ----
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY . .

RUN ./mvnw clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""

WORKDIR /opt/app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
