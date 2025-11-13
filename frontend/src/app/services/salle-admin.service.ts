import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Salle {
  id?: number;
  nom: string;
  capacite: number;
  batiment: string;
  equipements: string;
}

@Injectable({
  providedIn: 'root'
})
export class SalleAdminService {
  private apiUrl = '/api/admin/salles';

  constructor(private http: HttpClient) { }

  getAll(): Observable<Salle[]> {
    return this.http.get<Salle[]>(this.apiUrl);
  }

  getById(id: number): Observable<Salle> {
    return this.http.get<Salle>(`${this.apiUrl}/${id}`);
  }

  create(salle: Salle): Observable<Salle> {
    return this.http.post<Salle>(this.apiUrl, salle);
  }

  update(id: number, salle: Salle): Observable<Salle> {
    return this.http.put<Salle>(`${this.apiUrl}/${id}`, salle);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deleteAll(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/all`);
  }
}
