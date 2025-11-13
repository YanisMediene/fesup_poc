#!/usr/bin/env python3
"""
Script de conversion des données FESUP 2026 au format des templates.

Ce script convertit :
1. Les fichiers CSV des lycées (Fauriel et Brassens) -> eleves.csv
2. Le fichier texte des activités -> activites.csv

Usage:
    python convert_data.py
"""

import csv
import re
from pathlib import Path


def convert_eleves():
    """
    Convertit les fichiers CSV des lycées au format eleves_template.csv

    Format source: Etablissement;Nom de famille;Prénom;Id National;Lib. Structure
    Format cible: nom,prenom,idNational,lycee,ville,codePostal,demiJournee
    """
    print("=== Conversion des fichiers élèves ===")

    # Fichiers sources
    fichiers_lycees = [
        "LGT_Fauriel_FESUP_2026.csv",
        "Lycée_Georges_Brassens_FESUP_2026.csv",
    ]

    eleves = []
    stats = {}

    for fichier in fichiers_lycees:
        fichier_path = Path(__file__).parent / fichier

        if not fichier_path.exists():
            print(f"⚠️  Fichier non trouvé : {fichier}")
            continue

        print(f"📖 Lecture de {fichier}...")

        with open(fichier_path, "r", encoding="utf-8") as f:
            # Lecture avec séparateur point-virgule
            reader = csv.DictReader(f, delimiter=";")

            for row in reader:
                etablissement = row["Etablissement"].strip()
                nom = row["Nom de famille"].strip()
                prenom = row["Prénom"].strip()
                id_national = row["Id National"].strip()
                structure = row["Lib. Structure"].strip()

                # Déterminer le lycée et la ville
                if "Fauriel" in etablissement:
                    lycee = "Lycée Fauriel"
                    ville = "Saint-Étienne"
                    code_postal = "42000"
                elif "Brassens" in etablissement:
                    lycee = "Lycée Georges Brassens"
                    ville = "Rive-de-Gier"
                    code_postal = "42800"
                else:
                    lycee = etablissement
                    ville = "Saint-Étienne"
                    code_postal = "42000"

                # Déterminer la demi-journée en fonction de la classe
                # On va alterner pour répartir équitablement
                # Classes STMG -> JOUR1_MATIN
                # Classes 1RE 1-4 -> JOUR1_APRES_MIDI
                # Classes 1RE 5-8 -> JOUR2_MATIN
                # Classes 1RE 9+ et GEN -> JOUR2_APRES_MIDI

                if "STMG" in structure:
                    demi_journee = "JOUR1_MATIN"
                elif any(
                    x in structure
                    for x in ["1RE 1", "1RE 2", "1RE 3", "1RE 4", "1GEN1", "1GEN2"]
                ):
                    demi_journee = "JOUR1_APRES_MIDI"
                elif any(
                    x in structure
                    for x in ["1RE 5", "1RE 6", "1RE 7", "1RE 8", "1GEN3", "1GEN4"]
                ):
                    demi_journee = "JOUR2_MATIN"
                else:
                    demi_journee = "JOUR2_APRES_MIDI"

                eleves.append(
                    {
                        "nom": nom,
                        "prenom": prenom,
                        "idNational": id_national,
                        "lycee": lycee,
                        "ville": ville,
                        "codePostal": code_postal,
                        "demiJournee": demi_journee,
                    }
                )

                # Stats
                key = f"{lycee} - {demi_journee}"
                stats[key] = stats.get(key, 0) + 1

    # Écriture du fichier de sortie
    fichier_sortie = Path(__file__).parent / "eleves_converted.csv"

    with open(fichier_sortie, "w", encoding="utf-8", newline="") as f:
        fieldnames = [
            "nom",
            "prenom",
            "idNational",
            "lycee",
            "ville",
            "codePostal",
            "demiJournee",
        ]
        writer = csv.DictWriter(f, fieldnames=fieldnames)

        writer.writeheader()
        writer.writerows(eleves)

    print(f"✅ {len(eleves)} élèves convertis -> {fichier_sortie.name}")
    print("\n📊 Répartition par lycée et demi-journée :")
    for key, count in sorted(stats.items()):
        print(f"   {key}: {count} élèves")

    return len(eleves)


