# FESUP - Système Complet de Gestion de Forum des Métiers

## 📋 Vue d'ensemble

**FESUP** est une application web complète permettant la gestion de bout en bout d'un forum des métiers pour lycéens, de la saisie des vœux jusqu'à la génération automatique des plannings personnalisés.

### Fonctionnalités principales

✅ **Workflow Élève**
- Identification sécurisée (ID + Nom)
- Saisie de 5 vœux avec validation temps réel
- Téléchargement du planning PDF personnalisé

✅ **Interface Administrateur**
- Gestion complète des entités (Lycées, Élèves, Activités, Salles, Créneaux)
- Import CSV en masse
- Génération automatique des sessions
- Algorithme d'affectation intelligent (Timefold AI)
- Génération batch de tickets PDF
- Export CSV de toutes les données
- Statistiques et tableaux de bord

✅ **Super Admin**
- Réinitialisation complète du système
- Statistiques globales
- Export CSV/ZIP de toutes les données

### Stack Technique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| **Backend** | Spring Boot | 3.2.0 |
| **Base de données** | PostgreSQL | 16 |
| **Frontend** | Angular | 17 |
| **Serveur web** | Nginx | 1.25 |
| **Optimisation** | Timefold AI | Latest |
| **Génération PDF** | Apache PDFBox | 2.0.29 |
| **Conteneurisation** | Docker | Latest |

---

## 🏗️ Architecture Globale

### Vue d'ensemble N-Tiers

```
┌─────────────────────────────────────────────────────────────┐
│                     NAVIGATEUR CLIENT                       │
│                    (http://localhost)                       │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/HTTPS
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              FRONTEND - Angular 17 + Nginx                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  • Workflow Élève (4 écrans)                         │   │
│  │  • Dashboard Admin (9 modules)                       │   │
│  │  • Services HTTP + Guards + Interceptors             │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Nginx (Port 80)                                     │   │
│  │  • Reverse Proxy : /api/* → backend:8080             │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              BACKEND - Spring Boot 3.2.0                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controllers (REST API)                              │   │
│  │  • VoeuxController (public)                          │   │
│  │  • AdminControllers (secured)                        │   │
│  │  • SystemSettingsController (superadmin)             │   │
│  └──────────────────┬───────────────────────────────────┘   │
│  ┌──────────────────▼───────────────────────────────────┐   │
│  │  Services (Business Logic)                           │   │
│  │  • VoeuxService (validation vœux)                    │   │
│  │  • SessionGenerationService                          │   │
│  │  • AffectationService (Timefold)                     │   │
│  │  • BatchPdfService (génération async)                │   │
│  │  • CsvExportService (exports)                        │   │
│  └──────────────────┬───────────────────────────────────┘   │
│  ┌──────────────────▼───────────────────────────────────┐   │
│  │  Repositories (Spring Data JPA)                      │   │
│  └──────────────────┬───────────────────────────────────┘   │
└─────────────────────┼───────────────────────────────────────┘
                      │ JPA/Hibernate
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              DATABASE - PostgreSQL 16                       │
│  • Base: fesup_db                                           │
│  • 11 tables principales                                    │
│  • Volume persistant: postgres_data                         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              STOCKAGE FICHIERS                              │
│  • Volume: tickets_storage                                  │
│  • Path: /app/tickets/{année}/eleve_{id}.pdf                │
└─────────────────────────────────────────────────────────────┘
```

### Déploiement Docker Compose

```yaml
Services:
  - postgres (DB)
  - backend (Spring Boot)
  - frontend (Angular + Nginx)

Network: fesup-network (bridge)

Volumes:
  - postgres_data (persistance DB)
  - tickets_storage (PDFs)
```

---

## 📊 Modèle de Données

### Schéma Relationnel

```
                    ┌─────────┐
                    │  User   │
                    │ (Admin) │
                    └─────────┘

┌────────┐      ┌─────────┐      ┌──────────┐
│ Lycee  │──<───│  Eleve  │──<───│   Voeu   │
└────────┘      └────┬────┘      └────┬─────┘
                     │                 │
                     │                 │
                ┌────▼────┐       ┌────▼─────┐
                │ Ticket  │       │ Activite │
                │  (PDF)  │       └────┬─────┘
                └─────────┘            │
                                       │
┌─────────┐     ┌─────────┐      ┌────▼─────┐
│ Creneau │──<──│ Session │──<───│          │
└─────────┘     └────┬────┘      └──────────┘
                     │
┌─────────┐          │
│  Salle  │──────────┘
└─────────┘

                ┌──────────────┐
                │ Affectation  │
                │   (Résultat  │
                │  algorithme) │
                └──────────────┘
```

