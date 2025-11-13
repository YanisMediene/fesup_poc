import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { VoeuxService } from '../../services/voeux.service';
import { ActiviteService } from '../../services/activite.service';
import { Voeu } from '../../models/voeu.model';
import { Activite } from '../../models/activite.model';

@Component({
  selector: 'app-recapitulatif-voeux',
  templateUrl: './recapitulatif-voeux.component.html',
  styleUrls: ['./recapitulatif-voeux.component.scss']
})
export class RecapitulatifVoeuxComponent implements OnInit {
  voeux: Voeu[] = [];
  activites: Map<number, Activite> = new Map();
  loading = false;
  error = '';

  constructor(
    private voeuxService: VoeuxService,
    private activiteService: ActiviteService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.chargerVoeux();
  }

  chargerVoeux(): void {
    const eleveId = this.voeuxService.getEleveId();
    if (!eleveId) {
      this.router.navigate(['/']);
      return;
    }

    this.loading = true;
    this.voeuxService.getVoeuxByEleve(eleveId).subscribe({
      next: (voeux) => {
        this.voeux = voeux.sort((a, b) => a.ordre - b.ordre);
        this.chargerActivites();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des vœux:', err);
        this.error = 'Impossible de charger vos vœux';
        this.loading = false;
      }
    });
  }

  chargerActivites(): void {
    const activiteIds = [...new Set(this.voeux.map(v => v.activiteId))];
    let compteur = 0;

    activiteIds.forEach(id => {
      this.activiteService.getActivite(id).subscribe({
        next: (activite) => {
          this.activites.set(id, activite);
          compteur++;
          if (compteur === activiteIds.length) {
            this.loading = false;
          }
        },
        error: (err) => {
          console.error(`Erreur lors du chargement de l'activité ${id}:`, err);
          compteur++;
          if (compteur === activiteIds.length) {
            this.loading = false;
          }
        }
      });
    });
  }

  getActivite(activiteId: number): Activite | undefined {
    return this.activites.get(activiteId);
  }

  getTypeLabel(type: string): string {
    const labels: { [key: string]: string } = {
      'CONFERENCE': 'Conférence',
      'TABLE_RONDE': 'Table Ronde',
      'FLASH_METIER': 'Flash Métier'
    };
    return labels[type] || type;
  }

  confirmer(): void {
    const eleveId = this.voeuxService.getEleveId();
    if (!eleveId) {
      this.router.navigate(['/']);
      return;
    }

    this.loading = true;
    this.voeuxService.validerVoeux(eleveId).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/voeux/confirmation']);
      },
      error: (err) => {
        console.error('Erreur lors de la validation des vœux:', err);
        this.error = 'Impossible de valider vos vœux';
        this.loading = false;
      }
    });
  }

  modifier(): void {
    this.router.navigate(['/voeux/formulaire']);
  }
}
