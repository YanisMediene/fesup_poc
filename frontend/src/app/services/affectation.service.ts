import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AffectationDTO {
  id?: number;
  eleve: {
    id: number;
    nom: string;
    prenom: string;
    lycee?: { nom: string };
  };
  assignedSession?: {
    id: number;
    activite: {
      id: number;
      titre: string;
      type: string;
    };
    salle: {
      nom: string;
    };
    creneau: {
      libelle: string;
      demiJournee: string;
    };
  };
}

export interface AffectationResultat {
  status: string;
  score?: string;
  hardScore?: number;
  softScore?: number;
  affectations?: AffectationDTO[];
  message?: string;
  problemId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AffectationService {
  private apiUrl = '/api/admin/affectations';
  private ticketApiUrl = '/api/admin/tickets';

  constructor(private http: HttpClient) {}

  lancerAffectation(): Observable<AffectationResultat> {
    return this.http.post<AffectationResultat>(`${this.apiUrl}/lancer`, {});
  }

  getStatus(): Observable<{ running: boolean; hasExistingResults: boolean }> {
    return this.http.get<{ running: boolean; hasExistingResults: boolean }>(`${this.apiUrl}/status`);
  }

  getResultats(): Observable<AffectationResultat> {
    return this.http.get<AffectationResultat>(`${this.apiUrl}/resultats`);
  }

  getAllAffectations(): Observable<AffectationDTO[]> {
    return this.http.get<AffectationDTO[]>(this.apiUrl);
  }

  updateAffectation(affectationId: number, sessionId: number): Observable<AffectationDTO> {
    return this.http.put<AffectationDTO>(
      `${this.apiUrl}/${affectationId}?sessionId=${sessionId}`,
      {}
    );
  }
  
  // Nouveaux endpoints pour les tickets
  genererTousLesTickets(): Observable<any> {
    return this.http.post(`${this.ticketApiUrl}/generer-tous`, {});
  }
  
  regenererTicketEleve(eleveId: number): Observable<any> {
    return this.http.post(`${this.ticketApiUrl}/eleves/${eleveId}/regenerer`, {});
  }
  
  telechargerTicketEleve(eleveId: number): Observable<Blob> {
    return this.http.get(`${this.ticketApiUrl}/eleves/${eleveId}/ticket`, {
      responseType: 'blob'
    });
  }

  deleteAll(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/all`);
  }
}
