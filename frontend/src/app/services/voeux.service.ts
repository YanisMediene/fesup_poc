import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AuthRequest, AuthResponse } from '../models/eleve.model';
import { ActivitesGroupees } from '../models/activite.model';
import { VoeuxSubmission, VoeuxSubmissionResponse } from '../models/voeux-submission.model';

@Injectable({
  providedIn: 'root'
})
export class VoeuxService {
  private apiUrl = '/api/voeux';
  
  // State management simple pour le POC avec BehaviorSubject
  private eleveConnecte = new BehaviorSubject<AuthResponse | null>(null);
  public eleveConnecte$ = this.eleveConnecte.asObservable();
  
  private readonly STORAGE_KEY = 'eleve_connecte';
  
  constructor(private http: HttpClient) {
    // Restaurer l'élève depuis sessionStorage si disponible
    this.restaurerSession();
  }
  
  /**
   * 1. Vérifier l'identité de l'élève
   */
  verifierEleve(request: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth`, request)
      .pipe(
        tap(response => {
          this.eleveConnecte.next(response);
          sessionStorage.setItem(this.STORAGE_KEY, JSON.stringify(response));
        })
      );
  }
  
  /**
   * 2. Récupérer les activités par demi-journée
   */
  getActivites(demiJournee: string): Observable<ActivitesGroupees> {
    return this.http.get<ActivitesGroupees>(
      `${this.apiUrl}/activites/${demiJournee}`
    );
  }
  
  /**
   * 3. Soumettre les vœux
   */
  soumettreVoeux(submission: VoeuxSubmission): Observable<VoeuxSubmissionResponse> {
    return this.http.post<VoeuxSubmissionResponse>(
      `${this.apiUrl}/soumettre`, 
      submission
    );
  }
  
  /**
   * 4. Obtenir l'élève connecté (depuis le state)
   */
  getEleveConnecte(): AuthResponse | null {
    return this.eleveConnecte.value;
  }
  
  /**
   * 5. Vérifier si vœux déjà soumis
   */
  aDejasoumis(eleveId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/status/${eleveId}`);
  }
  
  /**
   * 6. Récupérer les vœux d'un élève
   */
  getVoeuxByEleve(eleveId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/eleves/${eleveId}/voeux`);
  }
  
  /**
   * 7. Valider définitivement les vœux (marquer comme soumis)
   */
  validerVoeux(eleveId: number): Observable<any> {
    return this.http.post(`/api/eleves/${eleveId}/valider-voeux`, {});
  }
  
  /**
   * 8. Obtenir l'ID de l'élève connecté
   */
  getEleveId(): number | null {
    const eleve = this.getEleveConnecte();
    return eleve ? eleve.id : null;
  }
  
  /**
   * 9. Réinitialiser la session
   */
  clearSession(): void {
    this.eleveConnecte.next(null);
    sessionStorage.removeItem(this.STORAGE_KEY);
  }
  
  /**
   * 10. Vérifier la disponibilité du ticket
   */
  checkTicketDisponible(eleveId: number, nom: string): Observable<{ disponible: boolean }> {
    return this.http.get<{ disponible: boolean }>(
      `${this.apiUrl}/mon-ticket/status`,
      { params: { eleveId: eleveId.toString(), nom } }
    );
  }
  
  /**
   * 11. Télécharger le ticket de l'élève
   */
  telechargerTicket(eleveId: number, nom: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/mon-ticket`, {
      params: { eleveId: eleveId.toString(), nom },
      responseType: 'blob'
    });
  }
  
  /**
   * Restaurer la session depuis sessionStorage
   */
  private restaurerSession(): void {
    const stored = sessionStorage.getItem(this.STORAGE_KEY);
    if (stored) {
      try {
        const eleve = JSON.parse(stored);
        this.eleveConnecte.next(eleve);
      } catch (e) {
        sessionStorage.removeItem(this.STORAGE_KEY);
      }
    }
  }
}
