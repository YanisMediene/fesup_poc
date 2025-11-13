import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Eleve {
  id: number;
  idNational: string;
  nom: string;
  prenom: string;
  demiJournee: string;
  voeuxSoumis: boolean;
  dateSoumission?: string;
  nbVoeux: number;
  lycee?: {
    id: number;
    nom: string;
    ville: string;
    codePostal: string;
  };
  voeux?: Array<{
    id: number;
    priorite: number;
    typeVoeu: string;
    activite: {
      id: number;
      titre: string;
      type: string;
    };
  }>;
}

export interface EleveStats {
  total: number;
  avecVoeux: number;
  sansVoeux: number;
  tauxCompletion: number;
}

export interface CreateEleveRequest {
  nom: string;
  prenom: string;
  lyceeId: number;
  demiJournee: string;
}

@Injectable({
  providedIn: 'root'
})
export class EleveAdminService {
  private apiUrl = '/api/admin/eleves';

  constructor(private http: HttpClient) { }

  getAll(): Observable<Eleve[]> {
    return this.http.get<Eleve[]>(this.apiUrl);
  }

  create(eleve: CreateEleveRequest): Observable<Eleve> {
    return this.http.post<Eleve>(this.apiUrl, eleve);
  }

  getById(id: number): Observable<Eleve> {
    return this.http.get<Eleve>(`${this.apiUrl}/${id}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  resetVoeux(id: number): Observable<Eleve> {
    return this.http.post<Eleve>(`${this.apiUrl}/${id}/reset-voeux`, {});
  }

  getStats(): Observable<EleveStats> {
    return this.http.get<EleveStats>(`${this.apiUrl}/stats`);
  }

  deleteAll(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/all`);
  }
}
