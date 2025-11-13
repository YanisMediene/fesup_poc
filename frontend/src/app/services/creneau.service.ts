import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Creneau, CreneauDTO, ImportCsvResponse, DeleteAllResponse } from '../models/creneau.model';

@Injectable({
  providedIn: 'root'
})
export class CreneauService {
  private apiUrl = '/api/admin/creneaux';

  constructor(private http: HttpClient) { }

  /**
   * Récupérer tous les créneaux
   */
  getAllCreneaux(): Observable<Creneau[]> {
    return this.http.get<Creneau[]>(this.apiUrl);
  }

  /**
   * Récupérer un créneau par ID
   */
  getCreneauById(id: number): Observable<Creneau> {
    return this.http.get<Creneau>(`${this.apiUrl}/${id}`);
  }

  /**
   * Créer un nouveau créneau
   */
  createCreneau(creneau: CreneauDTO): Observable<Creneau> {
    return this.http.post<Creneau>(this.apiUrl, creneau);
  }

  /**
   * Mettre à jour un créneau existant
   */
  updateCreneau(id: number, creneau: CreneauDTO): Observable<Creneau> {
    return this.http.put<Creneau>(`${this.apiUrl}/${id}`, creneau);
  }

  /**
   * Supprimer un créneau
   */
  deleteCreneau(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Supprimer tous les créneaux (et les sessions associées)
   */
  deleteAllCreneaux(): Observable<DeleteAllResponse> {
    return this.http.delete<DeleteAllResponse>(`${this.apiUrl}/all`);
  }

  /**
   * Importer des créneaux depuis un fichier CSV
   */
  importFromCsv(file: File): Observable<ImportCsvResponse> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<ImportCsvResponse>(`${this.apiUrl}/import-csv`, formData);
  }
}
