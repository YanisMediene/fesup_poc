import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EleveAdminService, Eleve, EleveStats, CreateEleveRequest } from '../../services/eleve-admin.service';
import { LyceeAdminService, Lycee } from '../../services/lycee-admin.service';
import { DemiJourneeLabelPipe } from '../../pipes/demi-journee-label.pipe';

@Component({
  selector: 'app-eleves',
  standalone: true,
  imports: [CommonModule, FormsModule, DemiJourneeLabelPipe],
  templateUrl: './eleves.component.html',
  styleUrls: ['./eleves.component.scss']
})
export class ElevesComponent implements OnInit {
  eleves: Eleve[] = [];
  filteredEleves: Eleve[] = [];
  stats: EleveStats | null = null;
  lycees: Lycee[] = [];
  loading = false;
  error = '';
  selectedEleve: Eleve | null = null;
  showDetails = false;
  showAddModal = false;
  filterStatus: 'all' | 'with' | 'without' = 'all';
  searchTerm = '';

  // Formulaire de création
  newEleve: CreateEleveRequest = {
    nom: '',
    prenom: '',
    lyceeId: 0,
    demiJournee: 'JOUR1_MATIN'
  };

  constructor(
    private eleveService: EleveAdminService,
    private lyceeService: LyceeAdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadEleves();
    this.loadLycees();
  }

  loadLycees(): void {
    this.lyceeService.getAll().subscribe({
      next: (data) => {
        this.lycees = data;
      },
      error: (err) => {
        console.error('Erreur chargement lycées:', err);
      }
    });
  }

  loadStats(): void {
    this.eleveService.getStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (err) => {
        console.error('Erreur chargement stats:', err);
      }
    });
  }

  loadEleves(): void {
    this.loading = true;
    this.error = '';
    
    this.eleveService.getAll().subscribe({
      next: (data) => {
        this.eleves = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des élèves';
        this.loading = false;
        console.error(err);
      }
    });
  }

  applyFilters(): void {
    let result = [...this.eleves];

    // Filtre par statut
    if (this.filterStatus === 'with') {
      result = result.filter(e => e.voeuxSoumis);
    } else if (this.filterStatus === 'without') {
      result = result.filter(e => !e.voeuxSoumis);
    }

    // Filtre par recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(e => 
        e.nom.toLowerCase().includes(term) ||
        e.prenom.toLowerCase().includes(term) ||
        e.lycee?.nom.toLowerCase().includes(term)
      );
    }

    this.filteredEleves = result;
  }

  setFilter(status: 'all' | 'with' | 'without'): void {
    this.filterStatus = status;
    this.applyFilters();
  }

  onSearchChange(event: Event): void {
    this.searchTerm = (event.target as HTMLInputElement).value;
    this.applyFilters();
  }

  viewDetails(eleve: Eleve): void {
    this.loading = true;
    this.eleveService.getById(eleve.id).subscribe({
      next: (data) => {
        this.selectedEleve = data;
        this.showDetails = true;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des détails';
        this.loading = false;
        console.error(err);
      }
    });
  }

  closeDetails(): void {
    this.showDetails = false;
    this.selectedEleve = null;
  }

  openAddModal(): void {
    this.newEleve = {
      nom: '',
      prenom: '',
      lyceeId: this.lycees.length > 0 ? this.lycees[0].id! : 0,
      demiJournee: 'JOUR1_MATIN'
    };
    this.showAddModal = true;
    this.error = '';
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.newEleve = {
      nom: '',
      prenom: '',
      lyceeId: 0,
      demiJournee: 'JOUR1_MATIN'
    };
    this.error = '';
  }

  createEleve(): void {
    // Validation
    if (!this.newEleve.nom.trim()) {
      this.error = 'Le nom est obligatoire';
      return;
    }
    if (!this.newEleve.prenom.trim()) {
      this.error = 'Le prénom est obligatoire';
      return;
    }
    if (!this.newEleve.lyceeId) {
      this.error = 'Le lycée est obligatoire';
      return;
    }

    this.loading = true;
    this.error = '';

    this.eleveService.create(this.newEleve).subscribe({
      next: () => {
        this.loading = false;
        this.closeAddModal();
        this.loadStats();
        this.loadEleves();
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la création de l\'élève';
        this.loading = false;
        console.error(err);
      }
    });
  }

  resetVoeux(eleve: Eleve): void {
    if (!confirm(`Êtes-vous sûr de vouloir réinitialiser les vœux de ${eleve.prenom} ${eleve.nom} ?`)) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.eleveService.resetVoeux(eleve.id).subscribe({
      next: () => {
        this.loading = false;
        this.loadStats();
        this.loadEleves();
        if (this.showDetails) {
          this.closeDetails();
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la réinitialisation des vœux';
        this.loading = false;
        console.error(err);
      }
    });
  }

  deleteEleve(eleve: Eleve): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer l'élève ${eleve.prenom} ${eleve.nom} ?`)) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.eleveService.delete(eleve.id).subscribe({
      next: () => {
        this.loading = false;
        this.loadStats();
        this.loadEleves();
        if (this.showDetails) {
          this.closeDetails();
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression de l\'élève';
        this.loading = false;
        console.error(err);
      }
    });
  }

  deleteAllEleves(): void {
    const confirmMessage = `⚠️ ATTENTION : Voulez-vous vraiment supprimer TOUS les élèves ?\n\n` +
                          `Cela supprimera également :\n` +
                          `- Tous les vœux associés\n` +
                          `- Toutes les affectations\n\n` +
                          `Cette action est IRRÉVERSIBLE !`;
    
    if (!confirm(confirmMessage)) {
      return;
    }

    // Double confirmation
    if (!confirm('Êtes-vous ABSOLUMENT sûr ? Tapez OK pour confirmer.')) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.eleveService.deleteAll().subscribe({
      next: (response) => {
        this.loading = false;
        alert(`✅ ${response.elevesSupprimes} élèves et ${response.voeuxSupprimes} vœux supprimés avec succès`);
        this.loadStats();
        this.loadEleves();
        if (this.showDetails) {
          this.closeDetails();
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression de tous les élèves';
        this.loading = false;
        console.error(err);
        alert('❌ Erreur lors de la suppression : ' + (err.error?.message || err.message));
      }
    });
  }

  getStatusBadgeClass(eleve: Eleve): string {
    return eleve.voeuxSoumis ? 'badge-success' : 'badge-warning';
  }

  getStatusText(eleve: Eleve): string {
    if (eleve.voeuxSoumis) {
      return `✓ ${eleve.nbVoeux} vœux soumis`;
    }
    return eleve.nbVoeux > 0 ? `⏳ ${eleve.nbVoeux} vœux en cours` : '⚠️ Aucun vœu';
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }
}
