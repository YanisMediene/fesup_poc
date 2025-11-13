import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AffectationService, AffectationDTO, AffectationResultat } from '../../services/affectation.service';
import { SessionAdminService, SessionDTO } from '../../services/session-admin.service';
import { interval, Subscription } from 'rxjs';
import { DemiJourneeLabelPipe } from '../../pipes/demi-journee-label.pipe';

@Component({
  selector: 'app-resultats-affectation',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DemiJourneeLabelPipe],
  templateUrl: './resultats-affectation.component.html',
  styleUrl: './resultats-affectation.component.scss'
})
export class ResultatsAffectationComponent implements OnInit {
  affectations: AffectationDTO[] = [];
  sessions: SessionDTO[] = [];
  
  isRunning = false;
  hasResults = false;
  isGeneratingTickets = false;
  
  score = '';
  hardScore = 0;
  softScore = 0;
  
  private pollingSubscription?: Subscription;
  
  constructor(
    private affectationService: AffectationService,
    private sessionService: SessionAdminService
  ) {}
  
  ngOnInit(): void {
    this.loadSessions();
    this.checkStatus();
    this.loadExistingAffectations();
  }
  
  ngOnDestroy(): void {
    this.pollingSubscription?.unsubscribe();
  }
  
  loadSessions(): void {
    this.sessionService.getAll().subscribe({
      next: (data) => this.sessions = data,
      error: (err) => console.error('Erreur chargement sessions:', err)
    });
  }
  
  checkStatus(): void {
    this.affectationService.getStatus().subscribe({
      next: (status) => {
        this.isRunning = status.running;
        
        if (this.isRunning) {
          this.startPolling();
        } else if (status.hasExistingResults && !this.hasResults) {
          // ✅ Charger automatiquement les résultats existants
          console.log('📊 Chargement automatique des résultats précédents...');
          this.loadExistingAffectations();
        }
      },
      error: (err) => console.error('Erreur statut:', err)
    });
  }
  