### Entités Principales

#### 1. **Eleve**
```java
- id : Long (PK, technique)
- nom : String
- prenom : String
- idNational : String (UNIQUE, NOT NULL, length=50) ⭐ IDENTIFIANT FONCTIONNEL
- lycee : Lycee (ManyToOne)
- demiJournee : JOUR1_MATIN | JOUR1_APRES_MIDI | JOUR2_MATIN | JOUR2_APRES_MIDI
- voeuxSoumis : boolean
- dateSoumission : LocalDateTime
- voeux : List<Voeu> (OneToMany)
- affectations : List<Affectation> (OneToMany)
- ticket : Ticket (OneToOne)
```

> 🔑 **Double identifiant** :
> - `id` : Clé primaire technique (auto-increment) pour les FK internes
> - `idNational` : Identifiant fonctionnel utilisé pour l'authentification (ex: `120890177FA`)
> - Format idNational : Alphanumérique majuscule, 5-50 caractères

#### 2. **Activite**
```java
- id : Long (PK)
- titre : String
- description : String
- type : CONFERENCE | TABLE_RONDE | FLASH_METIER
- demiJournee : JOUR1_MATIN | JOUR1_APRES_MIDI | JOUR2_MATIN | JOUR2_APRES_MIDI
- capaciteMax : int
- voeux : List<Voeu> (OneToMany)
- sessions : List<Session> (OneToMany)
```

#### 3. **Session** (Instance d'une activité dans un créneau/salle)
```java
- id : Long (PK)
- activite : Activite (ManyToOne)
- salle : Salle (ManyToOne)
- creneau : Creneau (ManyToOne)
- capacite : int
- placesPrises : int
- affectations : List<Affectation> (OneToMany)
```

#### 4. **Affectation** (Résultat de l'algorithme)
```java
- id : Long (PK)
- eleve : Eleve (ManyToOne)
- assignedSession : Session (ManyToOne)
```

#### 5. **Ticket** (PDF généré)
```java
- id : Long (PK)
- eleve : Eleve (OneToOne)
- cheminFichier : String
- dateGeneration : LocalDateTime
```

---

## 🔄 Workflow Complet

### 1️⃣ Workflow Élève (Interface Publique)

```
Étape 1: Identification
┌────────────────────────────────────────────┐
│  Saisie ID National + Nom                  │
│  Format ID: 12 chiffres + 2 lettres        │
│  Exemple: 120890177FA                      │
│  ↓                                         │
│  Validation backend (POST /api/voeux/auth) │
│  Recherche: findByIdNationalAndNom()       │
└────────────────────────────────────────────┘
                ↓
Étape 2: Confirmation Identité
┌──────────────────────────────────────┐
│  Affichage: Prénom, Nom, Lycée,      │
│  Demi-journée                        │
│  ↓                                   │
│  Confirmation visuelle               │
└──────────────────────────────────────┘
                ↓
Étape 3: Formulaire de Vœux
┌──────────────────────────────────────┐
│  Vœu 1: Conférence                   │
│  Vœu 2: Conférence (≠ Vœu 1)         │
│  Vœu 3: Conférence/Table/Flash       │
│  Vœu 4: Conférence/Table/Flash       │
│  Vœu 5: Conférence/Table/Flash       │
│  ↓                                   │
│  Validation temps réel (Angular)     │
└──────────────────────────────────────┘
                ↓
Étape 3.5: Récapitulatif
┌──────────────────────────────────────┐
│  Affichage des 5 vœux                │
│  ↓                                   │
│  Confirmation finale                 │
└──────────────────────────────────────┘
                ↓
Étape 4: Soumission
┌──────────────────────────────────────┐
│  POST /api/voeux/soumettre           │
│  ↓                                   │
│  Validation backend complexe         │
│  ↓                                   │
│  Sauvegarde en BDD                   │
│  ↓                                   │
│  Marquage voeuxSoumis = true         │
└──────────────────────────────────────┘
                ↓
Étape 5: Confirmation
┌──────────────────────────────────────┐
│  Message de succès                   │
│  Date de soumission                  │
└──────────────────────────────────────┘

[PLUS TARD, APRÈS AFFECTATION]

Étape 6: Téléchargement Planning
┌──────────────────────────────────────┐
│  Re-identification (ID National + Nom)│
│  ↓                                   │
│  GET /api/voeux/mon-ticket           │
│  Paramètres: idNational + nom        │
│  ↓                                   │
│  Téléchargement PDF                  │
└──────────────────────────────────────┘
```

