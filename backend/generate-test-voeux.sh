#!/bin/bash

# Script de génération COMPLÈTE de données de test avec ID NATIONAL
# Version 2.0 - Compatible avec l'architecture idNational (String, 50 chars)

echo "🎲 Génération COMPLÈTE des données de test (Version 2.0 - ID National)"
echo "=========================================================================="

# Configuration BDD
DB_NAME="fesup_db"
DB_USER="postgres"
DB_PASSWORD="postgres"
DB_PORT="5434"

# Nombre d'élèves à générer (modifiable)
NB_ELEVES=${1:-50}  # Par défaut 50 élèves

echo "📊 Génération de toutes les données (lycées, salles, activités, $NB_ELEVES élèves avec vœux)..."

# Exécution du script SQL
PGPASSWORD=$DB_PASSWORD psql -U $DB_USER -p $DB_PORT -d $DB_NAME << EOF

-- ============================================
-- SCRIPT DE GÉNÉRATION COMPLÈTE DE DONNÉES
-- Version 2.0 avec ID NATIONAL
-- ============================================

DO \$\$
DECLARE
    v_nb_eleves INTEGER := $NB_ELEVES; -- Nombre d'élèves à générer
    v_eleve_id BIGINT;
    v_lycee_id BIGINT;
    v_demi_journee VARCHAR(20);
    v_nom VARCHAR(100);
    v_prenom VARCHAR(100);
    v_id_national VARCHAR(50);
    
    -- IDs des activités par type et demi-journée
    v_conferences_jour1_matin BIGINT[];
    v_conferences_jour1_apres_midi BIGINT[];
    v_conferences_jour2_matin BIGINT[];
    v_conferences_jour2_apres_midi BIGINT[];
    v_tables_rondes_jour1_matin BIGINT[];
    v_tables_rondes_jour1_apres_midi BIGINT[];
    v_tables_rondes_jour2_matin BIGINT[];
    v_tables_rondes_jour2_apres_midi BIGINT[];
    v_flash_jour1_matin BIGINT[];
    v_flash_jour1_apres_midi BIGINT[];
    v_flash_jour2_matin BIGINT[];
    v_flash_jour2_apres_midi BIGINT[];
    
    -- Vœux sélectionnés
    v_conf1 BIGINT;
    v_conf2 BIGINT;
    v_voeu3 BIGINT;
    v_voeu4 BIGINT;
    v_voeu5 BIGINT;
    v_random INTEGER;
    
    -- Listes de noms/prénoms pour variation
    v_noms TEXT[] := ARRAY['MARTIN', 'BERNARD', 'THOMAS', 'PETIT', 'ROBERT', 'RICHARD', 'DURAND', 'DUBOIS', 'MOREAU', 'LAURENT', 'SIMON', 'MICHEL', 'LEFEBVRE', 'LEROY', 'ROUX', 'DAVID', 'BERTRAND', 'MOREL', 'FOURNIER', 'GIRARD', 'BONNET', 'DUPONT', 'LAMBERT', 'FONTAINE', 'ROUSSEAU', 'VINCENT', 'MULLER', 'LEFEVRE', 'FAURE', 'ANDRE'];
    v_prenoms TEXT[] := ARRAY['Jean', 'Marie', 'Pierre', 'Sophie', 'Luc', 'Julie', 'Paul', 'Emma', 'Marc', 'Léa', 'Thomas', 'Chloé', 'Nicolas', 'Sarah', 'Alexandre', 'Laura', 'Julien', 'Manon', 'Antoine', 'Camille', 'Maxime', 'Clara', 'Lucas', 'Inès', 'Hugo', 'Lisa', 'Nathan', 'Jade', 'Louis', 'Zoé'];
    
    -- Lettres pour ID national (2 lettres aléatoires à la fin)
    v_lettres TEXT := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    v_lettre1 CHAR(1);
    v_lettre2 CHAR(1);
    
    i INTEGER;
