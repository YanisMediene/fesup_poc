# ============================================
# EPIC-6 : Dockerfile Backend (Spring Boot)
# Multi-stage build optimisé pour Render.com
# ============================================

# ========== STAGE 1 : BUILD ==========
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances (mise en cache si pom.xml n'a pas changé)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Compiler et packager l'application (skip tests pour accélérer le build)
RUN mvn clean package -DskipTests -B

# ========== STAGE 2 : RUNTIME ==========
FROM eclipse-temurin:17-jre-alpine

# Métadonnées de l'image
LABEL maintainer="fesup-team"
LABEL description="FESUP Backend - Spring Boot Application (Render.com compatible)"
LABEL version="1.0.0"

# Installer curl pour healthchecks Render
RUN apk add --no-cache curl

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S spring && adduser -S spring -G spring

# Définir le répertoire de travail
WORKDIR /app

# Créer le dossier pour les tickets PDF
RUN mkdir -p /app/tickets && chown -R spring:spring /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Changer le propriétaire
RUN chown -R spring:spring /app

# Passer à l'utilisateur non-root
USER spring:spring

# Exposer le port (dynamique pour Render)
EXPOSE ${PORT:-8080}

# Variables d'environnement par défaut (override par Render/docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx450m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -XX:+ExitOnOutOfMemoryError"

# Point de santé (healthcheck avec support PORT dynamique)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/api/actuator/health || exit 1

# Commande de démarrage (support PORT dynamique pour Render)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Dspring.profiles.active=prod -jar app.jar"]
