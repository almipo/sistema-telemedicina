FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 simulador
COPY --from=build /build/target/simulador-iot-1.0.0.jar /app/simulador.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app/simulador.jar"]
