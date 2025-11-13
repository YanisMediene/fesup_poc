# 🚀 Guide Rapide - Déploiement FESUP sur Render.com

## ✅ Configuration Complète

Votre application est maintenant **100% prête** pour être déployée sur Render.com !

---

## 📋 Fichiers Configurés

### ✅ Fichiers Créés/Modifiés :

1. **`render.yaml`** - Blueprint orchestration complète (PostgreSQL + Backend + Frontend)
2. **`backend/Dockerfile`** - Optimisé pour Render (support PORT dynamique, healthcheck curl, JVM 512MB)
3. **`backend/src/main/resources/application-prod.properties`** - Support DATABASE_URL de Render
4. **`frontend/Dockerfile`** - Injection dynamique des variables d'environnement (PORT, BACKEND_URL)
5. **`frontend/nginx.conf`** - Support variables ${PORT} et ${BACKEND_URL} + endpoint /health

### ✅ Fichiers Existants (déjà OK) :
- `backend/.dockerignore`
- `frontend/.dockerignore`

---

## 🚀 Déploiement en 3 Étapes

### **Option 1 : Via Blueprint (RECOMMANDÉ)**

```bash
# 1. Pousser vers GitHub
cd /Users/mediene/Informatique/SEM9/POC_GIT/fesup_poc
git add .
git commit -m "Configure pour Render.com deployment"
git push origin main

# 2. Sur Render Dashboard (https://dashboard.render.com)
# - Cliquer "New +" → "Blueprint"
# - Connecter votre repo GitHub
# - Render détecte automatiquement render.yaml
# - Cliquer "Apply Blueprint"
# ✅ Tous les services se déploient automatiquement !
```

### **Option 2 : Déploiement Manuel**

#### A. Créer PostgreSQL
```
Dashboard Render → New + → PostgreSQL
- Name: fesup-postgres
- Database: fesup_db
- User: fesup_user
- Region: Frankfurt
- Plan: Free
✅ Noter l'URL de connexion (Internal Database URL)
```

#### B. Déployer Backend
```
Dashboard Render → New + → Web Service
- Repository: Votre repo GitHub
- Name: fesup-backend
- Environment: Docker
- Docker Context: ./backend
- Dockerfile Path: ./backend/Dockerfile
- Region: Frankfurt
- Plan: Free

Variables d'environnement :
- SPRING_PROFILES_ACTIVE=prod
- DATABASE_URL=[Copier Internal Database URL de PostgreSQL]
- JAVA_OPTS=-Xmx450m -Xms256m -XX:+UseContainerSupport
- PORT=8080

Advanced Settings :
- Health Check Path: /api/actuator/health

✅ Attendre le build (~5-10 min)
✅ Noter l'URL: https://fesup-backend.onrender.com
```

#### C. Déployer Frontend
```
Dashboard Render → New + → Web Service
- Repository: Votre repo GitHub
- Name: fesup-frontend
- Environment: Docker
- Docker Context: ./frontend
- Dockerfile Path: ./frontend/Dockerfile
- Region: Frankfurt
- Plan: Free

Variables d'environnement :
- BACKEND_URL=https://fesup-backend.onrender.com
- PORT=10000

Advanced Settings :
- Health Check Path: /health

✅ Attendre le build (~3-5 min)
✅ Noter l'URL: https://fesup-frontend.onrender.com
```

---

## 🔍 Vérification Post-Déploiement

### 1. Vérifier le Backend
```bash
# Health check
curl https://fesup-backend.onrender.com/api/actuator/health
# Résultat attendu: {"status":"UP"}

# Test API
curl https://fesup-backend.onrender.com/api/activites/publiques
```

### 2. Vérifier le Frontend
```bash
# Ouvrir dans le navigateur
https://fesup-frontend.onrender.com

# Ou tester health
curl https://fesup-frontend.onrender.com/health
# Résultat attendu: healthy
```

### 3. Vérifier la Database
```bash
# Dashboard Render → PostgreSQL → Connect → External Connection
psql "postgresql://fesup_user:PASSWORD@HOST/fesup_db"

# Vérifier tables
\dt

# Quitter
\q
```

---

## 📊 Architecture Déployée

```
┌──────────────────────────────────────────┐
│  https://fesup-frontend.onrender.com    │
│  Angular 17 + Nginx (Port 10000)        │
│  Variables: BACKEND_URL, PORT           │
└────────────────┬─────────────────────────┘
                 │ HTTPS
                 ▼
┌──────────────────────────────────────────┐
│  https://fesup-backend.onrender.com     │
│  Spring Boot (Port 8080)                │
│  Variables: DATABASE_URL, JAVA_OPTS     │
└────────────────┬─────────────────────────┘
                 │ PostgreSQL
                 ▼
┌──────────────────────────────────────────┐
│  fesup-postgres.render.com              │
│  PostgreSQL 16 (Internal)               │
│  Database: fesup_db                     │
└──────────────────────────────────────────┘
```