### 2️⃣ Workflow Administrateur

```
Phase 1: Configuration Initiale
┌──────────────────────────────────────┐
│  1. Connexion (/admin/login)         │
│  2. Créer Lycées                     │
│  3. Importer Élèves (CSV ou manuel)  │
│     Format CSV: nom,prenom,          │
│     idNational,lycee,ville,          │
│     codePostal,demiJournee           │
│  4. Créer Salles                     │
│  5. Créer Créneaux                   │
│  6. Créer Activités                  │
└──────────────────────────────────────┘
                ↓
Phase 2: Attente Soumission Vœux
┌──────────────────────────────────────┐
│  Les élèves soumettent leurs vœux    │
│  Suivi en temps réel sur dashboard   │
└──────────────────────────────────────┘
                ↓
Phase 3: Génération Sessions
┌───────────────────────────────────────────┐
│  POST /api/admin/sessions/generation/auto │
│  ↓                                        │
│  Analyse des vœux par activité            │
│  ↓                                        │
│  Calcul nb sessions nécessaires           │
│  ↓                                        │
│  Assignment auto salle + créneau          │
└───────────────────────────────────────────┘
                ↓
Phase 4: Lancement Algorithme
┌──────────────────────────────────────┐
│  POST /api/admin/affectations/lancer │
│  ↓                                   │
│  Timefold résout pendant 30s         │
│  ↓                                   │
│  Contraintes Hard (obligatoires)     │
│  - Pas de conflit créneau            │
│  - Capacité respectée                │
│  ↓                                   │
│  Contraintes Soft (à maximiser)      │
│  - Satisfaction vœux prioritaires    │
│  ↓                                   │
│  Sauvegarde affectations en BDD      │
└──────────────────────────────────────┘
                ↓
Phase 5: Validation Résultats
┌──────────────────────────────────────┐
│  Analyse du score                    │
│  - 0hard = Aucun conflit.            │
│  - Soft proche de 0 = Optimal        │
│  ↓                                   │
│  Modifications manuelles (optionnel) │
└──────────────────────────────────────┘
                ↓
Phase 6: Génération Tickets PDF
┌──────────────────────────────────────┐
│  POST /api/admin/tickets/generer-tous│
│  ↓                                   │
│  Génération asynchrone (batch)       │
│  ↓                                   │
│  Pour chaque élève:                  │
│  - Créer PDF avec PDFBox             │
│  - En-tête personnalisé              │
│  - Tableau des 5 activités           │
│  - Sauvegarder sur disque            │
│  - Créer entrée Ticket en BDD        │
└──────────────────────────────────────┘
                ↓
Phase 7: Export Données (optionnel)
┌──────────────────────────────────────┐
│  GET /api/superadmin/system/export/* │
│  ↓                                   │
│  Export CSV individuel ou ZIP        │
└──────────────────────────────────────┘
```

---

## 🎯 API REST - Endpoints Complets

### 🔓 Public (Workflow Élève)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/voeux/auth` | Vérification identité (ID National + Nom) |
| `GET` | `/api/voeux/activites/{demiJournee}` | Liste activités filtrées |
| `POST` | `/api/voeux/soumettre` | Soumission 5 vœux |
| `GET` | `/api/voeux/status/{eleveId}` | Statut soumission |
| `GET` | `/api/voeux/mon-ticket?idNational={id}&nom={nom}` | Téléchargement PDF |
| `GET` | `/api/voeux/mon-ticket/status?idNational={id}&nom={nom}` | Vérif disponibilité PDF |

### 🔐 Admin (Configuration)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/auth/login` | Connexion admin (génère JWT) |
| `GET/POST/PUT/DELETE` | `/api/admin/lycees` | CRUD Lycées |
| `GET/POST/PUT/DELETE` | `/api/admin/eleves` | CRUD Élèves |
| `POST` | `/api/admin/eleves/import-csv` | Import CSV élèves (avec idNational) |
| `GET/POST/PUT/DELETE` | `/api/admin/activites` | CRUD Activités |
| `POST` | `/api/admin/activites/import-csv` | Import CSV activités |
| `GET/POST/PUT/DELETE` | `/api/admin/salles` | CRUD Salles |
| `POST` | `/api/admin/salles/import-csv` | Import CSV salles |
| `GET/POST/PUT/DELETE` | `/api/admin/creneaux` | CRUD Créneaux |

