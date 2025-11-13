# ============================================
# Dockerfile Frontend (Angular 17 + Nginx)
# Optimisé pour Render.com avec variables dynamiques
# ============================================

# ========== STAGE 1 : BUILD ==========
FROM node:18-alpine AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de dépendances
COPY package*.json ./

# Installer les dépendances
RUN npm ci --legacy-peer-deps --quiet

# Copier le code source
COPY . .

# Build de l'application Angular en mode production
RUN npm run build -- --configuration production

# ========== STAGE 2 : RUNTIME ==========
FROM nginx:1.25-alpine

# Métadonnées de l'image
LABEL maintainer="fesup-team"
LABEL description="FESUP Frontend - Angular + Nginx (Render.com compatible)"
LABEL version="1.0.0"

# Supprimer la configuration Nginx par défaut
RUN rm -rf /usr/share/nginx/html/* /etc/nginx/conf.d/default.conf

# Copier les fichiers buildés depuis l'étape de build
COPY --from=build /app/dist/fesup-voeux-frontend /usr/share/nginx/html

# Copier la configuration Nginx comme template
COPY nginx.conf /etc/nginx/nginx.conf.template

# Installer gettext pour envsubst (substitution variables)
RUN apk add --no-cache gettext

# Créer le script de démarrage pour injecter variables Render
RUN cat > /docker-entrypoint.sh << 'EOF'
#!/bin/sh
set -e

echo "🚀 Starting NGINX with Render configuration..."

# Définir PORT par défaut si non défini
export PORT=${PORT:-10000}

# Définir BACKEND_URL par défaut si non défini
if [ -z "$BACKEND_URL" ]; then
    echo "⚠️  BACKEND_URL not set, using default"
    export BACKEND_URL="http://backend:8080"
else
    # Ajouter le schéma https:// si BACKEND_URL est un domaine Render
    if echo "$BACKEND_URL" | grep -q "onrender.com" && ! echo "$BACKEND_URL" | grep -q "^http"; then
        export BACKEND_URL="https://$BACKEND_URL"
    fi
fi

echo "📝 Configuration:"
echo "   PORT: $PORT"
echo "   BACKEND_URL: $BACKEND_URL"

# Remplacer variables dans nginx.conf
envsubst '${PORT} ${BACKEND_URL}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf

# Tester configuration NGINX
nginx -t

# Démarrer NGINX
echo "✅ Starting NGINX..."
exec nginx -g 'daemon off;'
EOF

RUN chmod +x /docker-entrypoint.sh

# Exposer port dynamique Render (10000 par défaut)
EXPOSE ${PORT:-10000}

# Healthcheck avec support PORT dynamique
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT:-10000}/health || exit 1

# Utiliser le script de démarrage
ENTRYPOINT ["/docker-entrypoint.sh"]
