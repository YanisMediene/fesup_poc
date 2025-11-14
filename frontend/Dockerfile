# ============================================
# EPIC-6 : Dockerfile Frontend (Angular 17)
# Multi-stage build avec Nginx
# ============================================

# ========== STAGE 1 : BUILD ==========
FROM node:18-alpine AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de dépendances
COPY package*.json ./

# Installer les dépendances
RUN npm ci --legacy-peer-deps

# Copier le code source
COPY . .

# Build de l'application Angular en mode production
RUN npm run build -- --configuration production

# ========== STAGE 2 : RUNTIME ==========
FROM nginx:1.25-alpine

# Métadonnées de l'image
LABEL maintainer="fesup-team"
LABEL description="FESUP Frontend - Angular Application with Nginx"
LABEL version="1.0.0"

# Supprimer la configuration Nginx par défaut
RUN rm -rf /usr/share/nginx/html/* /etc/nginx/conf.d/default.conf

# Copier les fichiers buildés depuis l'étape de build
COPY --from=build /app/dist/fesup-voeux-frontend /usr/share/nginx/html

# Copier le template Nginx (avec variables à injecter)
COPY nginx.conf.template /etc/nginx/nginx.conf.template

# Copier l'ancien nginx.conf pour compatibilité Docker Compose local
COPY nginx.conf /etc/nginx/conf.d/default.conf.backup

# Copier le script de démarrage pour Render
COPY start-nginx.sh /start-nginx.sh
RUN chmod +x /start-nginx.sh

# Installer gettext pour envsubst (substitution de variables)
RUN apk add --no-cache gettext

# Exposer le port dynamique (80 local, 10000 Render)
EXPOSE 80

# Healthcheck - port dynamique pour Render
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-80}/health || exit 1

# Démarrer via script pour Render (injection variables)
# Pour Docker Compose local, utiliser: docker-compose.yml avec command override
CMD ["/start-nginx.sh"]
