import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SessionDTO {
  id?: number;
  activiteId: number;
  activiteTitre?: string;
  activiteType?: string;
  salleId: number;
  salleNom?: string;
  salleCapacite?: number;
  creneauId: number;
  creneauLibelle?: string;
  creneauDemiJournee?: string;
  capaciteDisponible?: number;
}

@Injectable({
  providedIn: 'root'
})
export class SessionAdminService {
  private apiUrl = '/api/admin/sessions';

  constructor(private http: HttpClient) {}

  getAll(): Observable<SessionDTO[]> {
    return this.http.get<SessionDTO[]>(this.apiUrl);
  }

  getById(id: number): Observable<SessionDTO> {
    return this.http.get<SessionDTO>(`${this.apiUrl}/${id}`);
  }

  create(session: SessionDTO): Observable<SessionDTO> {
    return this.http.post<SessionDTO>(this.apiUrl, session);
  }

  update(id: number, session: SessionDTO): Observable<SessionDTO> {
    return this.http.put<SessionDTO>(`${this.apiUrl}/${id}`, session);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deleteAll(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/all`);
  }

  /**
   * Génère automatiquement les sessions en fonction des vœux des élèves
   */
  genererSessionsAutomatiquement(): Observable<any> {
    return this.http.post(`${this.apiUrl}/generation/auto`, {});
  }
}
