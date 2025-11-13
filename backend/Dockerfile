# Stage 1: Build avec Maven
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copier pom.xml et télécharger dépendances (optimisation cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier sources et compiler
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime léger
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Installer curl pour health checks
RUN apk add --no-cache curl

# Copier JAR compilé
COPY --from=builder /app/target/fesup-voeux-backend-1.0.0-SNAPSHOT.jar /app/app.jar

# Créer utilisateur non-root (sécurité)
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring
USER spring:spring

# Port dynamique Render
EXPOSE 8080

# Variables JVM optimisées pour 512MB RAM gratuit
ENV JAVA_OPTS="-Xmx450m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -XX:+ExitOnOutOfMemoryError"

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/api/actuator/health || exit 1

# Commande de démarrage
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Dspring.profiles.active=prod -jar app.jar"]