BEGIN
    -- ============================================
    -- 1. SUPPRIMER LES ANCIENNES DONNÉES
    -- ============================================
    RAISE NOTICE '🗑️  Suppression des anciennes données...';
    DELETE FROM affectations;
    DELETE FROM sessions;
    DELETE FROM voeux;
    DELETE FROM eleves;
    DELETE FROM activites;
    DELETE FROM salles;
    DELETE FROM creneaux;
    DELETE FROM lycees;
    
    -- ============================================
    -- 2. CRÉER LES LYCÉES
    -- ============================================
    RAISE NOTICE '🏫 Création des lycées...';
    
    INSERT INTO lycees (nom, ville, code_postal) VALUES
        ('Lycée Victor Hugo', 'Paris', '75001'),
        ('Lycée Pasteur', 'Strasbourg', '67000'),
        ('Lycée Condorcet', 'Bordeaux', '33000'),
        ('Lycée Jean Moulin', 'Lyon', '69000'),
        ('Lycée Descartes', 'Tours', '37000'),
        ('Lycée Montaigne', 'Bordeaux', '33800'),
        ('Lycée Voltaire', 'Paris', '75011'),
        ('Lycée Rousseau', 'Montmorency', '95160');
    
    RAISE NOTICE '✅ % lycées créés', (SELECT COUNT(*) FROM lycees);
    
    -- ============================================
    -- 3. CRÉER LES SALLES
    -- ============================================
    RAISE NOTICE '🏢 Création des salles...';
    
    INSERT INTO salles (nom, capacite, batiment, equipements) VALUES
        ('Amphithéâtre A', 150, 'Bâtiment Principal', 'Vidéoprojecteur 4K, Sono, Micro HF, Écran géant'),
        ('Amphithéâtre B', 120, 'Bâtiment Principal', 'Vidéoprojecteur, Sono, Écran'),
        ('Salle 101', 30, 'Bâtiment A', 'Tableau blanc, Vidéoprojecteur'),
        ('Salle 102', 30, 'Bâtiment A', 'Tableau blanc, Vidéoprojecteur'),
        ('Salle 103', 35, 'Bâtiment A', 'Tableau blanc, Vidéoprojecteur, Sono'),
        ('Salle 201', 40, 'Bâtiment B', 'Écran interactif, Sono'),
        ('Salle 202', 40, 'Bâtiment B', 'Écran interactif, Sono'),
        ('Salle 203', 25, 'Bâtiment B', 'Tableau blanc, Vidéoprojecteur'),
        ('Salle de Conférence', 80, 'Bâtiment Principal', 'Vidéoprojecteur 4K, Sono, Micro HF'),
        ('Salle Polyvalente', 60, 'Bâtiment C', 'Sono, Vidéoprojecteur');
    
    RAISE NOTICE '✅ % salles créées', (SELECT COUNT(*) FROM salles);
    
    -- ============================================
    -- 4. CRÉER LES CRÉNEAUX (16 créneaux sur 2 jours)
    -- ============================================
    RAISE NOTICE '🕐 Création des créneaux...';
    
    INSERT INTO creneaux (libelle, heure_debut, heure_fin, demi_journee) VALUES
        -- JOUR 1 - MATIN
        ('Jour 1 - 08h00 - 09h00', '08:00', '09:00', 'JOUR1_MATIN'),
        ('Jour 1 - 09h00 - 10h00', '09:00', '10:00', 'JOUR1_MATIN'),
        ('Jour 1 - 10h00 - 11h00', '10:00', '11:00', 'JOUR1_MATIN'),
        ('Jour 1 - 11h00 - 12h00', '11:00', '12:00', 'JOUR1_MATIN'),
        -- JOUR 1 - APRÈS-MIDI
        ('Jour 1 - 14h00 - 15h00', '14:00', '15:00', 'JOUR1_APRES_MIDI'),
        ('Jour 1 - 15h00 - 16h00', '15:00', '16:00', 'JOUR1_APRES_MIDI'),
        ('Jour 1 - 16h00 - 17h00', '16:00', '17:00', 'JOUR1_APRES_MIDI'),
        ('Jour 1 - 17h00 - 18h00', '17:00', '18:00', 'JOUR1_APRES_MIDI'),
        -- JOUR 2 - MATIN
        ('Jour 2 - 08h00 - 09h00', '08:00', '09:00', 'JOUR2_MATIN'),
        ('Jour 2 - 09h00 - 10h00', '09:00', '10:00', 'JOUR2_MATIN'),
        ('Jour 2 - 10h00 - 11h00', '10:00', '11:00', 'JOUR2_MATIN'),
        ('Jour 2 - 11h00 - 12h00', '11:00', '12:00', 'JOUR2_MATIN'),
        -- JOUR 2 - APRÈS-MIDI
        ('Jour 2 - 14h00 - 15h00', '14:00', '15:00', 'JOUR2_APRES_MIDI'),
        ('Jour 2 - 15h00 - 16h00', '15:00', '16:00', 'JOUR2_APRES_MIDI'),
        ('Jour 2 - 16h00 - 17h00', '16:00', '17:00', 'JOUR2_APRES_MIDI'),
        ('Jour 2 - 17h00 - 18h00', '17:00', '18:00', 'JOUR2_APRES_MIDI');
    
    RAISE NOTICE '✅ % créneaux créés', (SELECT COUNT(*) FROM creneaux);
    
    -- ============================================
    -- 5. CRÉER LES ACTIVITÉS
    -- ============================================
    RAISE NOTICE '🎯 Création des activités...';
    
    -- CONFÉRENCES JOUR 1 - MATIN (19)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Intelligence Artificielle et Machine Learning', 'Découverte des fondamentaux de l''IA et ses applications concrètes dans l''industrie', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Cybersécurité : Les enjeux de demain', 'Protection des systèmes informatiques et sensibilisation aux menaces', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Développement Web Moderne', 'React, Angular, Vue.js : panorama des frameworks actuels', 'CONFERENCE', 'JOUR1_MATIN', 35),
        ('Cloud Computing et DevOps', 'AWS, Azure, Docker, Kubernetes : l''infrastructure moderne', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Data Science et Big Data', 'Analyse de données massives et Machine Learning appliqué', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Blockchain et Cryptomonnaies', 'Technologies décentralisées et applications futures', 'CONFERENCE', 'JOUR1_MATIN', 25),
        ('Internet des Objets (IoT)', 'Capteurs connectés et smart cities', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Réalité Virtuelle et Augmentée', 'Immersion digitale et applications professionnelles', 'CONFERENCE', 'JOUR1_MATIN', 25),
        ('Green IT : Informatique Responsable', 'Développement durable et numérique', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('UX/UI Design', 'Expérience utilisateur et interfaces modernes', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Développement Mobile', 'iOS, Android : créer des applications mobiles', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Intelligence Artificielle Générative', 'ChatGPT, Midjourney : IA créative', 'CONFERENCE', 'JOUR1_MATIN', 35),
        ('Robotique et Automatisation', 'Robots industriels et cobotique', 'CONFERENCE', 'JOUR1_MATIN', 25),
        ('Quantum Computing', 'Introduction à l''informatique quantique', 'CONFERENCE', 'JOUR1_MATIN', 20),
        ('Systèmes Embarqués', 'Programmation bas niveau et hardware', 'CONFERENCE', 'JOUR1_MATIN', 25),
        ('E-commerce et Marketing Digital', 'Vendre en ligne et stratégies digitales', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Jeux Vidéo : Création et Développement', 'Unity, Unreal Engine, game design', 'CONFERENCE', 'JOUR1_MATIN', 30),
        ('Réseaux et Télécommunications', '5G, fibre optique, infrastructure réseau', 'CONFERENCE', 'JOUR1_MATIN', 25),
        ('Ethical Hacking', 'Sécurité offensive et tests de pénétration', 'CONFERENCE', 'JOUR1_MATIN', 30);
    
    -- CONFÉRENCES JOUR 1 - APRÈS-MIDI (19)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Intelligence Artificielle et Machine Learning', 'Découverte des fondamentaux de l''IA et ses applications concrètes dans l''industrie', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Cybersécurité : Les enjeux de demain', 'Protection des systèmes informatiques et sensibilisation aux menaces', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Développement Web Moderne', 'React, Angular, Vue.js : panorama des frameworks actuels', 'CONFERENCE', 'JOUR1_APRES_MIDI', 35),
        ('Cloud Computing et DevOps', 'AWS, Azure, Docker, Kubernetes : l''infrastructure moderne', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Data Science et Big Data', 'Analyse de données massives et Machine Learning appliqué', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Blockchain et Cryptomonnaies', 'Technologies décentralisées et applications futures', 'CONFERENCE', 'JOUR1_APRES_MIDI', 25),
        ('Internet des Objets (IoT)', 'Capteurs connectés et smart cities', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Réalité Virtuelle et Augmentée', 'Immersion digitale et applications professionnelles', 'CONFERENCE', 'JOUR1_APRES_MIDI', 25),
        ('Green IT : Informatique Responsable', 'Développement durable et numérique', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('UX/UI Design', 'Expérience utilisateur et interfaces modernes', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Développement Mobile', 'iOS, Android : créer des applications mobiles', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Intelligence Artificielle Générative', 'ChatGPT, Midjourney : IA créative', 'CONFERENCE', 'JOUR1_APRES_MIDI', 35),
        ('Robotique et Automatisation', 'Robots industriels et cobotique', 'CONFERENCE', 'JOUR1_APRES_MIDI', 25),
        ('Quantum Computing', 'Introduction à l''informatique quantique', 'CONFERENCE', 'JOUR1_APRES_MIDI', 20),
        ('Systèmes Embarqués', 'Programmation bas niveau et hardware', 'CONFERENCE', 'JOUR1_APRES_MIDI', 25),
        ('E-commerce et Marketing Digital', 'Vendre en ligne et stratégies digitales', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Jeux Vidéo : Création et Développement', 'Unity, Unreal Engine, game design', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30),
        ('Réseaux et Télécommunications', '5G, fibre optique, infrastructure réseau', 'CONFERENCE', 'JOUR1_APRES_MIDI', 25),
        ('Ethical Hacking', 'Sécurité offensive et tests de pénétration', 'CONFERENCE', 'JOUR1_APRES_MIDI', 30);
    
    -- CONFÉRENCES JOUR 2 - MATIN (19)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Intelligence Artificielle et Machine Learning', 'Découverte des fondamentaux de l''IA et ses applications concrètes dans l''industrie', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Cybersécurité : Les enjeux de demain', 'Protection des systèmes informatiques et sensibilisation aux menaces', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Développement Web Moderne', 'React, Angular, Vue.js : panorama des frameworks actuels', 'CONFERENCE', 'JOUR2_MATIN', 35),
        ('Cloud Computing et DevOps', 'AWS, Azure, Docker, Kubernetes : l''infrastructure moderne', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Data Science et Big Data', 'Analyse de données massives et Machine Learning appliqué', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Blockchain et Cryptomonnaies', 'Technologies décentralisées et applications futures', 'CONFERENCE', 'JOUR2_MATIN', 25),
        ('Internet des Objets (IoT)', 'Capteurs connectés et smart cities', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Réalité Virtuelle et Augmentée', 'Immersion digitale et applications professionnelles', 'CONFERENCE', 'JOUR2_MATIN', 25),
        ('Green IT : Informatique Responsable', 'Développement durable et numérique', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('UX/UI Design', 'Expérience utilisateur et interfaces modernes', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Développement Mobile', 'iOS, Android : créer des applications mobiles', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Intelligence Artificielle Générative', 'ChatGPT, Midjourney : IA créative', 'CONFERENCE', 'JOUR2_MATIN', 35),
        ('Robotique et Automatisation', 'Robots industriels et cobotique', 'CONFERENCE', 'JOUR2_MATIN', 25),
        ('Quantum Computing', 'Introduction à l''informatique quantique', 'CONFERENCE', 'JOUR2_MATIN', 20),
        ('Systèmes Embarqués', 'Programmation bas niveau et hardware', 'CONFERENCE', 'JOUR2_MATIN', 25),
        ('E-commerce et Marketing Digital', 'Vendre en ligne et stratégies digitales', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Jeux Vidéo : Création et Développement', 'Unity, Unreal Engine, game design', 'CONFERENCE', 'JOUR2_MATIN', 30),
        ('Réseaux et Télécommunications', '5G, fibre optique, infrastructure réseau', 'CONFERENCE', 'JOUR2_MATIN', 25),
        ('Ethical Hacking', 'Sécurité offensive et tests de pénétration', 'CONFERENCE', 'JOUR2_MATIN', 30);
    
    -- CONFÉRENCES JOUR 2 - APRÈS-MIDI (19)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Intelligence Artificielle et Machine Learning', 'Découverte des fondamentaux de l''IA et ses applications concrètes dans l''industrie', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Cybersécurité : Les enjeux de demain', 'Protection des systèmes informatiques et sensibilisation aux menaces', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Développement Web Moderne', 'React, Angular, Vue.js : panorama des frameworks actuels', 'CONFERENCE', 'JOUR2_APRES_MIDI', 35),
        ('Cloud Computing et DevOps', 'AWS, Azure, Docker, Kubernetes : l''infrastructure moderne', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Data Science et Big Data', 'Analyse de données massives et Machine Learning appliqué', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Blockchain et Cryptomonnaies', 'Technologies décentralisées et applications futures', 'CONFERENCE', 'JOUR2_APRES_MIDI', 25),
        ('Internet des Objets (IoT)', 'Capteurs connectés et smart cities', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Réalité Virtuelle et Augmentée', 'Immersion digitale et applications professionnelles', 'CONFERENCE', 'JOUR2_APRES_MIDI', 25),
        ('Green IT : Informatique Responsable', 'Développement durable et numérique', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('UX/UI Design', 'Expérience utilisateur et interfaces modernes', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Développement Mobile', 'iOS, Android : créer des applications mobiles', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Intelligence Artificielle Générative', 'ChatGPT, Midjourney : IA créative', 'CONFERENCE', 'JOUR2_APRES_MIDI', 35),
        ('Robotique et Automatisation', 'Robots industriels et cobotique', 'CONFERENCE', 'JOUR2_APRES_MIDI', 25),
        ('Quantum Computing', 'Introduction à l''informatique quantique', 'CONFERENCE', 'JOUR2_APRES_MIDI', 20),
        ('Systèmes Embarqués', 'Programmation bas niveau et hardware', 'CONFERENCE', 'JOUR2_APRES_MIDI', 25),
        ('E-commerce et Marketing Digital', 'Vendre en ligne et stratégies digitales', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Jeux Vidéo : Création et Développement', 'Unity, Unreal Engine, game design', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30),
        ('Réseaux et Télécommunications', '5G, fibre optique, infrastructure réseau', 'CONFERENCE', 'JOUR2_APRES_MIDI', 25),
        ('Ethical Hacking', 'Sécurité offensive et tests de pénétration', 'CONFERENCE', 'JOUR2_APRES_MIDI', 30);
    
    -- TABLES RONDES JOUR 1 - MATIN (3)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Table Ronde : Métiers du Digital', 'Échanges avec des professionnels du numérique', 'TABLE_RONDE', 'JOUR1_MATIN', 40),
        ('Table Ronde : Entrepreneuriat Tech', 'Créer sa startup dans le digital', 'TABLE_RONDE', 'JOUR1_MATIN', 40),
        ('Table Ronde : Femmes dans la Tech', 'Diversité et inclusion dans l''informatique', 'TABLE_RONDE', 'JOUR1_MATIN', 40);
    
    -- TABLES RONDES JOUR 1 - APRÈS-MIDI (3)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Table Ronde : Métiers du Digital', 'Échanges avec des professionnels du numérique', 'TABLE_RONDE', 'JOUR1_APRES_MIDI', 40),
        ('Table Ronde : Entrepreneuriat Tech', 'Créer sa startup dans le digital', 'TABLE_RONDE', 'JOUR1_APRES_MIDI', 40),
        ('Table Ronde : Femmes dans la Tech', 'Diversité et inclusion dans l''informatique', 'TABLE_RONDE', 'JOUR1_APRES_MIDI', 40);
    
    -- TABLES RONDES JOUR 2 - MATIN (3)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Table Ronde : Métiers du Digital', 'Échanges avec des professionnels du numérique', 'TABLE_RONDE', 'JOUR2_MATIN', 40),
        ('Table Ronde : Entrepreneuriat Tech', 'Créer sa startup dans le digital', 'TABLE_RONDE', 'JOUR2_MATIN', 40),
        ('Table Ronde : Femmes dans la Tech', 'Diversité et inclusion dans l''informatique', 'TABLE_RONDE', 'JOUR2_MATIN', 40);
    
    -- TABLES RONDES JOUR 2 - APRÈS-MIDI (3)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Table Ronde : Métiers du Digital', 'Échanges avec des professionnels du numérique', 'TABLE_RONDE', 'JOUR2_APRES_MIDI', 40),
        ('Table Ronde : Entrepreneuriat Tech', 'Créer sa startup dans le digital', 'TABLE_RONDE', 'JOUR2_APRES_MIDI', 40),
        ('Table Ronde : Femmes dans la Tech', 'Diversité et inclusion dans l''informatique', 'TABLE_RONDE', 'JOUR2_APRES_MIDI', 40);
    
    -- FLASH MÉTIERS JOUR 1 - MATIN (6)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Flash Métier : Développeur Full-Stack', 'Présentation rapide du métier de développeur web', 'FLASH_METIER', 'JOUR1_MATIN', 20),
        ('Flash Métier : Data Scientist', 'Découverte du métier d''analyste de données', 'FLASH_METIER', 'JOUR1_MATIN', 20),
        ('Flash Métier : DevOps Engineer', 'Le métier de l''infrastructure automatisée', 'FLASH_METIER', 'JOUR1_MATIN', 20),
        ('Flash Métier : Architecte Cloud', 'Concevoir des infrastructures cloud', 'FLASH_METIER', 'JOUR1_MATIN', 20),
        ('Flash Métier : Product Owner', 'Gérer des produits digitaux', 'FLASH_METIER', 'JOUR1_MATIN', 20),
        ('Flash Métier : UX Designer', 'Créer des expériences utilisateur', 'FLASH_METIER', 'JOUR1_MATIN', 20);
    
    -- FLASH MÉTIERS JOUR 1 - APRÈS-MIDI (6)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Flash Métier : Développeur Full-Stack', 'Présentation rapide du métier de développeur web', 'FLASH_METIER', 'JOUR1_APRES_MIDI', 20),
        ('Flash Métier : Data Scientist', 'Découverte du métier d''analyste de données', 'FLASH_METIER', 'JOUR1_APRES_MIDI', 20),
        ('Flash Métier : DevOps Engineer', 'Le métier de l''infrastructure automatisée', 'FLASH_METIER', 'JOUR1_APRES_MIDI', 20),
        ('Flash Métier : Architecte Cloud', 'Concevoir des infrastructures cloud', 'FLASH_METIER', 'JOUR1_APRES_MIDI', 20),
        ('Flash Métier : Product Owner', 'Gérer des produits digitaux', 'FLASH_METIER', 'JOUR1_APRES_MIDI', 20),
        ('Flash Métier : UX Designer', 'Créer des expériences utilisateur', 'FLASH_METIER', 'JOUR1_APRES_MIDI', 20);
    
    -- FLASH MÉTIERS JOUR 2 - MATIN (6)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Flash Métier : Développeur Full-Stack', 'Présentation rapide du métier de développeur web', 'FLASH_METIER', 'JOUR2_MATIN', 20),
        ('Flash Métier : Data Scientist', 'Découverte du métier d''analyste de données', 'FLASH_METIER', 'JOUR2_MATIN', 20),
        ('Flash Métier : DevOps Engineer', 'Le métier de l''infrastructure automatisée', 'FLASH_METIER', 'JOUR2_MATIN', 20),
        ('Flash Métier : Architecte Cloud', 'Concevoir des infrastructures cloud', 'FLASH_METIER', 'JOUR2_MATIN', 20),
        ('Flash Métier : Product Owner', 'Gérer des produits digitaux', 'FLASH_METIER', 'JOUR2_MATIN', 20),
        ('Flash Métier : UX Designer', 'Créer des expériences utilisateur', 'FLASH_METIER', 'JOUR2_MATIN', 20);
    
    -- FLASH MÉTIERS JOUR 2 - APRÈS-MIDI (6)
    INSERT INTO activites (titre, description, type, demi_journee, capacite_max) VALUES
        ('Flash Métier : Développeur Full-Stack', 'Présentation rapide du métier de développeur web', 'FLASH_METIER', 'JOUR2_APRES_MIDI', 20),
        ('Flash Métier : Data Scientist', 'Découverte du métier d''analyste de données', 'FLASH_METIER', 'JOUR2_APRES_MIDI', 20),
        ('Flash Métier : DevOps Engineer', 'Le métier de l''infrastructure automatisée', 'FLASH_METIER', 'JOUR2_APRES_MIDI', 20),
        ('Flash Métier : Architecte Cloud', 'Concevoir des infrastructures cloud', 'FLASH_METIER', 'JOUR2_APRES_MIDI', 20),
        ('Flash Métier : Product Owner', 'Gérer des produits digitaux', 'FLASH_METIER', 'JOUR2_APRES_MIDI', 20),
        ('Flash Métier : UX Designer', 'Créer des expériences utilisateur', 'FLASH_METIER', 'JOUR2_APRES_MIDI', 20);
    
    RAISE NOTICE '✅ % activités créées', (SELECT COUNT(*) FROM activites);
    RAISE NOTICE '   • JOUR1_MATIN: % conférences, % tables rondes, % flash métiers', 
        (SELECT COUNT(*) FROM activites WHERE type='CONFERENCE' AND demi_journee='JOUR1_MATIN'),
        (SELECT COUNT(*) FROM activites WHERE type='TABLE_RONDE' AND demi_journee='JOUR1_MATIN'),
        (SELECT COUNT(*) FROM activites WHERE type='FLASH_METIER' AND demi_journee='JOUR1_MATIN');
    RAISE NOTICE '   • JOUR1_APRES_MIDI: % conférences, % tables rondes, % flash métiers',
        (SELECT COUNT(*) FROM activites WHERE type='CONFERENCE' AND demi_journee='JOUR1_APRES_MIDI'),
        (SELECT COUNT(*) FROM activites WHERE type='TABLE_RONDE' AND demi_journee='JOUR1_APRES_MIDI'),
        (SELECT COUNT(*) FROM activites WHERE type='FLASH_METIER' AND demi_journee='JOUR1_APRES_MIDI');
    RAISE NOTICE '   • JOUR2_MATIN: % conférences, % tables rondes, % flash métiers', 
        (SELECT COUNT(*) FROM activites WHERE type='CONFERENCE' AND demi_journee='JOUR2_MATIN'),
        (SELECT COUNT(*) FROM activites WHERE type='TABLE_RONDE' AND demi_journee='JOUR2_MATIN'),
        (SELECT COUNT(*) FROM activites WHERE type='FLASH_METIER' AND demi_journee='JOUR2_MATIN');
    RAISE NOTICE '   • JOUR2_APRES_MIDI: % conférences, % tables rondes, % flash métiers',
        (SELECT COUNT(*) FROM activites WHERE type='CONFERENCE' AND demi_journee='JOUR2_APRES_MIDI'),
        (SELECT COUNT(*) FROM activites WHERE type='TABLE_RONDE' AND demi_journee='JOUR2_APRES_MIDI'),
        (SELECT COUNT(*) FROM activites WHERE type='FLASH_METIER' AND demi_journee='JOUR2_APRES_MIDI');
    
    -- ============================================
    -- 6. CRÉER LES ÉLÈVES AVEC VŒUX ET ID NATIONAL
    -- ============================================
    RAISE NOTICE '';
    RAISE NOTICE '👥 Création de % élèves avec ID National et vœux aléatoires...', v_nb_eleves;
    
    -- Récupérer les IDs des activités par type et demi-journée
    SELECT ARRAY_AGG(id) INTO v_conferences_jour1_matin 
    FROM activites WHERE type = 'CONFERENCE' AND demi_journee = 'JOUR1_MATIN';
    
    SELECT ARRAY_AGG(id) INTO v_conferences_jour1_apres_midi 
    FROM activites WHERE type = 'CONFERENCE' AND demi_journee = 'JOUR1_APRES_MIDI';
    
    SELECT ARRAY_AGG(id) INTO v_conferences_jour2_matin 
    FROM activites WHERE type = 'CONFERENCE' AND demi_journee = 'JOUR2_MATIN';
    
    SELECT ARRAY_AGG(id) INTO v_conferences_jour2_apres_midi 
    FROM activites WHERE type = 'CONFERENCE' AND demi_journee = 'JOUR2_APRES_MIDI';
    
    SELECT ARRAY_AGG(id) INTO v_tables_rondes_jour1_matin 
    FROM activites WHERE type = 'TABLE_RONDE' AND demi_journee = 'JOUR1_MATIN';
    
    SELECT ARRAY_AGG(id) INTO v_tables_rondes_jour1_apres_midi 
    FROM activites WHERE type = 'TABLE_RONDE' AND demi_journee = 'JOUR1_APRES_MIDI';
    
    SELECT ARRAY_AGG(id) INTO v_tables_rondes_jour2_matin 
    FROM activites WHERE type = 'TABLE_RONDE' AND demi_journee = 'JOUR2_MATIN';
    
    SELECT ARRAY_AGG(id) INTO v_tables_rondes_jour2_apres_midi 
    FROM activites WHERE type = 'TABLE_RONDE' AND demi_journee = 'JOUR2_APRES_MIDI';
    
    SELECT ARRAY_AGG(id) INTO v_flash_jour1_matin 
    FROM activites WHERE type = 'FLASH_METIER' AND demi_journee = 'JOUR1_MATIN';
    
    SELECT ARRAY_AGG(id) INTO v_flash_jour1_apres_midi 
    FROM activites WHERE type = 'FLASH_METIER' AND demi_journee = 'JOUR1_APRES_MIDI';
    
    SELECT ARRAY_AGG(id) INTO v_flash_jour2_matin 
    FROM activites WHERE type = 'FLASH_METIER' AND demi_journee = 'JOUR2_MATIN';
    
    SELECT ARRAY_AGG(id) INTO v_flash_jour2_apres_midi 
    FROM activites WHERE type = 'FLASH_METIER' AND demi_journee = 'JOUR2_APRES_MIDI';
    
    -- Boucle de génération des élèves
    FOR i IN 1..v_nb_eleves LOOP
        
        -- Sélection aléatoire des paramètres
        v_lycee_id := (SELECT id FROM lycees ORDER BY RANDOM() LIMIT 1);
        
        -- Répartition équitable sur 4 demi-journées
        v_random := floor(random() * 4)::int;
        v_demi_journee := CASE v_random
            WHEN 0 THEN 'JOUR1_MATIN'
            WHEN 1 THEN 'JOUR1_APRES_MIDI'
            WHEN 2 THEN 'JOUR2_MATIN'
            ELSE 'JOUR2_APRES_MIDI'
        END;
        
        v_nom := v_noms[1 + floor(random() * array_length(v_noms, 1))::int];
        v_prenom := v_prenoms[1 + floor(random() * array_length(v_prenoms, 1))::int];
        
        -- Générer un ID National unique au format : 12 chiffres + 2 lettres majuscules
        -- Exemple : 120890177FA, 220456889BC, etc.
        LOOP
            v_lettre1 := substring(v_lettres from (1 + floor(random() * 26)::int) for 1);
            v_lettre2 := substring(v_lettres from (1 + floor(random() * 26)::int) for 1);
            
            v_id_national := 
                lpad((floor(random() * 1000000000000)::bigint)::text, 12, '0') || 
                v_lettre1 || v_lettre2;
            
            -- Vérifier l'unicité de l'ID National
            EXIT WHEN NOT EXISTS (SELECT 1 FROM eleves WHERE id_national = v_id_national);
        END LOOP;
        
        -- Créer l'élève avec ID National (vœux soumis pour permettre l'affectation)
        INSERT INTO eleves (nom, prenom, id_national, lycee_id, demi_journee, voeux_soumis, date_soumission)
        VALUES (v_nom, v_prenom, v_id_national, v_lycee_id, v_demi_journee, true, NOW())
        RETURNING id INTO v_eleve_id;
        
        -- Sélectionner 2 conférences différentes pour vœux 1 et 2
        IF v_demi_journee = 'JOUR1_MATIN' THEN
            v_conf1 := v_conferences_jour1_matin[1 + floor(random() * array_length(v_conferences_jour1_matin, 1))::int];
            LOOP
                v_conf2 := v_conferences_jour1_matin[1 + floor(random() * array_length(v_conferences_jour1_matin, 1))::int];
                EXIT WHEN v_conf2 != v_conf1;
            END LOOP;
            
            v_random := floor(random() * 3)::int;
            CASE v_random
                WHEN 0 THEN 
                    LOOP
                        v_voeu3 := v_conferences_jour1_matin[1 + floor(random() * array_length(v_conferences_jour1_matin, 1))::int];
                        EXIT WHEN v_voeu3 != v_conf1 AND v_voeu3 != v_conf2;
                    END LOOP;
                WHEN 1 THEN 
                    v_voeu3 := v_tables_rondes_jour1_matin[1 + floor(random() * array_length(v_tables_rondes_jour1_matin, 1))::int];
                ELSE 
                    v_voeu3 := v_flash_jour1_matin[1 + floor(random() * array_length(v_flash_jour1_matin, 1))::int];
            END CASE;
            
            v_random := floor(random() * 3)::int;
            LOOP
                CASE v_random
                    WHEN 0 THEN 
                        v_voeu4 := v_conferences_jour1_matin[1 + floor(random() * array_length(v_conferences_jour1_matin, 1))::int];
                    WHEN 1 THEN 
                        v_voeu4 := v_tables_rondes_jour1_matin[1 + floor(random() * array_length(v_tables_rondes_jour1_matin, 1))::int];
                    ELSE 
                        v_voeu4 := v_flash_jour1_matin[1 + floor(random() * array_length(v_flash_jour1_matin, 1))::int];
                END CASE;
                EXIT WHEN v_voeu4 != v_conf1 AND v_voeu4 != v_conf2 AND v_voeu4 != v_voeu3;
            END LOOP;
            
            v_random := floor(random() * 3)::int;
            LOOP
                CASE v_random
                    WHEN 0 THEN 
                        v_voeu5 := v_conferences_jour1_matin[1 + floor(random() * array_length(v_conferences_jour1_matin, 1))::int];
                    WHEN 1 THEN 
                        v_voeu5 := v_tables_rondes_jour1_matin[1 + floor(random() * array_length(v_tables_rondes_jour1_matin, 1))::int];
                    ELSE 
                        v_voeu5 := v_flash_jour1_matin[1 + floor(random() * array_length(v_flash_jour1_matin, 1))::int];
                END CASE;
                EXIT WHEN v_voeu5 != v_conf1 AND v_voeu5 != v_conf2 AND v_voeu5 != v_voeu3 AND v_voeu5 != v_voeu4;
            END LOOP;
            
        ELSIF v_demi_journee = 'JOUR1_APRES_MIDI' THEN
            v_conf1 := v_conferences_jour1_apres_midi[1 + floor(random() * array_length(v_conferences_jour1_apres_midi, 1))::int];
            LOOP
                v_conf2 := v_conferences_jour1_apres_midi[1 + floor(random() * array_length(v_conferences_jour1_apres_midi, 1))::int];
                EXIT WHEN v_conf2 != v_conf1;
            END LOOP;
            
            v_random := floor(random() * 3)::int;
            CASE v_random
                WHEN 0 THEN 
                    LOOP
                        v_voeu3 := v_conferences_jour1_apres_midi[1 + floor(random() * array_length(v_conferences_jour1_apres_midi, 1))::int];
                        EXIT WHEN v_voeu3 != v_conf1 AND v_voeu3 != v_conf2;
                    END LOOP;
                WHEN 1 THEN 
                    v_voeu3 := v_tables_rondes_jour1_apres_midi[1 + floor(random() * array_length(v_tables_rondes_jour1_apres_midi, 1))::int];
                ELSE 
                    v_voeu3 := v_flash_jour1_apres_midi[1 + floor(random() * array_length(v_flash_jour1_apres_midi, 1))::int];
            END CASE;
            
            v_random := floor(random() * 3)::int;
            LOOP
                CASE v_random
                    WHEN 0 THEN 
                        v_voeu4 := v_conferences_jour1_apres_midi[1 + floor(random() * array_length(v_conferences_jour1_apres_midi, 1))::int];
                    WHEN 1 THEN 
                        v_voeu4 := v_tables_rondes_jour1_apres_midi[1 + floor(random() * array_length(v_tables_rondes_jour1_apres_midi, 1))::int];
                    ELSE 
                        v_voeu4 := v_flash_jour1_apres_midi[1 + floor(random() * array_length(v_flash_jour1_apres_midi, 1))::int];
                END CASE;
                EXIT WHEN v_voeu4 != v_conf1 AND v_voeu4 != v_conf2 AND v_voeu4 != v_voeu3;
            END LOOP;
            
            v_random := floor(random() * 3)::int;
            LOOP
                CASE v_random
                    WHEN 0 THEN 
                        v_voeu5 := v_conferences_jour1_apres_midi[1 + floor(random() * array_length(v_conferences_jour1_apres_midi, 1))::int];
                    WHEN 1 THEN 
                        v_voeu5 := v_tables_rondes_jour1_apres_midi[1 + floor(random() * array_length(v_tables_rondes_jour1_apres_midi, 1))::int];
                    ELSE 
                        v_voeu5 := v_flash_jour1_apres_midi[1 + floor(random() * array_length(v_flash_jour1_apres_midi, 1))::int];
                END CASE;
                EXIT WHEN v_voeu5 != v_conf1 AND v_voeu5 != v_conf2 AND v_voeu5 != v_voeu3 AND v_voeu5 != v_voeu4;
            END LOOP;
            
        ELSIF v_demi_journee = 'JOUR2_MATIN' THEN
            v_conf1 := v_conferences_jour2_matin[1 + floor(random() * array_length(v_conferences_jour2_matin, 1))::int];
            LOOP
                v_conf2 := v_conferences_jour2_matin[1 + floor(random() * array_length(v_conferences_jour2_matin, 1))::int];
                EXIT WHEN v_conf2 != v_conf1;
            END LOOP;
            
            v_random := floor(random() * 3)::int;
            CASE v_random
                WHEN 0 THEN 
                    LOOP
                        v_voeu3 := v_conferences_jour2_matin[1 + floor(random() * array_length(v_conferences_jour2_matin, 1))::int];
                        EXIT WHEN v_voeu3 != v_conf1 AND v_voeu3 != v_conf2;
                    END LOOP;
                WHEN 1 THEN 
                    v_voeu3 := v_tables_rondes_jour2_matin[1 + floor(random() * array_length(v_tables_rondes_jour2_matin, 1))::int];
                ELSE 
                    v_voeu3 := v_flash_jour2_matin[1 + floor(random() * array_length(v_flash_jour2_matin, 1))::int];
            END CASE;
            
            v_random := floor(random() * 3)::int;
            LOOP
                CASE v_random
                    WHEN 0 THEN 
                        v_voeu4 := v_conferences_jour2_matin[1 + floor(random() * array_length(v_conferences_jour2_matin, 1))::int];
                    WHEN 1 THEN 
                        v_voeu4 := v_tables_rondes_jour2_matin[1 + floor(random() * array_length(v_tables_rondes_jour2_matin, 1))::int];
                    ELSE 
                        v_voeu4 := v_flash_jour2_matin[1 + floor(random() * array_length(v_flash_jour2_matin, 1))::int];
                END CASE;
                EXIT WHEN v_voeu4 != v_conf1 AND v_voeu4 != v_conf2 AND v_voeu4 != v_voeu3;
            END LOOP;
            
            v_random := floor(random() * 3)::int;
            LOOP
                CASE v_random
                    WHEN 0 THEN 
                        v_voeu5 := v_conferences_jour2_matin[1 + floor(random() * array_length(v_conferences_jour2_matin, 1))::int];
                    WHEN 1 THEN 
                        v_voeu5 := v_tables_rondes_jour2_matin[1 + floor(random() * array_length(v_tables_rondes_jour2_matin, 1))::int];
                    ELSE 
                        v_voeu5 := v_flash_jour2_matin[1 + floor(random() * array_length(v_flash_jour2_matin, 1))::int];
                END CASE;
                EXIT WHEN v_voeu5 != v_conf1 AND v_voeu5 != v_conf2 AND v_voeu5 != v_voeu3 AND v_voeu5 != v_voeu4;
            END LOOP;
            
        ELSE -- JOUR2_APRES_MIDI
            v_conf1 := v_conferences_jour2_apres_midi[1 + floor(random() * array_length(v_conferences_jour2_apres_midi, 1))::int];
            LOOP
                v_conf2 := v_conferences_jour2_apres_midi[1 + floor(random() * array_length(v_conferences_jour2_apres_midi, 1))::int];
                EXIT WHEN v_conf2 != v_conf1;
            END LOOP;
            
            v_random := floor(random() * 3)::int;
            CASE v_random
                WHEN 0 THEN 
                    LOOP
                        v_voeu3 := v_conferences_jour2_apres_midi[1 + floor(random() * array_length(v_conferences_jour2_apres_midi, 1))::int];
                        EXIT WHEN v_voeu3 != v_conf1 AND v_voeu3 != v_conf2;
                    END LOOP;
                WHEN 1 THEN 
                    v_voeu3 := v_tables_rondes_jour2_apres_midi[1 + floor(random() * array_length(v_tables_rondes_jour2_apres_midi, 1))::int];
                ELSE 
                    v_voeu3 := v_flash_jour2_apres_midi[1 + floor(random() * array_length(v_flash_jour2_apres_midi, 1))::int];
            END CASE;
            
            v_random := floor(random() * 3)::int;
            LOOP
                CASE v_random
                    WHEN 0 THEN 
                        v_voeu4 := v_conferences_jour2_apres_midi[1 + floor(random() * array_length(v_conferences_jour2_apres_midi, 1))::int];
                    WHEN 1 THEN 
                        v_voeu4 := v_tables_rondes_jour2_apres_midi[1 + floor(random() * array_length(v_tables_rondes_jour2_apres_midi, 1))::int];
                    ELSE 
                        v_voeu4 := v_flash_jour2_apres_midi[1 + floor(random() * array_length(v_flash_jour2_apres_midi, 1))::int];
                END CASE;
                EXIT WHEN v_voeu4 != v_conf1 AND v_voeu4 != v_conf2 AND v_voeu4 != v_voeu3;
            END LOOP;
            
            v_random := floor(random() * 3)::int;
            LOOP
                CASE v_random
                    WHEN 0 THEN 
                        v_voeu5 := v_conferences_jour2_apres_midi[1 + floor(random() * array_length(v_conferences_jour2_apres_midi, 1))::int];
                    WHEN 1 THEN 
                        v_voeu5 := v_tables_rondes_jour2_apres_midi[1 + floor(random() * array_length(v_tables_rondes_jour2_apres_midi, 1))::int];
                    ELSE 
                        v_voeu5 := v_flash_jour2_apres_midi[1 + floor(random() * array_length(v_flash_jour2_apres_midi, 1))::int];
                END CASE;
                EXIT WHEN v_voeu5 != v_conf1 AND v_voeu5 != v_conf2 AND v_voeu5 != v_voeu3 AND v_voeu5 != v_voeu4;
            END LOOP;
        END IF;
        
        -- Insérer les 5 vœux
        INSERT INTO voeux (eleve_id, activite_id, priorite, type_voeu) VALUES
            (v_eleve_id, v_conf1, 1, 'VOEU_1_2'),
            (v_eleve_id, v_conf2, 2, 'VOEU_1_2'),
            (v_eleve_id, v_voeu3, 3, 'VOEU_3_4_5'),
            (v_eleve_id, v_voeu4, 4, 'VOEU_3_4_5'),
            (v_eleve_id, v_voeu5, 5, 'VOEU_3_4_5');
        
        IF i % 10 = 0 THEN
            RAISE NOTICE '  ✓ % élèves générés...', i;
        END IF;
    END LOOP;
    
    RAISE NOTICE '';
    RAISE NOTICE '✅ Génération terminée !';
    RAISE NOTICE '========================================';
    RAISE NOTICE '📊 STATISTIQUES FINALES :';
    RAISE NOTICE '   • Lycées: %', (SELECT COUNT(*) FROM lycees);
    RAISE NOTICE '   • Salles: %', (SELECT COUNT(*) FROM salles);
    RAISE NOTICE '   • Créneaux: %', (SELECT COUNT(*) FROM creneaux);
    RAISE NOTICE '   • Activités: %', (SELECT COUNT(*) FROM activites);
    RAISE NOTICE '   • Élèves: %', (SELECT COUNT(*) FROM eleves);
    RAISE NOTICE '   • Vœux: %', (SELECT COUNT(*) FROM voeux);
    RAISE NOTICE '   • JOUR1_MATIN: % élèves', (SELECT COUNT(*) FROM eleves WHERE demi_journee = 'JOUR1_MATIN');
    RAISE NOTICE '   • JOUR1_APRES_MIDI: % élèves', (SELECT COUNT(*) FROM eleves WHERE demi_journee = 'JOUR1_APRES_MIDI');
    RAISE NOTICE '   • JOUR2_MATIN: % élèves', (SELECT COUNT(*) FROM eleves WHERE demi_journee = 'JOUR2_MATIN');
    RAISE NOTICE '   • JOUR2_APRES_MIDI: % élèves', (SELECT COUNT(*) FROM eleves WHERE demi_journee = 'JOUR2_APRES_MIDI');
    RAISE NOTICE '========================================';
    
END\$\$;

-- Afficher un échantillon d'élèves avec leurs ID Nationaux
SELECT 
    e.id,
    e.nom,
    e.prenom,
    e.id_national,
    e.demi_journee,
    l.nom as lycee,
    COUNT(v.id) as nb_voeux
FROM eleves e
LEFT JOIN lycees l ON e.lycee_id = l.id
LEFT JOIN voeux v ON v.eleve_id = e.id
GROUP BY e.id, e.nom, e.prenom, e.id_national, e.demi_journee, l.nom
ORDER BY e.id
LIMIT 10;

EOF

echo ""
echo "✅ Génération complète terminée avec ID National !"
echo ""
echo "🎯 Vous pouvez maintenant :"
echo "   1. Créer des sessions dans 'Gestion Sessions'"
echo "   2. Lancer l'algorithme dans 'Affectations'"
echo ""
echo "💡 Pour modifier le nombre d'élèves :"
echo "   ./generate-test-voeux-v2.sh 100  # Génère 100 élèves"
echo ""
echo "🔑 Format ID National : 12 chiffres + 2 lettres (ex: 120890177FA)"
