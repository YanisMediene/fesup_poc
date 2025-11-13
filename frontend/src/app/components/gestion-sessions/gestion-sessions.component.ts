import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SessionAdminService, SessionDTO } from '../../services/session-admin.service';
import { SalleAdminService, Salle } from '../../services/salle-admin.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DemiJourneeLabelPipe } from '../../pipes/demi-journee-label.pipe';

interface Activite {
  id: number;
  titre: string;
  type: string;
}

interface Creneau {
  id: number;
  libelle: string;
  demiJournee: string;
}

@Component({
  selector: 'app-gestion-sessions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, DemiJourneeLabelPipe],
  templateUrl: './gestion-sessions.component.html',
  styleUrl: './gestion-sessions.component.scss'
})
export class GestionSessionsComponent implements OnInit {
  sessions: SessionDTO[] = [];
  activites: Activite[] = [];
  salles: Salle[] = [];
  creneaux: Creneau[] = [];
  
  sessionForm: FormGroup;
  editMode = false;
  selectedSessionId?: number;
  
  // Génération automatique
  isGenerating = false;
  generationResult: any = null;
  
  constructor(
    private sessionService: SessionAdminService,
    private salleService: SalleAdminService,
    private http: HttpClient,
    private fb: FormBuilder
  ) {
    this.sessionForm = this.fb.group({
      activiteId: ['', Validators.required],
      salleId: ['', Validators.required],
      creneauId: ['', Validators.required]
    });
  }
  
  ngOnInit(): void {
    this.loadSessions();
    this.loadActivites();
    this.loadSalles();
    this.loadCreneaux();
  }
  
  loadSessions(): void {
    this.sessionService.getAll().subscribe({
      next: (data) => this.sessions = data,
      error: (err) => console.error('Erreur chargement sessions:', err)
    });
  }
  
  loadActivites(): void {
    this.http.get<Activite[]>('/api/admin/activites')
      .subscribe({
        next: (data) => this.activites = data,
        error: (err) => console.error('Erreur chargement activités:', err)
      });
  }
  
  loadSalles(): void {
    this.salleService.getAll().subscribe({
      next: (data) => this.salles = data,
      error: (err) => console.error('Erreur chargement salles:', err)
    });
  }
  
  loadCreneaux(): void {
    this.http.get<Creneau[]>('/api/admin/creneaux')
      .subscribe({
        next: (data) => this.creneaux = data,
        error: (err) => console.error('Erreur chargement créneaux:', err)
      });
  }
  
  onSubmit(): void {
    if (this.sessionForm.invalid) return;
    
    const sessionData = this.sessionForm.value;
    
    if (this.editMode && this.selectedSessionId) {
      this.sessionService.update(this.selectedSessionId, sessionData)
        .subscribe({
          next: () => {
            this.loadSessions();
            this.resetForm();
          },
          error: (err) => console.error('Erreur mise à jour session:', err)
        });
    } else {
      this.sessionService.create(sessionData)
        .subscribe({
          next: () => {
            this.loadSessions();
            this.resetForm();
          },
          error: (err) => console.error('Erreur création session:', err)
        });
    }
  }
  
  editSession(session: SessionDTO): void {
    this.editMode = true;
    this.selectedSessionId = session.id;
    this.sessionForm.patchValue({
      activiteId: session.activiteId,
      salleId: session.salleId,
      creneauId: session.creneauId
    });
  }
  
  deleteSession(id: number): void {
    if (confirm('Supprimer cette session ?')) {
      this.sessionService.delete(id).subscribe({
        next: () => this.loadSessions(),
        error: (err) => console.error('Erreur suppression session:', err)
      });
    }
  }

  deleteAllSessions(): void {
    const confirmMessage = `⚠️ ATTENTION : Voulez-vous vraiment supprimer TOUTES les sessions ?\n\n` +
                          `Cela supprimera également :\n` +
                          `- Toutes les affectations associées\n` +
                          `- Les tickets PDF générés\n\n` +
                          `Cette action est IRRÉVERSIBLE !`;
    
    if (!confirm(confirmMessage)) {
      return;
    }

    if (!confirm('Êtes-vous ABSOLUMENT sûr ? Tapez OK pour confirmer.')) {
      return;
    }

    this.sessionService.deleteAll().subscribe({
      next: (response) => {
        alert(`✅ ${response.sessionsSupprimes} sessions et ${response.affectationsSupprimes} affectations supprimées avec succès`);
        this.loadSessions();
      },
      error: (err) => {
        console.error('Erreur suppression de toutes les sessions:', err);
        alert('❌ Erreur lors de la suppression : ' + (err.error?.message || err.message));
      }
    });
  }
  
  resetForm(): void {
    this.editMode = false;
    this.selectedSessionId = undefined;
    this.sessionForm.reset();
  }

  /**
   * Génère automatiquement les sessions en fonction des vœux
   */
  genererSessionsAuto(): void {
    if (!confirm('⚠️ Cela va SUPPRIMER toutes les sessions existantes et les recréer automatiquement en fonction des vœux des élèves. Continuer ?')) {
      return;
    }

    this.isGenerating = true;
    this.generationResult = null;

    this.sessionService.genererSessionsAutomatiquement().subscribe({
      next: (result) => {
        console.log('✅ Sessions générées:', result);
        this.generationResult = result;
        this.isGenerating = false;
        this.loadSessions(); // Recharger la liste
      },
      error: (err) => {
        console.error('❌ Erreur génération sessions:', err);
        alert('Erreur lors de la génération automatique des sessions');
        this.isGenerating = false;
      }
    });
  }
}
