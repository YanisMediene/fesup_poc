import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { VoeuxService } from '../../services/voeux.service';
import { AuthResponse } from '../../models/eleve.model';
import { ActivitesGroupees } from '../../models/activite.model';

@Component({
  selector: 'app-formulaire-voeux',
  templateUrl: './formulaire-voeux.component.html',
  styleUrls: ['./formulaire-voeux.component.scss']
})
export class FormulaireVoeuxComponent implements OnInit {
  
  formulaire!: FormGroup;
  activites: ActivitesGroupees | null = null;
  eleve: AuthResponse | null = null;
  erreurs: string[] = [];
  isLoading: boolean = false;
  isSubmitting: boolean = false;
  
  constructor(
    private fb: FormBuilder,
    private voeuxService: VoeuxService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    // Récupérer l'élève connecté
    this.eleve = this.voeuxService.getEleveConnecte();
    
    if (!this.eleve) {
      this.router.navigate(['/voeux/identification']);
      return;
    }
    
    if (this.eleve.voeuxDejasoumis) {
      this.router.navigate(['/voeux/confirmation']);
      return;
    }
    
    // Charger les activités
    this.isLoading = true;
    this.voeuxService.getActivites(this.eleve.demiJournee).subscribe({
      next: (data) => {
        this.activites = data;
        this.isLoading = false;
      },
      error: (error) => {
        this.erreurs.push('Erreur lors du chargement des activités');
        this.isLoading = false;
      }
    });
    
    // Créer le formulaire avec validations
    this.formulaire = this.fb.group({
      conferenceVoeu1: [null, Validators.required],
      conferenceVoeu2: [null, Validators.required],
      activiteVoeu3: [null, Validators.required],
      activiteVoeu4: [null, Validators.required],
      activiteVoeu5: [null, Validators.required]
    }, {
      validators: [
        this.validateur12Differents(),
        this.validateur345Differents(),
        this.validateurPasDeDoublons()
      ]
    });
    
    // Écouter les changements pour afficher les erreurs en temps réel
    this.formulaire.valueChanges.subscribe(() => {
      this.mettreAJourErreurs();
    });
  }
  
  /**
   * VALIDATEUR PERSONNALISÉ : Vœux 1 et 2 différents
   */
  validateur12Differents() {
    return (control: AbstractControl): ValidationErrors | null => {
      const voeu1 = control.get('conferenceVoeu1')?.value;
      const voeu2 = control.get('conferenceVoeu2')?.value;
      
      if (voeu1 && voeu2 && voeu1 === voeu2) {
        return { doublonVoeux12: true };
      }
      
      return null;
    };
  }
  
  /**
   * VALIDATEUR PERSONNALISÉ : Vœux 3-4-5 tous différents
   */
  validateur345Differents() {
    return (control: AbstractControl): ValidationErrors | null => {
      const voeu3 = control.get('activiteVoeu3')?.value;
      const voeu4 = control.get('activiteVoeu4')?.value;
      const voeu5 = control.get('activiteVoeu5')?.value;
      
      const voeux = [voeu3, voeu4, voeu5].filter(v => v !== null);
      const unique = new Set(voeux);
      
      if (voeux.length > 0 && voeux.length !== unique.size) {
        return { doublonVoeux345: true };
      }
      
      return null;
    };
  }
  
  /**
   * VALIDATEUR PERSONNALISÉ : Pas de doublons entre vœux 1-2 et 3-4-5
   */
  validateurPasDeDoublons() {
    return (control: AbstractControl): ValidationErrors | null => {
      const voeu1 = control.get('conferenceVoeu1')?.value;
      const voeu2 = control.get('conferenceVoeu2')?.value;
      const voeu3 = control.get('activiteVoeu3')?.value;
      const voeu4 = control.get('activiteVoeu4')?.value;
      const voeu5 = control.get('activiteVoeu5')?.value;
      
      const voeux12 = new Set([voeu1, voeu2].filter(v => v !== null));
      const voeux345 = [voeu3, voeu4, voeu5].filter(v => v !== null);
      
      for (const voeu of voeux345) {
        if (voeux12.has(voeu)) {
          return { doublonEntre12Et345: true };
        }
      }
      
      return null;
    };
  }
  
  /**
   * Mettre à jour les messages d'erreur pour l'UI
   */
  mettreAJourErreurs(): void {
    this.erreurs = [];
    
    if (this.formulaire.hasError('doublonVoeux12')) {
      this.erreurs.push('⚠️ Les vœux 1 et 2 doivent être différents');
    }
    
    if (this.formulaire.hasError('doublonVoeux345')) {
      this.erreurs.push('⚠️ Les vœux 3, 4 et 5 doivent tous être différents');
    }
    
    if (this.formulaire.hasError('doublonEntre12Et345')) {
      this.erreurs.push('⚠️ Vous ne pouvez pas choisir une activité déjà sélectionnée en vœu 1 ou 2');
    }
  }
  
  /**
   * Soumettre le formulaire
   */
  soumettre(): void {
    if (this.formulaire.invalid || !this.eleve) {
      this.erreurs.push('❌ Veuillez corriger les erreurs avant de soumettre');
      return;
    }
    
    this.isSubmitting = true;
    this.erreurs = [];
    
    const submission = {
      eleveId: this.eleve.id,
      ...this.formulaire.value
    };
    
    this.voeuxService.soumettreVoeux(submission).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        // Naviguer vers le récapitulatif pour vérification avant validation finale
        this.router.navigate(['/voeux/recapitulatif']);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.erreurs.push('❌ ' + (error.error?.message || 'Erreur lors de la sauvegarde'));
      }
    });
  }
  
  /**
   * Retour à l'écran précédent
   */
  retour(): void {
    this.router.navigate(['/voeux/confirmation-identite']);
  }
}
