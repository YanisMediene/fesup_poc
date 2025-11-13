import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { VoeuxService } from '../../services/voeux.service';
import { AuthResponse } from '../../models/eleve.model';

@Component({
  selector: 'app-confirmation-soumission',
  templateUrl: './confirmation-soumission.component.html',
  styleUrls: ['./confirmation-soumission.component.scss']
})
export class ConfirmationSoumissionComponent implements OnInit {
  
  eleve: AuthResponse | null = null;
  dateSoumission: Date = new Date();
  ticketDisponible: boolean = false;
  isLoadingTicket: boolean = false;
  isCheckingTicket: boolean = true;
  
  constructor(
    private voeuxService: VoeuxService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.eleve = this.voeuxService.getEleveConnecte();
    
    // Si pas d'élève connecté, rediriger vers identification
    if (!this.eleve) {
      this.router.navigate(['/voeux/identification']);
      return;
    }
    
    // Marquer l'élève comme ayant soumis ses vœux
    if (this.eleve && !this.eleve.voeuxDejasoumis) {
      this.eleve.voeuxDejasoumis = true;
      // Mettre à jour dans sessionStorage
      sessionStorage.setItem('eleve_connecte', JSON.stringify(this.eleve));
    }
    
    // Vérifier la disponibilité du ticket
    this.verifierDisponibiliteTicket();
  }
  
  verifierDisponibiliteTicket(): void {
    if (!this.eleve) return;
    
    this.isCheckingTicket = true;
    this.voeuxService.checkTicketDisponible(this.eleve.id, this.eleve.nom)
      .subscribe({
        next: (response) => {
          this.ticketDisponible = response.disponible;
          this.isCheckingTicket = false;
        },
        error: () => {
          this.ticketDisponible = false;
          this.isCheckingTicket = false;
        }
      });
  }
  
  telechargerMonPlanning(): void {
    if (!this.eleve) {
      alert('Erreur d\'authentification');
      return;
    }
    
    this.isLoadingTicket = true;
    
    this.voeuxService.telechargerTicket(this.eleve.id, this.eleve.nom)
      .subscribe({
        next: (blob) => {
          // Créer un lien de téléchargement
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `planning_fesup_${this.eleve?.prenom}_${this.eleve?.nom}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
          
          this.isLoadingTicket = false;
        },
        error: (error) => {
          console.error('Erreur téléchargement:', error);
          alert('Impossible de télécharger le planning. Il sera bientôt disponible.');
          this.isLoadingTicket = false;
        }
      });
  }
  
  fermer(): void {
    // Réinitialiser la session et retourner à l'accueil
    this.voeuxService.clearSession();
    this.router.navigate(['/voeux/identification']);
  }
}
