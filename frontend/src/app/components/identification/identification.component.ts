import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { VoeuxService } from '../../services/voeux.service';

@Component({
  selector: 'app-identification',
  templateUrl: './identification.component.html',
  styleUrls: ['./identification.component.scss']
})
export class IdentificationComponent implements OnInit {
  
  formIdentification!: FormGroup;
  erreurAuth: string = '';
  isLoading: boolean = false;
  
  constructor(
    private fb: FormBuilder,
    private voeuxService: VoeuxService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    // Réinitialiser la session au cas où
    this.voeuxService.clearSession();
    
    // Créer le formulaire
    this.formIdentification = this.fb.group({
      idNational: ['', [
        Validators.required, 
        Validators.minLength(5),
        Validators.pattern(/^[A-Z0-9]+$/)
      ]],
      nom: ['', [Validators.required, Validators.minLength(2)]]
    });
  }
  
  verifier(): void {
    if (this.formIdentification.invalid) {
      return;
    }
    
    this.erreurAuth = '';
    this.isLoading = true;
    
    const request = {
      idNational: this.formIdentification.value.idNational.toUpperCase(),
      nom: this.formIdentification.value.nom.toUpperCase()
    };
    
    this.voeuxService.verifierEleve(request).subscribe({
      next: (response) => {
        this.isLoading = false;
        // Navigation vers l'écran de confirmation d'identité
        this.router.navigate(['/voeux/confirmation-identite']);
      },
      error: (error) => {
        this.isLoading = false;
        this.erreurAuth = error.error?.message || 'ID National ou Nom incorrect';
      }
    });
  }
}