### 🚀 Admin (Sessions & Affectations)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/admin/sessions/generation/auto` | Génération auto sessions |
| `GET/DELETE` | `/api/admin/sessions` | Liste/Suppression sessions |
| `POST` | `/api/admin/affectations/lancer` | Lancer algorithme Timefold |
| `GET` | `/api/admin/affectations/status` | Statut algorithme |
| `GET` | `/api/admin/affectations/resultats` | Résultats affectation |
| `PUT` | `/api/admin/affectations/{id}` | Modification manuelle |
| `DELETE` | `/api/admin/affectations/all` | Suppression toutes affectations |

### 📄 Admin (Tickets PDF)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/admin/tickets/generer-tous` | Génération batch tickets |
| `POST` | `/api/admin/tickets/eleves/{id}/regenerer` | Régénérer ticket élève |
| `GET` | `/api/admin/tickets/eleves/{id}/ticket` | Télécharger ticket (admin) |

### 🔐 SuperAdmin

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `DELETE` | `/api/superadmin/system/purge-all` | Suppression totale données |
| `GET` | `/api/superadmin/system/stats` | Statistiques globales |
| `GET` | `/api/superadmin/system/export/eleves` | Export CSV élèves |
| `GET` | `/api/superadmin/system/export/activites` | Export CSV activités |
| `GET` | `/api/superadmin/system/export/salles` | Export CSV salles |
| `GET` | `/api/superadmin/system/export/creneaux` | Export CSV créneaux |
| `GET` | `/api/superadmin/system/export/lycees` | Export CSV lycées |
| `GET` | `/api/superadmin/system/export/voeux` | Export CSV vœux |
| `GET` | `/api/superadmin/system/export/sessions` | Export CSV sessions |
| `GET` | `/api/superadmin/system/export/affectations` | Export CSV affectations |
| `GET` | `/api/superadmin/system/export/all` | Export ZIP complet |

---

## 🖥️ Frontend - Structure Complète

### Modules et Composants

```
frontend/src/app/
├── app.module.ts                     # NgModule principal
├── app-routing.module.ts             # Routing
│
├── components/
│   ├── identification/               # ✅ Étape 1 (public)
│   ├── confirmation-identite/        # ✅ Étape 2 (public)
│   ├── formulaire-voeux/            # ✅ Étape 3 (public)
│   ├── recapitulatif-voeux/         # ✅ Étape 3.5 (public)
│   ├── confirmation-soumission/     # ✅ Étape 4 (public)
│   │
│   ├── login/                       # 🔐 Connexion admin
│   ├── admin-dashboard/             # 🔐 Dashboard (standalone)
│   ├── admin-lycees/                # 🔐 CRUD Lycées
│   ├── admin-eleves/                # 🔐 CRUD Élèves + Import CSV
│   ├── admin-activites/             # 🔐 CRUD Activités + Import
│   ├── admin-salles/                # 🔐 CRUD Salles + Import
│   ├── admin-creneaux/              # 🔐 CRUD Créneaux
│   ├── admin-sessions/              # 🔐 Génération sessions
│   ├── admin-affectations/          # 🔐 Algorithme + Résultats
│   ├── admin-tickets/               # 🔐 Génération PDF batch
│   └── system-settings/             # 🔐 SuperAdmin (purge + export)
│
├── services/
│   ├── voeux.service.ts             # Gestion workflow élève
│   ├── auth.service.ts              # Authentification JWT
│   ├── lycee.service.ts             # CRUD Lycées
│   ├── eleve.service.ts             # CRUD Élèves
│   ├── activite.service.ts          # CRUD Activités
│   ├── salle.service.ts             # CRUD Salles
│   ├── creneau.service.ts           # CRUD Créneaux
│   ├── session.service.ts           # Génération sessions
│   ├── affectation.service.ts       # Algorithme + Résultats
│   ├── ticket.service.ts            # Génération PDF
│   ├── system-settings.service.ts   # SuperAdmin + Export
│   └── csv-import.service.ts        # Import CSV
│
├── guards/
│   ├── auth.guard.ts                # Protection routes admin
│   ├── voeux.guard.ts               # Protection workflow élève
│   └── superadmin.guard.ts          # Protection SuperAdmin
│
├── interceptors/
│   └── jwt.interceptor.ts           # Injection token JWT
│
└── models/
    └── *.interface.ts               # Interfaces TypeScript
```

