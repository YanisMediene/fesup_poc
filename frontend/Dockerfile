# Stage 1: Build Angular
FROM node:18-alpine AS builder
WORKDIR /app

# Installer dépendances
COPY package*.json ./
RUN npm ci --legacy-peer-deps --quiet

# Copier sources et compiler en mode production
COPY . .
RUN npm run build -- --configuration production

# Stage 2: NGINX Runtime
FROM nginx:1.25-alpine
WORKDIR /usr/share/nginx/html

# Copier le build Angular
COPY --from=builder /app/dist/frontend/browser ./

# Copier configuration NGINX template
COPY nginx.conf /etc/nginx/nginx.conf.template

# Installer gettext pour envsubst (substitution variables)
RUN apk add --no-cache gettext

# Script de démarrage pour injecter variables Render
RUN cat > /docker-entrypoint.sh << 'EOF'
#!/bin/sh
set -e

echo "🚀 Starting NGINX with Render configuration..."

# Définir PORT par défaut si non défini
export PORT=${PORT:-10000}

# Définir BACKEND_URL par défaut si non défini
if [ -z "$BACKEND_URL" ]; then
    echo "⚠️  BACKEND_URL not set, using default"
    export BACKEND_URL="http://localhost:8080"
fi

echo "📝 Configuration:"
echo "   PORT: $PORT"
echo "   BACKEND_URL: $BACKEND_URL"

# Remplacer variables dans nginx.conf
envsubst '${PORT} ${BACKEND_URL}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

# Tester configuration NGINX
nginx -t

# Démarrer NGINX
echo "✅ Starting NGINX..."
exec nginx -g 'daemon off;'
EOF

RUN chmod +x /docker-entrypoint.sh

# Expose port dynamique Render (10000 par défaut)
EXPOSE ${PORT:-10000}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT:-10000}/health || exit 1

ENTRYPOINT ["/docker-entrypoint.sh"]