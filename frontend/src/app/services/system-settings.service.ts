import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SystemStats {
  voeux: number;
  affectations: number;
  sessions: number;
  eleves: number;
  activites: number;
  salles: number;
  creneaux: number;
  lycees: number;
}

@Injectable({
  providedIn: 'root'
})
export class SystemSettingsService {
  private apiUrl = '/api/superadmin/system';

  constructor(private http: HttpClient) { }

  getSystemStats(): Observable<SystemStats> {
    return this.http.get<SystemStats>(`${this.apiUrl}/stats`);
  }

  purgeAllData(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/purge-all`);
  }

  // Méthodes d'export CSV
  exportEleves(): void {
    this.downloadFile(`${this.apiUrl}/export/eleves`, 'eleves.csv');
  }

  exportActivites(): void {
    this.downloadFile(`${this.apiUrl}/export/activites`, 'activites.csv');
  }

  exportSalles(): void {
    this.downloadFile(`${this.apiUrl}/export/salles`, 'salles.csv');
  }

  exportCreneaux(): void {
    this.downloadFile(`${this.apiUrl}/export/creneaux`, 'creneaux.csv');
  }

  exportLycees(): void {
    this.downloadFile(`${this.apiUrl}/export/lycees`, 'lycees.csv');
  }

  exportVoeux(): void {
    this.downloadFile(`${this.apiUrl}/export/voeux`, 'voeux.csv');
  }

  exportSessions(): void {
    this.downloadFile(`${this.apiUrl}/export/sessions`, 'sessions.csv');
  }

  exportAffectations(): void {
    this.downloadFile(`${this.apiUrl}/export/affectations`, 'affectations.csv');
  }

  exportAll(): void {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5);
    this.downloadFile(`${this.apiUrl}/export/all`, `export_fesup_${timestamp}.zip`);
  }

  private downloadFile(url: string, filename: string): void {
    this.http.get(url, { responseType: 'blob', observe: 'response' }).subscribe({
      next: (response) => {
        const blob = response.body;
        if (blob) {
          const link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = filename;
          link.click();
          window.URL.revokeObjectURL(link.href);
        }
      },
      error: (err) => {
        console.error('Erreur lors du téléchargement:', err);
        alert('Erreur lors du téléchargement du fichier');
      }
    });
  }
}
