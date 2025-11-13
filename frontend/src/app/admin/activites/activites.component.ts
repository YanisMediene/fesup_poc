import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ActiviteAdminService, Activite, TypeActivite } from '../../services/activite-admin.service';
import { DemiJournee, DEMI_JOURNEE_OPTIONS } from '../../models/common.model';
import { DemiJourneeLabelPipe } from '../../pipes/demi-journee-label.pipe';

@Component({
  selector: 'app-activites',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DemiJourneeLabelPipe],
  templateUrl: './activites.component.html',
  styleUrls: ['./activites.component.scss']
})
export class ActivitesComponent implements OnInit {
  activites: Activite[] = [];
  loading = false;
  error = '';
  showForm = false;
  editingId: number | null = null;
  activiteForm: FormGroup;
  
  // Enums pour le template
  typesActivite = Object.values(TypeActivite);
  demiJournees = DEMI_JOURNEE_OPTIONS;

  constructor(
    private activiteService: ActiviteAdminService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.activiteForm = this.fb.group({
      titre: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', Validators.required],
      type: [TypeActivite.CONFERENCE, Validators.required],
      demiJournee: [DemiJournee.JOUR1_MATIN, Validators.required],
      capaciteMax: [30, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loadActivites();
  }

  loadActivites(): void {
    this.loading = true;
    this.error = '';
    
    this.activiteService.getAll().subscribe({
      next: (data) => {
        this.activites = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des activités';
        this.loading = false;
        console.error(err);
      }
    });
  }

  openCreateForm(): void {
    this.showForm = true;
    this.editingId = null;
    this.activiteForm.reset({
      type: TypeActivite.CONFERENCE,
      demiJournee: DemiJournee.JOUR1_MATIN,
      capaciteMax: 30
    });
  }

  openEditForm(activite: Activite): void {
    this.showForm = true;
    this.editingId = activite.id ?? null;
    this.activiteForm.patchValue({
      titre: activite.titre,
      description: activite.description,
      type: activite.type,
      demiJournee: activite.demiJournee,
      capaciteMax: activite.capaciteMax
    });
  }

  closeForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.activiteForm.reset();
    this.error = '';
  }

  onSubmit(): void {
    if (this.activiteForm.invalid) {
      Object.keys(this.activiteForm.controls).forEach(key => {
        this.activiteForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.error = '';

    const activiteData = this.activiteForm.value;

    if (this.editingId) {
      // Update
      this.activiteService.update(this.editingId, activiteData).subscribe({
        next: () => {
          this.loading = false;
          this.closeForm();
          this.loadActivites();
        },
        error: (err) => {
          this.error = 'Erreur lors de la modification de l\'activité';
          this.loading = false;
          console.error(err);
        }
      });
    } else {
      // Create
      this.activiteService.create(activiteData).subscribe({
        next: () => {
          this.loading = false;
          this.closeForm();
          this.loadActivites();
        },
        error: (err) => {
          this.error = 'Erreur lors de la création de l\'activité';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  deleteActivite(id: number, titre: string): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer l'activité "${titre}" ?`)) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.activiteService.delete(id).subscribe({
      next: () => {
        this.loading = false;
        this.loadActivites();
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression de l\'activité';
        this.loading = false;
        console.error(err);
      }
    });
  }

  deleteAllActivites(): void {
    const confirmMessage = `⚠️ ATTENTION : Voulez-vous vraiment supprimer TOUTES les activités ?\n\n` +
                          `Cela supprimera également :\n` +
                          `- Tous les vœux des élèves\n` +
                          `- Toutes les sessions associées\n` +
                          `- Toutes les affectations associées\n` +
                          `- Les tickets PDF générés\n\n` +
                          `Cette action est IRRÉVERSIBLE !`;
    
    if (!confirm(confirmMessage)) {
      return;
    }

    if (!confirm('Êtes-vous ABSOLUMENT sûr ? Tapez OK pour confirmer.')) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.activiteService.deleteAll().subscribe({
      next: (response) => {
        this.loading = false;
        alert(`✅ ${response.activitesSupprimes} activités, ${response.voeuxSupprimes} vœux, ${response.sessionsSupprimes} sessions et ${response.affectationsSupprimes} affectations supprimées avec succès`);
        this.loadActivites();
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression de toutes les activités';
        this.loading = false;
        console.error(err);
        alert('❌ Erreur lors de la suppression : ' + (err.error?.message || err.message));
      }
    });
  }

  getTypeBadgeClass(type: string): string {
    switch (type) {
      case 'CONFERENCE':
        return 'badge-conference';
      case 'TABLE_RONDE':
        return 'badge-table-ronde';
      case 'FLASH_METIER':
        return 'badge-flash-metier';
      default:
        return '';
    }
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }
}