def convert_activites():
    """
    Convertit le fichier texte des activités au format activites_template.csv

    Format source: Fichier texte avec sections "19 Conférences", "5 Tables rondes", "5 Flashs métiers"
    Format cible: titre,description,type,demiJournee,capaciteMax
    """
    print("\n=== Conversion du fichier activités ===")

    fichier_source = (
        Path(__file__).parent
        / "Liste_conferences-tablesronde_s-flashmétiers_FESUP_2026.txt"
    )

    if not fichier_source.exists():
        print(f"⚠️  Fichier non trouvé : {fichier_source.name}")
        return 0

    print(f"📖 Lecture de {fichier_source.name}...")

    activites = []
    current_type = None
    demi_journees = [
        "JOUR1_MATIN",
        "JOUR1_APRES_MIDI",
        "JOUR2_MATIN",
        "JOUR2_APRES_MIDI",
    ]
    dj_index = 0

    with open(fichier_source, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()

            if not line:
                continue

            # Détection des sections
            if "Conférences" in line:
                current_type = "CONFERENCE"
                print("📚 Section Conférences détectée")
                continue
            elif "Tables rondes" in line or (
                "Table ronde" in line and current_type is None
            ):
                current_type = "TABLE_RONDE"
                print("🎤 Section Tables rondes détectée")
                continue
            elif "Flashs métiers" in line or (
                "Flash" in line and current_type != "TABLE_RONDE"
            ):
                current_type = "FLASH_METIER"
                print("⚡ Section Flashs métiers détectée")
                continue

            # Ignorer les lignes de comptage (ex: "19 Conférences")
            if re.match(r"^\d+\s+", line):
                continue

            # Traiter les lignes d'activités
            if current_type and line:
                # Nettoyer le titre
                titre = line
                titre = re.sub(r"^Table ronde:\s*", "", titre)
                titre = re.sub(r"^Table ronde\s+", "", titre)
                titre = re.sub(r"^flash métier\s+", "", titre, flags=re.IGNORECASE)

                # Nettoyer les parties inutiles
                titre = re.sub(r"\s*\(animation par des professionnels\)\s*", "", titre)
                titre = re.sub(r"\s*\(animation par des étudiants\)\s*", "", titre)

                # Nettoyer tous les guillemets
                titre = titre.replace('"', "").replace("'", "").strip()

                # Première lettre en majuscule si nécessaire
                if titre and not titre[0].isupper():
                    titre = titre[0].upper() + titre[1:]

                # Générer une description
                if current_type == "CONFERENCE":
                    description = f"Conférence sur {titre.lower()}"
                elif current_type == "TABLE_RONDE":
                    description = f"Table ronde : {titre}"
                else:  # FLASH_METIER
                    description = (
                        f"Flash métier présentant le métier de {titre.lower()}"
                    )

                # Déterminer la capacité max selon le type
                if current_type == "CONFERENCE":
                    capacite_max = 40
                elif current_type == "TABLE_RONDE":
                    capacite_max = 25
                else:  # FLASH_METIER
                    capacite_max = 20

                # Alterner les demi-journées pour répartir les activités
                demi_journee = demi_journees[dj_index % len(demi_journees)]
                dj_index += 1

                activites.append(
                    {
                        "titre": titre,
                        "description": description,
                        "type": current_type,
                        "demiJournee": demi_journee,
                        "capaciteMax": capacite_max,
                    }
                )

    # Écriture du fichier de sortie
    fichier_sortie = Path(__file__).parent / "activites_converted.csv"

    with open(fichier_sortie, "w", encoding="utf-8", newline="") as f:
        fieldnames = ["titre", "description", "type", "demiJournee", "capaciteMax"]
        writer = csv.DictWriter(f, fieldnames=fieldnames)

        writer.writeheader()
        writer.writerows(activites)

    print(f"✅ {len(activites)} activités converties -> {fichier_sortie.name}")

    # Stats par type
    stats = {}
    for act in activites:
        type_act = act["type"]
        stats[type_act] = stats.get(type_act, 0) + 1

    print("\n📊 Répartition par type :")
    for type_act, count in sorted(stats.items()):
        print(f"   {type_act}: {count} activités")

    return len(activites)


def main():
    """Point d'entrée principal du script"""
    print("╔══════════════════════════════════════════════════════════════╗")
    print("║  Script de conversion des données FESUP 2026                ║")
    print("╚══════════════════════════════════════════════════════════════╝\n")

    total_eleves = convert_eleves()
    total_activites = convert_activites()

    print("\n" + "=" * 60)
    print("🎉 CONVERSION TERMINÉE")
    print("=" * 60)
    print(f"✅ {total_eleves} élèves convertis")
    print(f"✅ {total_activites} activités converties")
    print("\n📁 Fichiers générés :")
    print("   - eleves_converted.csv")
    print("   - activites_converted.csv")
    print(
        "\n💡 Les fichiers créneaux et salles utilisent déjà les templates existants."
    )
    print("=" * 60)


if __name__ == "__main__":
    main()
