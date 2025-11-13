# 🐳 EPIC-6 : Guide de Déploiement Docker - FESUP

## 📋 **Vue d'ensemble**

Cette architecture Docker contient :
- **Backend** : Spring Boot (Java 17) sur port 8080 (interne)
- **Frontend** : Angular 17 + Nginx sur port 80 (exposé)
- **Database** : PostgreSQL 16 sur port 5432 (interne) / 5433 (externe optionnel)

---

## 🚀 **Démarrage Rapide**

### **Prérequis**
```bash
# Vérifier Docker et Docker Compose
docker --version          # Docker version 24.0+
docker-compose --version  # Docker Compose version 2.0+
```

### **Lancer l'application**
```bash
# 1. Se placer à la racine du projet
cd /Users/mediene/Informatique/SEM9/POC

# 2. Construire et démarrer tous les services
docker-compose up --build

# OU en arrière-plan (mode détaché)
docker-compose up -d --build
```

### **Accéder à l'application**
- **Frontend** : http://localhost
- **Backend API** (via Nginx) : http://localhost/api
- **PostgreSQL** (depuis l'hôte) : localhost:5433

---

## 🛠️ **Commandes Essentielles**

### **Gestion des conteneurs**
```bash
# Voir les logs en temps réel
docker-compose logs -f

# Logs d'un service spécifique
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f db

# Voir le statut des services
docker-compose ps

# Arrêter les services
docker-compose stop

# Arrêter et supprimer les conteneurs
docker-compose down

# Arrêter et supprimer conteneurs + volumes
docker-compose down -v
```

### **Rebuild après modifications**
```bash
# Rebuild un service spécifique
docker-compose build backend
docker-compose build frontend

# Rebuild tout et redémarrer
docker-compose up --build -d

# Forcer le rebuild sans cache
docker-compose build --no-cache backend
```

### **Accéder aux conteneurs**
```bash
# Shell dans le backend
docker exec -it fesup-backend sh

# Shell dans PostgreSQL
docker exec -it fesup-postgres psql -U postgres -d fesup_db

# Shell dans le frontend
docker exec -it fesup-frontend sh
```

---

## 📂 **Structure des Fichiers Docker**

```
POC/
├── docker-compose.yml              # Orchestration des 3 services
├── backend/
│   ├── Dockerfile                  # Multi-stage build Spring Boot
│   ├── .dockerignore              # Exclusions Maven/IDE
│   └── src/main/resources/
│       └── application-prod.properties  # Config production
├── frontend/
│   ├── Dockerfile                  # Multi-stage build Angular
│   ├── nginx.conf                  # Reverse proxy + SPA routing
│   └── .dockerignore              # Exclusions Node/IDE
└── README-DOCKER.md               # Ce fichier
```

---

## 🔧 **Configuration Détaillée**

### **Backend (Spring Boot)**

**Dockerfile** :
- **Stage 1 (build)** : Maven 3.9 + JDK 17 → Compile le `.jar`
- **Stage 2 (runtime)** : JRE 17 Alpine → Image légère (~200 MB)

**Variables d'environnement** (dans `docker-compose.yml`) :
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/fesup_db
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: postgres
APPLICATION_TICKETS_STORAGE_PATH: /app/tickets
JAVA_OPTS: -Xmx512m -Xms256m
```

**Volumes** :
- `tickets_storage:/app/tickets` → Persistance des PDF générés

---

### **Frontend (Angular + Nginx)**

**Dockerfile** :
- **Stage 1 (build)** : Node 18 → Build Angular (`npm run build`)
- **Stage 2 (runtime)** : Nginx Alpine → Serveur web léger (~40 MB)

**nginx.conf** :
- Servir les fichiers statiques Angular
- Reverse proxy : `/api/*` → `http://backend:8080`
- Fallback routing : `try_files` pour le SPA Angular
- Headers CORS configurés
- Compression Gzip activée

**Exemple de requête** :
```
http://localhost/api/voeux/auth
    ↓ (Nginx reverse proxy)
http://backend:8080/api/voeux/auth
```

---

### **Database (PostgreSQL)**

**Configuration** :
```yaml
POSTGRES_DB: fesup_db
POSTGRES_USER: postgres
POSTGRES_PASSWORD: postgres
```

**Volume** :
- `postgres_data:/var/lib/postgresql/data` → Persistance des données

**Accès depuis l'hôte** :
```bash
# Port 5433 mappé vers 5432 interne
psql -h localhost -p 5433 -U postgres -d fesup_db
```

---

## 🔍 **Vérification et Debug**

### **Healthchecks**
Tous les services ont des healthchecks automatiques :
```bash
# Vérifier la santé des services
docker-compose ps

# Détails du healthcheck
docker inspect fesup-backend | grep -A 10 Health
```

### **Tester le backend**
```bash
# Via le frontend (reverse proxy)
curl http://localhost/api/actuator/health

# Directement (si exposé)
curl http://localhost:8080/actuator/health
```

### **Tester le frontend**
```bash
curl http://localhost
# Doit renvoyer le HTML de l'application Angular
```

### **Vérifier la base de données**
```bash
# Se connecter à PostgreSQL
docker exec -it fesup-postgres psql -U postgres -d fesup_db

# Lister les tables
\dt

# Compter les élèves
SELECT COUNT(*) FROM eleves;

# Quitter
\q
```

---

## 📊 **Monitoring et Logs**

### **Logs en temps réel**
```bash
# Tous les services
docker-compose logs -f

# Filtrer par service
docker-compose logs -f backend | grep ERROR
docker-compose logs -f frontend | grep nginx
```

### **Statistiques des conteneurs**
```bash
# Utilisation CPU/RAM/Réseau
docker stats

# Ressources d'un conteneur spécifique
docker stats fesup-backend
```

### **Inspecter les volumes**
```bash
# Lister les volumes
docker volume ls

# Inspecter un volume
docker volume inspect fesup_postgres_data
docker volume inspect fesup_tickets_storage
```

---

## 🔄 **Mise à Jour de l'Application**

### **Workflow de mise à jour**
```bash
# 1. Arrêter les services
docker-compose down

# 2. Modifier le code (backend ou frontend)

# 3. Rebuild et redémarrer
docker-compose up --build -d

# 4. Vérifier les logs
docker-compose logs -f
```

### **Mise à jour d'un seul service**
```bash
# Exemple : mise à jour du backend uniquement
docker-compose stop backend
docker-compose build backend
docker-compose up -d backend
```

---

## 🧹 **Nettoyage**

### **Arrêter et supprimer tout**
```bash
# Supprimer conteneurs et réseaux
docker-compose down

# Supprimer conteneurs, réseaux ET volumes (⚠️ perte de données)
docker-compose down -v

# Supprimer toutes les images FESUP
docker images | grep fesup | awk '{print $3}' | xargs docker rmi
```

### **Nettoyage complet Docker**
```bash
# Supprimer tous les conteneurs arrêtés
docker container prune -f

# Supprimer toutes les images non utilisées
docker image prune -a -f

# Supprimer tous les volumes non utilisés
docker volume prune -f

# Nettoyage global (⚠️ attention)
docker system prune -a --volumes -f
```

---

## 🐛 **Résolution de Problèmes**

### **Problème : Port 80 déjà utilisé**
```bash
# Trouver le processus utilisant le port 80
sudo lsof -i :80

# Modifier le port dans docker-compose.yml
ports:
  - "8080:80"  # Accès via http://localhost:8080
```

### **Problème : Erreur de connexion backend → database**
```bash
# Vérifier que PostgreSQL est démarré
docker-compose ps db

# Vérifier les logs de la DB
docker-compose logs db

# Attendre que le healthcheck passe au vert
docker-compose ps
```

### **Problème : CORS errors dans le frontend**
```bash
# Vérifier nginx.conf
docker exec -it fesup-frontend cat /etc/nginx/conf.d/default.conf

# Recharger Nginx
docker exec -it fesup-frontend nginx -s reload
```

### **Problème : Frontend ne se construit pas**
```bash
# Nettoyer le cache node_modules
rm -rf frontend/node_modules frontend/package-lock.json

# Rebuild sans cache
docker-compose build --no-cache frontend
```

---

## 📈 **Optimisations de Production**

### **1. Limiter les ressources**
```yaml
# Dans docker-compose.yml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          memory: 512M
```

### **2. Utiliser des secrets**
```yaml
# Créer un fichier .env
DB_PASSWORD=super_secret_password

# Dans docker-compose.yml
services:
  db:
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD}
```

### **3. Multi-stage build optimisé**
```dockerfile
# Utiliser des layers cachés
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package
```

---

## 🔐 **Sécurité**

### **Bonnes pratiques appliquées**
✅ Utilisateur non-root dans les conteneurs  
✅ Healthchecks pour la résilience  
✅ Volumes pour la persistance  
✅ Réseaux isolés (bridge)  
✅ Headers de sécurité Nginx  
✅ Secrets via variables d'environnement  
✅ Images Alpine (surface d'attaque réduite)  

### **À améliorer pour la production**
- [ ] Utiliser Docker Secrets au lieu de variables d'environnement
- [ ] Configurer HTTPS avec Let's Encrypt
- [ ] Limiter les ressources CPU/RAM
- [ ] Scanner les images avec Trivy/Snyk
- [ ] Mettre en place un reverse proxy externe (Traefik/Nginx)
- [ ] Configurer les logs centralisés (ELK, Loki)

---

## 📚 **Ressources**

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Angular Deployment](https://angular.io/guide/deployment)
- [Nginx Configuration](https://nginx.org/en/docs/)

---

## ✅ **Checklist de Déploiement**

- [x] Dockerfile backend (multi-stage)
- [x] Dockerfile frontend (multi-stage)
- [x] nginx.conf (reverse proxy + SPA)
- [x] docker-compose.yml (3 services)
- [x] .dockerignore (backend + frontend)
- [x] application-prod.properties
- [x] Healthchecks configurés
- [x] Volumes de persistance
- [x] Documentation complète

---

## 🎉 **Application Déployée !**

Votre application FESUP est maintenant conteneurisée et prête pour le déploiement !

```bash
# Démarrer l'application
docker-compose up -d

# Accéder à l'application
open http://localhost
```

**Support** : Pour toute question, consultez les logs avec `docker-compose logs -f`
