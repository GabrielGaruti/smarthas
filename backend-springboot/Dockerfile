# ============================================================
# Smart HAS API - imagem Docker (Fase 7)
# Build em duas etapas: compila com Maven e roda so com o JRE.
#   docker build -t smarthas-api backend-springboot
#   docker run -p 8080:8080 smarthas-api                       (H2)
#   docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=oracle \
#     -e ORACLE_URL=... -e ORACLE_USER=... -e ORACLE_PASSWORD=... smarthas-api
# ============================================================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --home /app smarthas && mkdir -p /app/data && chown smarthas /app/data
COPY --from=build /app/target/smarthas-api-*.jar app.jar
USER smarthas
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
