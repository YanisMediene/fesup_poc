-- Script d'initialisation de la base de données FESUP
-- À exécuter après la création des tables par Hibernate

-- ============================================
-- DONNÉES DE TEST : LYCÉES
-- ============================================

INSERT INTO lycees (nom, ville, code_postal) 
VALUES 
  ('Lycée Victor Hugo', 'Paris', '75015'),
  ('Lycée Voltaire', 'Paris', '75011'),
  ('Lycée Descartes', 'Tours', '37000'),
  ('Lycée Molière', 'Paris', '75016');

-- ============================================
-- DONNÉES DE TEST : ÉLÈVES
-- ============================================

INSERT INTO eleves (nom, prenom, id_national, lycee_id, demi_journee, voeux_soumis, date_soumission) 
VALUES 
  ('DUPONT', 'Jean', '120890177FA', (SELECT id FROM lycees WHERE nom = 'Lycée Victor Hugo'), 'MATIN', false, NULL),
  ('MARTIN', 'Sophie', '120113325CK', (SELECT id FROM lycees WHERE nom = 'Lycée Voltaire'), 'APRES_MIDI', false, NULL),
  ('BERNARD', 'Lucas', '120234556AB', (SELECT id FROM lycees WHERE nom = 'Lycée Descartes'), 'MATIN', false, NULL),
  ('PETIT', 'Emma', '120345667BC', (SELECT id FROM lycees WHERE nom = 'Lycée Molière'), 'APRES_MIDI', false, NULL),
  ('DURAND', 'Thomas', '120456778CD', (SELECT id FROM lycees WHERE nom = 'Lycée Victor Hugo'), 'MATIN', false, NULL);

-- ============================================
-- DONNÉES DE TEST : ACTIVITÉS - MATIN
-- ============================================

-- CONFÉRENCES MATIN (19)
INSERT INTO activites (titre, description, type, demi_journee, capacite_max)
VALUES 
  ('Intelligence Artificielle et Machine Learning', 'Découvrez les fondamentaux de l''IA et ses applications concrètes', 'CONFERENCE', 'MATIN', 30),
  ('Cybersécurité : Les enjeux de demain', 'Protection des données et sécurité informatique', 'CONFERENCE', 'MATIN', 30),
  ('Développement Web Moderne', 'React, Angular, Vue.js : tour d''horizon des frameworks', 'CONFERENCE', 'MATIN', 30),
  ('Cloud Computing et DevOps', 'AWS, Azure, Google Cloud : infrastructure moderne', 'CONFERENCE', 'MATIN', 30),
  ('Big Data et Data Science', 'Analyse de données massives et visualisation', 'CONFERENCE', 'MATIN', 30),
  ('Blockchain et Cryptomonnaies', 'Technologies blockchain et applications', 'CONFERENCE', 'MATIN', 30),
  ('Internet des Objets (IoT)', 'Objets connectés et domotique', 'CONFERENCE', 'MATIN', 30),
  ('Réalité Virtuelle et Augmentée', 'VR/AR dans le monde professionnel', 'CONFERENCE', 'MATIN', 30),
  ('Robotique et Automatisation', 'Robots industriels et intelligence artificielle', 'CONFERENCE', 'MATIN', 30),
  ('Systèmes Embarqués', 'Programmation embarquée et Arduino', 'CONFERENCE', 'MATIN', 30),
  ('Architecture Logicielle', 'Design patterns et bonnes pratiques', 'CONFERENCE', 'MATIN', 30),
  ('Génie Logiciel Agile', 'Scrum, Kanban et méthodes agiles', 'CONFERENCE', 'MATIN', 30),
  ('Bases de Données NoSQL', 'MongoDB, Redis, Cassandra', 'CONFERENCE', 'MATIN', 30),
  ('Intelligence d''Affaires (BI)', 'Business Intelligence et décisionnel', 'CONFERENCE', 'MATIN', 30),
  ('Sécurité des Applications Web', 'OWASP et tests de pénétration', 'CONFERENCE', 'MATIN', 30),
  ('Développement Mobile', 'iOS, Android, React Native, Flutter', 'CONFERENCE', 'MATIN', 30),
  ('Green IT et Numérique Responsable', 'Informatique éco-responsable', 'CONFERENCE', 'MATIN', 30),
  ('Technologies Quantiques', 'Introduction à l''informatique quantique', 'CONFERENCE', 'MATIN', 30),
  ('5G et Réseaux du Futur', 'Évolution des télécommunications', 'CONFERENCE', 'MATIN', 30);

-- TABLES RONDES MATIN (5)
INSERT INTO activites (titre, description, type, demi_journee, capacite_max)
VALUES 
  ('Table Ronde : Métiers du Digital', 'Échange avec des professionnels du numérique', 'TABLE_RONDE', 'MATIN', 20),
  ('Table Ronde : Entrepreneuriat Tech', 'Créer sa startup dans la tech', 'TABLE_RONDE', 'MATIN', 20),
  ('Table Ronde : Femmes dans la Tech', 'Diversité et inclusion dans l''IT', 'TABLE_RONDE', 'MATIN', 20),
  ('Table Ronde : Innovation et R&D', 'Recherche et développement en entreprise', 'TABLE_RONDE', 'MATIN', 20),
  ('Table Ronde : Transition Numérique', 'Transformation digitale des entreprises', 'TABLE_RONDE', 'MATIN', 20);

