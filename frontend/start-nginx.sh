#!/bin/sh
# ============================================
# Script de démarrage Nginx pour Render.com
# Injecte les variables d'environnement dynamiques
# ============================================

set -e

echo "🚀 Démarrage de Nginx pour Render.com..."

# Définir PORT par défaut si non défini
export PORT=${PORT:-10000}

# Définir BACKEND_URL par défaut si non défini
if [ -z "$BACKEND_URL" ]; then
    echo "⚠️  BACKEND_URL n'est pas définie, utilisation de la valeur par défaut"
    export BACKEND_URL="http://localhost:8080"
else
    echo "✅ BACKEND_URL détectée: $BACKEND_URL"
fi

echo "📝 Configuration Nginx:"
echo "   PORT: $PORT"
echo "   BACKEND_URL: $BACKEND_URL"

# Remplacer les variables dans le template nginx.conf
echo "🔧 Injection des variables d'environnement dans nginx.conf..."
envsubst '${PORT} ${BACKEND_URL}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

# Afficher la configuration générée (pour debug)
echo "📄 Configuration Nginx générée:"
cat /etc/nginx/nginx.conf | head -20

# Tester la configuration Nginx
echo "✓ Test de la configuration Nginx..."
nginx -t

if [ $? -eq 0 ]; then
    echo "✅ Configuration Nginx valide!"
else
    echo "❌ Erreur dans la configuration Nginx!"
    exit 1
fi

# Démarrer Nginx en mode foreground
echo "🎯 Démarrage de Nginx sur le port $PORT..."
exec nginx -g 'daemon off;'
