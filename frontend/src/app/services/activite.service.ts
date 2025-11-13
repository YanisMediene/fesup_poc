import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Activite } from '../models/activite.model';

@Injectable({
  providedIn: 'root'
})
export class ActiviteService {
  private apiUrl = '/api/activites';

  constructor(private http: HttpClient) {}

  /**
   * Récupérer une activité par son ID
   */
  getActivite(id: number): Observable<Activite> {
    return this.http.get<Activite>(`${this.apiUrl}/${id}`);
  }

  /**
   * Récupérer toutes les activités
   */
  getAllActivites(): Observable<Activite[]> {
    return this.http.get<Activite[]>(this.apiUrl);
  }
}
