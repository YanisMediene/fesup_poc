export interface Eleve {
  id: number;
  idNational: string;
  nom: string;
  prenom: string;
  lycee: string;
  demiJournee: string;
  voeuxDejasoumis: boolean;
}

export interface AuthRequest {
  idNational: string;
  nom: string;
}

export interface AuthResponse {
  id: number;
  idNational: string;
  nom: string;
  prenom: string;
  lycee: string;
  demiJournee: string;
  voeuxDejasoumis: boolean;
}
