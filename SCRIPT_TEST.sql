-- SCRIPT_TEST.sql

-- Réinitialisation complète de la base de données
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- Nettoyer les données de test avant d'insérer de nouvelles données
DELETE FROM voeux;
UPDATE eleves SET voeux_soumis = false, date_soumission = NULL;

-- Voir tous les élèves
SELECT * FROM eleves;

SELECT * FROM users;

SELECT * FROM activites;

SELECT * FROM lycees;

SELECT * FROM salles;

SELECT * FROM creneaux;


-- Voir toutes les activités du MATIN
SELECT * FROM activites WHERE demi_journee = 'MATIN';

-- Compter les activités par type et demi-journée
SELECT type, demi_journee, COUNT(*) 
FROM activites 
GROUP BY type, demi_journee 
ORDER BY demi_journee, type;

-- Voir les vœux soumis (si des élèves ont déjà soumis)
SELECT e.nom, e.prenom, a.titre, v.priorite, v.type_voeu
FROM voeux v
JOIN eleves e ON v.eleve_id = e.id
JOIN activites a ON v.activite_id = a.id
ORDER BY e.nom, v.priorite;

SELECT 
    COUNT(DISTINCT e.id) as total_eleves,
    COUNT(DISTINCT aff.eleve_id) as eleves_affectes,
    COUNT(DISTINCT e.id) - COUNT(DISTINCT aff.eleve_id) as eleves_non_affectes
FROM eleves e
LEFT JOIN affectations aff ON aff.eleve_id = e.id;

SELECT 
    'Vœu 1 ou 2 satisfait' as statut,
    COUNT(DISTINCT e.id) as nb_eleves
FROM eleves e
JOIN affectations aff ON aff.eleve_id = e.id
JOIN sessions s ON s.id = aff.session_id
JOIN voeux v ON v.eleve_id = e.id AND v.activite_id = s.activite_id AND v.priorite <= 2
UNION ALL
SELECT 
    'Aucun vœu 1 ou 2 satisfait' as statut,
    COUNT(DISTINCT e.id) as nb_eleves
FROM eleves e
WHERE NOT EXISTS (
    SELECT 1 FROM affectations aff
    JOIN sessions s ON s.id = aff.session_id
    JOIN voeux v ON v.eleve_id = e.id AND v.activite_id = s.activite_id AND v.priorite <= 2
    WHERE aff.eleve_id = e.id
);

SELECT 
    SUM(CASE WHEN v.priorite = 1 THEN 1 ELSE 0 END) as voeu1_satisfait,
    SUM(CASE WHEN v.priorite = 2 THEN 1 ELSE 0 END) as voeu2_satisfait
FROM eleves e
JOIN affectations aff ON aff.eleve_id = e.id
JOIN sessions s ON s.id = aff.session_id
JOIN voeux v ON v.eleve_id = e.id AND v.activite_id = s.activite_id AND v.priorite <= 2;