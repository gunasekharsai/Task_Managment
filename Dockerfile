FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY app/pom.xml .
COPY app/src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

CMD ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]
