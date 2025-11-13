import { DemiJournee } from './common.model';

/**
 * Modèle pour un créneau horaire
 */
export interface Creneau {
  id?: number;
  libelle: string;
  heureDebut: string; // Format HH:mm (ex: "08:30")
  heureFin: string;   // Format HH:mm (ex: "09:30")
  demiJournee: DemiJournee;
}

/**
 * Données pour créer ou mettre à jour un créneau
 */
export interface CreneauDTO {
  libelle: string;
  heureDebut: string;
  heureFin: string;
  demiJournee: DemiJournee;
}

/**
 * Réponse de l'import CSV
 */
export interface ImportCsvResponse {
  success: number;
  total: number;
  errors: string[];
  message: string;
}

/**
 * Réponse de la suppression en masse
 */
export interface DeleteAllResponse {
  creneauxSupprimes: number;
  sessionsSupprimes: number;
}