### Routing

```typescript
Routes:
  / (public)
    ├── '' → identification
    ├── 'confirmation-identite' → confirmation-identite (guard: VoeuxGuard)
    ├── 'formulaire-voeux' → formulaire-voeux (guard: VoeuxGuard)
    ├── 'recapitulatif-voeux' → recapitulatif-voeux (guard: VoeuxGuard)
    └── 'confirmation-soumission' → confirmation-soumission (guard: VoeuxGuard)

  /admin (secured)
    ├── 'login' → login
    ├── 'dashboard' → admin-dashboard (guard: AuthGuard)
    ├── 'lycees' → admin-lycees (guard: AuthGuard)
    ├── 'eleves' → admin-eleves (guard: AuthGuard)
    ├── 'activites' → admin-activites (guard: AuthGuard)
    ├── 'salles' → admin-salles (guard: AuthGuard)
    ├── 'creneaux' → admin-creneaux (guard: AuthGuard)
    ├── 'sessions' → admin-sessions (guard: AuthGuard)
    ├── 'affectations' → admin-affectations (guard: AuthGuard)
    ├── 'tickets' → admin-tickets (guard: AuthGuard)
    └── 'system-settings' → system-settings (guard: SuperAdminGuard)
```

---

## 🚀 Installation et Démarrage

### Prérequis

- **Docker** : 20.10+
- **Docker Compose** : 2.0+

### Option 1 : Démarrage rapide avec Docker (Recommandé)

```bash
# Cloner le projet
git clone <repo-url>
cd POC

# Ou manuellement
docker-compose up --build -d
```

**Services disponibles :**
- Frontend : http://localhost
- Backend API : http://localhost:8080
- Base de données : localhost:5434

### Option 2 : Développement local

#### Backend

```bash
cd backend

# Installer les dépendances
./mvnw clean install

# Lancer le backend
./mvnw spring-boot:run
```

#### Frontend

```bash
cd frontend

# Installer les dépendances
npm install

# Lancer le serveur de développement
npm start
```

### Générer des données de test

```bash
cd backend
./generate-test-voeux.sh
```

**Ce script crée :**
- 16 créneaux (4 par demi-journée)
- 112 activités (28 × 4 demi-journées)
- 8 salles
- 10 lycées
- 50 élèves avec 250 vœux
- Compte SuperAdmin

---

## 🧪 Tests et Validation

### Tests manuels

#### Test workflow élève

1. Ouvrir http://localhost
2. Saisir ID : `1` et Nom : `DUPONT`
3. Remplir les 5 vœux
4. Vérifier la soumission

#### Test interface admin

1. Ouvrir http://localhost/admin
2. Se connecter : `admin@fesup.fr` / `admin123!`
3. Tester chaque module CRUD
4. Lancer la génération de sessions
5. Lancer l'algorithme
6. Générer les tickets PDF

### Documentation de test

- **TEST_EXPORT_CSV.md** : Tests export CSV
- **TEST_GUIDE_MULTI_DAY.md** : Tests structure multi-jours

---

## 📚 Documentation Complémentaire

| Document | Description |
|----------|-------------|
| `documentation_technique.txt` | Architecture détaillée + API complète |
| `documentation_utilisateur.txt` | Guide utilisateur + FAQ |
| `README-DOCKER.md` | Guide Docker Compose |

---

## 🔒 Sécurité

### Authentification

- **JWT** (JSON Web Tokens) pour l'authentification admin
- **Rôles** : ADMIN, SUPERADMIN
- **Protection routes** : `@PreAuthorize("hasRole('...')")`

### Validation

**Côté Backend (Spring Boot) :**
- Validation Bean Validation (`@Valid`, `@NotNull`, etc.)
- Validation métier complexe dans `VoeuxService`
- Vérification des doublons
- Vérification des types d'activités
- Vérification des demi-journées

**Côté Frontend (Angular) :**
- Validateurs personnalisés
- Validation temps réel
- Guards de protection de routes

### Isolation Élève

- Authentification par couple **ID + Nom**
- Pas de session persistante
- Téléchargement PDF sécurisé (vérification ID + Nom)

