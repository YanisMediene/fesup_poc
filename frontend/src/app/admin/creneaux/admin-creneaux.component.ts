import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CreneauService } from '../../services/creneau.service';
import { Creneau, CreneauDTO } from '../../models/creneau.model';
import { DEMI_JOURNEE_OPTIONS, DemiJourneeOption, DemiJournee, getDemiJourneeLabel } from '../../models/common.model';
import { DemiJourneeLabelPipe } from '../../pipes/demi-journee-label.pipe';

@Component({
  selector: 'app-admin-creneaux',
  templateUrl: './admin-creneaux.component.html',
  styleUrls: ['./admin-creneaux.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DemiJourneeLabelPipe]
})
export class AdminCreneauxComponent implements OnInit {
  creneaux: Creneau[] = [];
  filteredCreneaux: Creneau[] = [];
  demiJourneeOptions: DemiJourneeOption[] = DEMI_JOURNEE_OPTIONS;
  
  // Formulaire
  showForm = false;
  isEditing = false;
  currentCreneau: Partial<Creneau> = {
    libelle: '',
    heureDebut: '',
    heureFin: '',
    demiJournee: DemiJournee.JOUR1_MATIN
  };
  
  // Import CSV
  selectedFile: File | null = null;
  importing = false;
  importResult: { success: number; total: number; errors: string[]; message: string } | null = null;
  
  // Filtres
  filterDemiJournee: string = '';
  searchText: string = '';
  
  // Loading states
  loading = false;
  deleting = false;
  
  // Messages
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private creneauService: CreneauService) { }

  ngOnInit(): void {
    this.loadCreneaux();
  }

  /**
   * Charger tous les créneaux
   */
  loadCreneaux(): void {
    this.loading = true;
    this.creneauService.getAllCreneaux().subscribe({
      next: (data) => {
        this.creneaux = data.sort((a, b) => {
          // Trier par demi-journée puis par heure de début
          if (a.demiJournee !== b.demiJournee) {
            return a.demiJournee.localeCompare(b.demiJournee);
          }
          return a.heureDebut.localeCompare(b.heureDebut);
        });
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des créneaux', err);
        this.showError('Erreur lors du chargement des créneaux');
        this.loading = false;
      }
    });
  }

  /**
   * Appliquer les filtres
   */
  applyFilters(): void {
    this.filteredCreneaux = this.creneaux.filter(c => {
      const matchDemiJournee = !this.filterDemiJournee || c.demiJournee === this.filterDemiJournee;
      const matchSearch = !this.searchText || 
        c.libelle.toLowerCase().includes(this.searchText.toLowerCase()) ||
        c.heureDebut.includes(this.searchText) ||
        c.heureFin.includes(this.searchText);
      return matchDemiJournee && matchSearch;
    });
  }

  /**
   * Ouvrir le formulaire pour un nouveau créneau
   */
  openNewForm(): void {
    this.currentCreneau = {
      libelle: '',
      heureDebut: '',
      heureFin: '',
      demiJournee: DemiJournee.JOUR1_MATIN
    };
    this.isEditing = false;
    this.showForm = true;
    this.clearMessages();
  }

  /**
   * Ouvrir le formulaire pour éditer un créneau
   */
  editCreneau(creneau: Creneau): void {
    this.currentCreneau = { ...creneau };
    this.isEditing = true;
    this.showForm = true;
    this.clearMessages();
  }

  /**
   * Sauvegarder le créneau (création ou modification)
   */
  saveCreneau(): void {
    if (!this.validateForm()) {
      return;
    }

    const creneauDTO: CreneauDTO = {
      libelle: this.currentCreneau.libelle!,
      heureDebut: this.currentCreneau.heureDebut!,
      heureFin: this.currentCreneau.heureFin!,
      demiJournee: this.currentCreneau.demiJournee!
    };

    if (this.isEditing && this.currentCreneau.id) {
      // Mise à jour
      this.creneauService.updateCreneau(this.currentCreneau.id, creneauDTO).subscribe({
        next: () => {
          this.showSuccess('Créneau modifié avec succès');
          this.loadCreneaux();
          this.closeForm();
        },
        error: (err) => {
          console.error('Erreur lors de la modification', err);
          this.showError('Erreur lors de la modification du créneau');
        }
      });
    } else {
      // Création
      this.creneauService.createCreneau(creneauDTO).subscribe({
        next: () => {
          this.showSuccess('Créneau créé avec succès');
          this.loadCreneaux();
          this.closeForm();
        },
        error: (err) => {
          console.error('Erreur lors de la création', err);
          this.showError('Erreur lors de la création du créneau');
        }
      });
    }
  }

  /**
   * Supprimer un créneau
   */
  deleteCreneau(id: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce créneau ?')) {
      return;
    }

    this.creneauService.deleteCreneau(id).subscribe({
      next: () => {
        this.showSuccess('Créneau supprimé avec succès');
        this.loadCreneaux();
      },
      error: (err) => {
        console.error('Erreur lors de la suppression', err);
        if (err.status === 409) {
          this.showError(err.error.message || 'Ce créneau est utilisé dans des sessions');
        } else {
          this.showError('Erreur lors de la suppression du créneau');
        }
      }
    });
  }

  /**
   * Supprimer tous les créneaux
   */
  deleteAllCreneaux(): void {
    if (!confirm('⚠️ ATTENTION : Cela supprimera tous les créneaux ET toutes les sessions associées. Continuer ?')) {
      return;
    }

    this.deleting = true;
    this.creneauService.deleteAllCreneaux().subscribe({
      next: (response) => {
        this.showSuccess(`${response.creneauxSupprimes} créneaux et ${response.sessionsSupprimes} sessions supprimés`);
        this.loadCreneaux();
        this.deleting = false;
      },
      error: (err) => {
        console.error('Erreur lors de la suppression en masse', err);
        this.showError('Erreur lors de la suppression en masse');
        this.deleting = false;
      }
    });
  }

  /**
   * Gérer la sélection de fichier CSV
   */
  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
    this.importResult = null;
  }

  /**
   * Importer le fichier CSV
   */
  importCsv(): void {
    if (!this.selectedFile) {
      this.showError('Veuillez sélectionner un fichier CSV');
      return;
    }

    this.importing = true;
    this.importResult = null;
    this.clearMessages();

    this.creneauService.importFromCsv(this.selectedFile).subscribe({
      next: (response) => {
        this.importResult = response;
        if (response.errors.length === 0) {
          this.showSuccess(response.message);
        } else {
          this.showError(`Import terminé avec ${response.errors.length} erreur(s)`);
        }
        this.loadCreneaux();
        this.importing = false;
        this.selectedFile = null;
      },
      error: (err) => {
        console.error('Erreur lors de l\'import CSV', err);
        this.showError('Erreur lors de l\'import du fichier CSV');
        this.importing = false;
      }
    });
  }

  /**
   * Valider le formulaire
   */
  validateForm(): boolean {
    if (!this.currentCreneau.libelle?.trim()) {
      this.showError('Le libellé est obligatoire');
      return false;
    }
    if (!this.currentCreneau.heureDebut) {
      this.showError('L\'heure de début est obligatoire');
      return false;
    }
    if (!this.currentCreneau.heureFin) {
      this.showError('L\'heure de fin est obligatoire');
      return false;
    }
    if (this.currentCreneau.heureDebut >= this.currentCreneau.heureFin) {
      this.showError('L\'heure de fin doit être après l\'heure de début');
      return false;
    }
    return true;
  }

  /**
   * Fermer le formulaire
   */
  closeForm(): void {
    this.showForm = false;
    this.currentCreneau = {
      libelle: '',
      heureDebut: '',
      heureFin: '',
      demiJournee: DemiJournee.JOUR1_MATIN
    };
  }

  /**
   * Obtenir le label d'une demi-journée
   */
  getDemiJourneeLabel(demiJournee: DemiJournee): string {
    return getDemiJourneeLabel(demiJournee);
  }

  /**
   * Grouper les créneaux par demi-journée
   */
  getCreneauxByDemiJournee(): Map<DemiJournee, Creneau[]> {
    const grouped = new Map<DemiJournee, Creneau[]>();
    this.filteredCreneaux.forEach(c => {
      if (!grouped.has(c.demiJournee)) {
        grouped.set(c.demiJournee, []);
      }
      grouped.get(c.demiJournee)!.push(c);
    });
    return grouped;
  }

  /**
   * Afficher un message de succès
   */
  private showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => this.successMessage = '', 5000);
  }

  /**
   * Afficher un message d'erreur
   */
  private showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
  }

  /**
   * Effacer les messages
   */
  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  /**
   * Compter les créneaux du matin (JOUR1_MATIN + JOUR2_MATIN)
   */
  get countMatin(): number {
    return this.creneaux.filter(c => c.demiJournee.includes('MATIN')).length;
  }

  /**
   * Compter les créneaux de l'après-midi (JOUR1_APRES_MIDI + JOUR2_APRES_MIDI)
   */
  get countApresMidi(): number {
    return this.creneaux.filter(c => c.demiJournee.includes('APRES_MIDI')).length;
  }

  /**
   * Compter les créneaux d'une demi-journée spécifique
   */
  countByDemiJournee(demiJournee: string): number {
    return this.creneaux.filter(c => c.demiJournee === demiJournee).length;
  }
}
