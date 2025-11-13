import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { VoeuxService } from '../../services/voeux.service';
import { AuthResponse } from '../../models/eleve.model';

@Component({
  selector: 'app-confirmation-identite',
  templateUrl: './confirmation-identite.component.html',
  styleUrls: ['./confirmation-identite.component.scss']
})
export class ConfirmationIdentiteComponent implements OnInit {
  
  eleve: AuthResponse | null = null;
  
  constructor(
    private voeuxService: VoeuxService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    // Récupérer l'élève connecté
    this.eleve = this.voeuxService.getEleveConnecte();
    
    // Si pas d'élève connecté, rediriger vers identification
    if (!this.eleve) {
      this.router.navigate(['/voeux/identification']);
      return;
    }
    
    // Si vœux déjà soumis, rediriger vers confirmation
    if (this.eleve.voeuxDejasoumis) {
      this.router.navigate(['/voeux/confirmation']);
    }
  }
  
  confirmer(): void {
    // Navigation vers le formulaire de vœux
    this.router.navigate(['/voeux/formulaire']);
  }
  
  retour(): void {
    // Retour à l'identification
    this.voeuxService.clearSession();
    this.router.navigate(['/voeux/identification']);
  }
}