---

## ⚙️ Services Métier Clés

### 1. VoeuxService

**Responsabilité** : Validation complexe des vœux

**Règles métier :**
1. Vœux 1-2 : CONFERENCE uniquement, différentes
2. Vœux 3-4-5 : CONFERENCE | TABLE_RONDE | FLASH_METIER, différents
3. Pas de doublon entre les 5 vœux
4. Demi-journée cohérente
5. Élève ne peut soumettre qu'une seule fois

### 2. SessionGenerationService

**Responsabilité** : Création automatique des sessions

**Logique :**
1. Analyser les vœux par activité
2. Calculer `nbSessions = ceil(nbVoeux / capacitéSalle)`
3. Garanties minimales par type :
   - CONFERENCE : 1 si vœux > 0
   - TABLE_RONDE : 1 si vœux ≥ 5
   - FLASH_METIER : 1 si vœux ≥ 3
4. Assigner automatiquement salle + créneau

### 3. AffectationService (Timefold AI)

**Responsabilité** : Optimisation des affectations

**Contraintes Hard (obligatoires) :**
- Un élève ne peut avoir 2 activités au même créneau
- Capacité des sessions respectée
- Demi-journée respectée

**Contraintes Soft (à maximiser) :**
- Priorité des vœux (vœu 1 >> vœu 2 >> vœu 3 ...)
- Satisfaction globale

**Temps de calcul :** 30 secondes

### 4. BatchPdfService

**Responsabilité** : Génération asynchrone des tickets PDF

**Processus :**
1. Chargement des élèves avec affectations
2. Génération PDF avec PDFBox (en-tête + tableau)
3. Sauvegarde sur disque : `/app/tickets/{année}/eleve_{id}.pdf`
4. Création entrée `Ticket` en BDD

**Performance :** ~1-2 secondes par ticket

### 5. CsvExportService

**Responsabilité** : Export des données en CSV/ZIP

**Formats :**
- CSV : UTF-8 avec BOM, séparateur virgule
- ZIP : contient les 8 CSV (eleves, activites, salles, creneaux, lycees, voeux, sessions, affectations)

---

## 📈 Statistiques et Monitoring

### Dashboard Admin

- Nombre total d'élèves
- Vœux soumis
- Sessions créées
- Affectations générées
- Tickets PDF générés
- Graphiques : répartition par lycée, type activité, taux remplissage

### SuperAdmin

- Statistiques globales système
- Export CSV de toutes les entités
- Export ZIP complet
- Purge totale des données

---

## 🐛 Dépannage

### Backend ne démarre pas

```bash
# Vérifier les logs
docker logs fesup-backend

# Redémarrer
docker-compose restart backend
```

### Frontend ne s'affiche pas

```bash
# Vérifier les logs
docker logs fesup-frontend

# Vérifier Nginx
docker exec -it fesup-frontend cat /etc/nginx/conf.d/default.conf
```

### Base de données inaccessible

```bash
# Vérifier le conteneur
docker ps | grep postgres

# Se connecter à la DB
docker exec -it fesup-postgres psql -U fesup_user -d fesup_db
```

### Réinitialisation complète

```bash
# Supprimer tous les conteneurs et volumes
docker-compose down -v

# Redémarrer
./start.sh
```

---

## 🚧 Évolutions Futures

- [ ] Authentification élève par email
- [ ] Notifications email (soumission vœux, planning disponible)
- [ ] Export Excel (.xlsx) en plus du CSV
- [ ] Planification exports automatiques (cron)
- [ ] API publique pour intégrations externes
- [ ] Multi-tenant (plusieurs forums simultanés)
- [ ] Gestion des préférences élèves (accessibilité, etc.)
- [ ] Statistiques avancées avec graphiques interactifs
- [ ] Module de messagerie admin ↔ élèves

---

## 👥 Support et Contact

**Documentation :**
- Technique : `documentation_technique.txt`
- Utilisateur : `documentation_utilisateur.txt`

**Logs :**
```bash
docker-compose logs -f [backend|frontend|postgres]
```

**Sauvegarde DB :**
```bash
docker exec fesup-postgres pg_dump -U fesup_user fesup_db > backup.sql
```

**Restauration DB :**
```bash
docker exec -i fesup-postgres psql -U fesup_user fesup_db < backup.sql
```

---

## 📜 Licence

Projet FESUP - Tous droits réservés