  loadExistingAffectations(): void {
    this.affectationService.getAllAffectations().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.affectations = data;
          this.hasResults = true;
        }
      },
      error: (err) => console.error('Erreur chargement affectations:', err)
    });
  }
  
  lancerAlgorithme(): void {
    if (confirm('Lancer l\'algorithme d\'affectation ? (durée : ~5 min)')) {
      this.isRunning = true;
      this.hasResults = false;
      
      this.affectationService.lancerAffectation().subscribe({
        next: (response) => {
          console.log('Algorithme lancé:', response);
          this.startPolling();
        },
        error: (err) => {
          console.error('Erreur lancement:', err);
          this.isRunning = false;
          
          // ✅ Afficher un message d'erreur explicite
          const errorMessage = err.error?.message || err.message || 'Erreur inconnue';
          if (errorMessage.includes('aucune session')) {
            alert('⚠️ Impossible de lancer l\'algorithme :\n\n' + 
                  'Aucune session n\'a été créée.\n' +
                  'Veuillez d\'abord créer des sessions dans le menu "Gestion des Sessions".');
          } else {
            alert('Erreur lors du lancement de l\'algorithme :\n\n' + errorMessage);
          }
        }
      });
    }
  }
  
  private startPolling(): void {
    this.pollingSubscription?.unsubscribe();
    
    // Vérifier toutes les 5 secondes
    this.pollingSubscription = interval(5000).subscribe(() => {
      this.affectationService.getResultats().subscribe({
        next: (response) => {
          if (response.status === 'COMPLETED') {
            this.isRunning = false;
            this.hasResults = true;
            this.score = response.score || '';
            this.hardScore = response.hardScore || 0;
            this.softScore = response.softScore || 0;
            this.affectations = response.affectations || [];
            this.pollingSubscription?.unsubscribe();
          }
        },
        error: (err) => {
          console.error('Erreur polling:', err);
          this.isRunning = false;
          this.pollingSubscription?.unsubscribe();
        }
      });
    });
  }
  
  onSessionChange(affectation: AffectationDTO, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const sessionId = parseInt(select.value);
    
    if (affectation.id && sessionId) {
      this.affectationService.updateAffectation(affectation.id, sessionId).subscribe({
        next: (updated) => {
          console.log('Affectation mise à jour:', updated);
          // Mettre à jour l'affichage
          const index = this.affectations.findIndex(a => a.id === affectation.id);
          if (index !== -1) {
            this.affectations[index] = updated;
          }
        },
        error: (err) => console.error('Erreur mise à jour:', err)
      });
    }
  }
  
  getSessionsForEleve(affectation: AffectationDTO): SessionDTO[] {
    if (!affectation.assignedSession) return this.sessions;
    
    // Filtrer les sessions de la même demi-journée
    const demiJournee = affectation.assignedSession.creneau.demiJournee;
    return this.sessions.filter(s => s.creneauDemiJournee === demiJournee);
  }
  
  calculateStats(): { total: number; affectes: number; nonAffectes: number; tauxSatisfaction: number } {
    // Compter le nombre d'élèves uniques au lieu du nombre d'affectations
    const uniqueEleveIds = new Set(this.affectations.map(a => a.eleve.id));
    const total = uniqueEleveIds.size;
    
    // Compter les élèves avec au moins une affectation
    const elevesAvecAffectation = new Set(
      this.affectations.filter(a => a.assignedSession).map(a => a.eleve.id)
    );
    const affectes = elevesAvecAffectation.size;
    
    const nonAffectes = total - affectes;
    const tauxSatisfaction = total > 0 ? Math.round((affectes / total) * 100) : 0;
    
    return { total, affectes, nonAffectes, tauxSatisfaction };
  }
  
  // Nouveaux méthodes pour les tickets
  genererTousLesTickets(): void {
    if (confirm('Générer tous les tickets PDF en lot ? Cette opération peut prendre quelques minutes.')) {
      this.isGeneratingTickets = true;
      
      this.affectationService.genererTousLesTickets().subscribe({
        next: (response) => {
          console.log('Génération lancée:', response);
          alert('Génération des tickets lancée en arrière-plan. Les tickets seront disponibles sous peu.');
          this.isGeneratingTickets = false;
        },
        error: (err) => {
          console.error('Erreur génération tickets:', err);
          alert('Erreur lors de la génération des tickets');
          this.isGeneratingTickets = false;
        }
      });
    }
  }
  
  telechargerTicketEleve(eleve: any): void {
    this.affectationService.telechargerTicketEleve(eleve.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `ticket_${eleve.prenom}_${eleve.nom}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erreur téléchargement:', error);
        if (error.status === 404) {
          alert('Ticket non disponible pour cet élève. Générez d\'abord les tickets en lot.');
        } else {
          alert('Erreur lors du téléchargement du ticket');
        }
      }
    });
  }
  
  regenererTicketEleve(eleve: any): void {
    if (confirm(`Régénérer le ticket pour ${eleve.prenom} ${eleve.nom} ?`)) {
      this.affectationService.regenererTicketEleve(eleve.id).subscribe({
        next: (response) => {
          console.log('Ticket régénéré:', response);
          alert('Ticket régénéré avec succès !');
        },
        error: (err) => {
          console.error('Erreur régénération:', err);
          alert('Erreur lors de la régénération du ticket');
        }
      });
    }
  }

  deleteAllAffectations(): void {
    const confirmMessage = `⚠️ ATTENTION : Voulez-vous vraiment supprimer TOUTES les affectations ?\n\n` +
                          `Cela supprimera également :\n` +
                          `- Tous les tickets générés\n` +
                          `- Tous les fichiers PDF\n\n` +
                          `Cette action est IRRÉVERSIBLE !`;
    
    if (!confirm(confirmMessage)) {
      return;
    }

    if (!confirm('Êtes-vous ABSOLUMENT sûr ? Tapez OK pour confirmer.')) {
      return;
    }

    this.affectationService.deleteAll().subscribe({
      next: (response) => {
        alert(`✅ ${response.count} affectations et tous les PDFs supprimés avec succès`);
        this.affectations = [];
        this.hasResults = false;
        this.score = '';
        this.hardScore = 0;
        this.softScore = 0;
      },
      error: (err) => {
        console.error('Erreur suppression des affectations:', err);
        alert('❌ Erreur lors de la suppression : ' + (err.error?.message || err.message));
      }
    });
  }
}
