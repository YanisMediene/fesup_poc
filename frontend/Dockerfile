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

# Copier la configuration Nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exposer le port 80
EXPOSE 80

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1

# Démarrer Nginx (au premier plan)
CMD ["nginx", "-g", "daemon off;"]