-- FLASHS MÉTIERS MATIN (5)
INSERT INTO activites (titre, description, type, demi_journee, capacite_max)
VALUES 
  ('Flash Métier : Développeur Full-Stack', 'Présentation du métier de développeur', 'FLASH_METIER', 'MATIN', 15),
  ('Flash Métier : Data Scientist', 'Carrière en science des données', 'FLASH_METIER', 'MATIN', 15),
  ('Flash Métier : DevOps Engineer', 'Le rôle du DevOps en entreprise', 'FLASH_METIER', 'MATIN', 15),
  ('Flash Métier : UX/UI Designer', 'Design d''interfaces utilisateur', 'FLASH_METIER', 'MATIN', 15),
  ('Flash Métier : Chef de Projet IT', 'Management de projets informatiques', 'FLASH_METIER', 'MATIN', 15);

-- ============================================
-- DONNÉES DE TEST : ACTIVITÉS - APRÈS-MIDI
-- ============================================

-- CONFÉRENCES APRÈS-MIDI (19)
INSERT INTO activites (titre, description, type, demi_journee, capacite_max)
VALUES 
  ('Intelligence Artificielle et Machine Learning', 'Découvrez les fondamentaux de l''IA et ses applications concrètes', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Cybersécurité : Les enjeux de demain', 'Protection des données et sécurité informatique', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Développement Web Moderne', 'React, Angular, Vue.js : tour d''horizon des frameworks', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Cloud Computing et DevOps', 'AWS, Azure, Google Cloud : infrastructure moderne', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Big Data et Data Science', 'Analyse de données massives et visualisation', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Blockchain et Cryptomonnaies', 'Technologies blockchain et applications', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Internet des Objets (IoT)', 'Objets connectés et domotique', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Réalité Virtuelle et Augmentée', 'VR/AR dans le monde professionnel', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Robotique et Automatisation', 'Robots industriels et intelligence artificielle', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Systèmes Embarqués', 'Programmation embarquée et Arduino', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Architecture Logicielle', 'Design patterns et bonnes pratiques', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Génie Logiciel Agile', 'Scrum, Kanban et méthodes agiles', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Bases de Données NoSQL', 'MongoDB, Redis, Cassandra', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Intelligence d''Affaires (BI)', 'Business Intelligence et décisionnel', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Sécurité des Applications Web', 'OWASP et tests de pénétration', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Développement Mobile', 'iOS, Android, React Native, Flutter', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Green IT et Numérique Responsable', 'Informatique éco-responsable', 'CONFERENCE', 'APRES_MIDI', 30),
  ('Technologies Quantiques', 'Introduction à l''informatique quantique', 'CONFERENCE', 'APRES_MIDI', 30),
  ('5G et Réseaux du Futur', 'Évolution des télécommunications', 'CONFERENCE', 'APRES_MIDI', 30);

-- TABLES RONDES APRÈS-MIDI (5)
INSERT INTO activites (titre, description, type, demi_journee, capacite_max)
VALUES 
  ('Table Ronde : Métiers du Digital', 'Échange avec des professionnels du numérique', 'TABLE_RONDE', 'APRES_MIDI', 20),
  ('Table Ronde : Entrepreneuriat Tech', 'Créer sa startup dans la tech', 'TABLE_RONDE', 'APRES_MIDI', 20),
  ('Table Ronde : Femmes dans la Tech', 'Diversité et inclusion dans l''IT', 'TABLE_RONDE', 'APRES_MIDI', 20),
  ('Table Ronde : Innovation et R&D', 'Recherche et développement en entreprise', 'TABLE_RONDE', 'APRES_MIDI', 20),
  ('Table Ronde : Transition Numérique', 'Transformation digitale des entreprises', 'TABLE_RONDE', 'APRES_MIDI', 20);

-- FLASHS MÉTIERS APRÈS-MIDI (5)
INSERT INTO activites (titre, description, type, demi_journee, capacite_max)
VALUES 
  ('Flash Métier : Développeur Full-Stack', 'Présentation du métier de développeur', 'FLASH_METIER', 'APRES_MIDI', 15),
  ('Flash Métier : Data Scientist', 'Carrière en science des données', 'FLASH_METIER', 'APRES_MIDI', 15),
  ('Flash Métier : DevOps Engineer', 'Le rôle du DevOps en entreprise', 'FLASH_METIER', 'APRES_MIDI', 15),
  ('Flash Métier : UX/UI Designer', 'Design d''interfaces utilisateur', 'FLASH_METIER', 'APRES_MIDI', 15),
  ('Flash Métier : Chef de Projet IT', 'Management de projets informatiques', 'FLASH_METIER', 'APRES_MIDI', 15);

-- ============================================
-- VÉRIFICATION DES DONNÉES INSÉRÉES
-- ============================================

-- Compter les élèves
SELECT COUNT(*) as total_eleves FROM eleves;

-- Compter les activités par type et demi-journée
SELECT 
  type, 
  demi_journee, 
  COUNT(*) as nombre 
FROM activites 
GROUP BY type, demi_journee 
ORDER BY demi_journee, type;

-- Afficher les élèves
SELECT id, nom, prenom, lycee, demi_journee, voeux_soumis FROM eleves;
