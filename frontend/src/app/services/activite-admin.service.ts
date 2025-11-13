import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DemiJournee } from '../models/common.model';

export enum TypeActivite {
  CONFERENCE = 'CONFERENCE',
  TABLE_RONDE = 'TABLE_RONDE',
  FLASH_METIER = 'FLASH_METIER'
}

export interface Activite {
  id?: number;
  titre: string;
  description: string;
  type: string;
  demiJournee: string;
  capaciteMax: number;
}

@Injectable({
  providedIn: 'root'
})
export class ActiviteAdminService {
  private apiUrl = '/api/admin/activites';

  constructor(private http: HttpClient) { }

  getAll(): Observable<Activite[]> {
    return this.http.get<Activite[]>(this.apiUrl);
  }

  getById(id: number): Observable<Activite> {
    return this.http.get<Activite>(`${this.apiUrl}/${id}`);
  }

  create(activite: Activite): Observable<Activite> {
    return this.http.post<Activite>(this.apiUrl, activite);
  }

  update(id: number, activite: Activite): Observable<Activite> {
    return this.http.put<Activite>(`${this.apiUrl}/${id}`, activite);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deleteAll(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/all`);
  }
}
