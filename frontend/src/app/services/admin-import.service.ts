import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ImportReport {
  totalLines: number;
  successCount: number;
  errorCount: number;
  errors: ImportError[];
}

export interface ImportError {
  lineNumber: number;
  reason: string;
  lineContent: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminImportService {
  private apiUrl = '/api/admin/import';

  constructor(private http: HttpClient) { }

  importEleves(file: File): Observable<ImportReport> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ImportReport>(`${this.apiUrl}/eleves`, formData);
  }

  importActivites(file: File): Observable<ImportReport> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ImportReport>(`${this.apiUrl}/activites`, formData);
  }

  importSalles(file: File): Observable<ImportReport> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ImportReport>(`${this.apiUrl}/salles`, formData);
  }
}