---

## ⚡ Optimisations Appliquées

### Backend
- ✅ Support `DATABASE_URL` de Render (format PostgreSQL natif)
- ✅ Port dynamique via `${PORT:-8080}`
- ✅ JVM optimisé pour 512MB RAM gratuit
- ✅ Pool de connexions Hikari limité (max 5)
- ✅ Healthcheck avec `curl` (plus léger que `wget`)
- ✅ Compression HTTP activée

### Frontend
- ✅ Script d'injection dynamique des variables au démarrage
- ✅ Support `BACKEND_URL` avec détection auto https://
- ✅ Port dynamique Nginx via `${PORT:-10000}`
- ✅ Endpoint `/health` pour healthcheck Render
- ✅ Compression Gzip activée
- ✅ Multi-stage build (Node 18 → Nginx Alpine)

### Database
- ✅ PostgreSQL 16 gratuit illimité
- ✅ Connexion interne (pas d'exposition publique)
- ✅ Persistance automatique

---

## 🎯 Variables d'Environnement

### Backend
| Variable | Valeur par défaut | Render |
|----------|-------------------|--------|
| `PORT` | 8080 | Auto-injecté |
| `DATABASE_URL` | - | Auto-injecté depuis PostgreSQL |
| `SPRING_PROFILES_ACTIVE` | prod | Défini manuellement |
| `JAVA_OPTS` | -Xmx450m... | Défini manuellement |

### Frontend
| Variable | Valeur par défaut | Render |
|----------|-------------------|--------|
| `PORT` | 10000 | Auto-injecté |
| `BACKEND_URL` | http://backend:8080 | Lien vers backend service |

---

## 🐛 Troubleshooting

### ❌ Backend : "Connection refused to database"
```
Vérifier :
1. PostgreSQL service est "Live"
2. DATABASE_URL est bien configuré
3. Logs backend : "HikariPool started"
```

### ❌ Frontend : "CORS error"
```
Vérifier :
1. BACKEND_URL pointe vers https://fesup-backend.onrender.com
2. application-prod.properties contient :
   cors.allowed-origins=https://*.onrender.com
```

### ❌ Services dormants (15 min inactivité)
```
Plan gratuit Render : apps dorment après 15 min
Premier accès : ~30s pour se réveiller
Solution : Upgrade vers plan payant ($7/mois)
```

### ❌ Build failed
```
Vérifier logs détaillés :
- Dashboard → Service → Logs
- Erreurs courantes :
  * Maven dependencies timeout → Retry build
  * npm install failed → Vérifier package.json
  * Docker context error → Vérifier paths dans render.yaml
```

---

## 💰 Coûts (Plan Gratuit)

| Service | Plan | Coût | Limites |
|---------|------|------|---------|
| PostgreSQL | Free | 0€ | 1GB stockage, 97 connexions |
| Backend | Free | 0€ | 512MB RAM, dort après 15min |
| Frontend | Free | 0€ | 512MB RAM, dort après 15min |
| **TOTAL** | | **0€** | Sans carte bancaire ✅ |

---

## 📚 Ressources

- [Render Documentation](https://render.com/docs)
- [Render Blueprint Spec](https://render.com/docs/blueprint-spec)
- [Guide Complet FESUP Render](./GUIDE-DEPLOIEMENT-RENDER.md)
- [Docker Documentation](./README-DOCKER.md)

---

## ✅ Checklist Finale

- [x] `render.yaml` créé et configuré
- [x] Backend Dockerfile optimisé (curl, PORT dynamique)
- [x] Frontend Dockerfile optimisé (envsubst, script injection)
- [x] `application-prod.properties` supporte DATABASE_URL
- [x] `nginx.conf` supporte variables ${PORT} et ${BACKEND_URL}
- [x] `.dockerignore` présents (backend + frontend)
- [x] Healthchecks configurés
- [x] CORS configuré pour *.onrender.com

---

## 🎉 Prêt à Déployer !

Votre application est **100% prête** pour Render.com. Suivez l'Option 1 (Blueprint) pour un déploiement automatique en quelques clics !

**Commande Git :**
```bash
git add render.yaml backend/ frontend/
git commit -m "Ready for Render.com deployment"
git push origin main
```

**Puis sur Render :**
1. New + → Blueprint
2. Sélectionner votre repo
3. Apply Blueprint
4. ☕ Attendre 10-15 minutes
5. ✅ Application déployée !
