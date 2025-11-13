import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Lycee {
  id?: number;
  nom: string;
  ville: string;
  codePostal: string;
}

@Injectable({
  providedIn: 'root'
})
export class LyceeAdminService {
  private apiUrl = '/api/admin/lycees';

  constructor(private http: HttpClient) { }

  getAll(): Observable<Lycee[]> {
    return this.http.get<Lycee[]>(this.apiUrl);
  }

  getById(id: number): Observable<Lycee> {
    return this.http.get<Lycee>(`${this.apiUrl}/${id}`);
  }

  create(lycee: Lycee): Observable<Lycee> {
    return this.http.post<Lycee>(this.apiUrl, lycee);
  }

  update(id: number, lycee: Lycee): Observable<Lycee> {
    return this.http.put<Lycee>(`${this.apiUrl}/${id}`, lycee);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deleteAll(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/all`);
  }
}
