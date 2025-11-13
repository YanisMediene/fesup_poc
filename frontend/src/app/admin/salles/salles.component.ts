import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SalleAdminService, Salle } from '../../services/salle-admin.service';

@Component({
  selector: 'app-salles',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './salles.component.html',
  styleUrls: ['./salles.component.scss']
})
export class SallesComponent implements OnInit {
  salles: Salle[] = [];
  loading = false;
  error = '';
  showForm = false;
  editingId: number | null = null;
  salleForm: FormGroup;

  constructor(
    private salleService: SalleAdminService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.salleForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      capacite: [30, [Validators.required, Validators.min(1)]],
      batiment: ['', Validators.required],
      equipements: ['']
    });
  }

  ngOnInit(): void {
    this.loadSalles();
  }

  loadSalles(): void {
    this.loading = true;
    this.error = '';
    
    this.salleService.getAll().subscribe({
      next: (data) => {
        this.salles = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des salles';
        this.loading = false;
        console.error(err);
      }
    });
  }

  openCreateForm(): void {
    this.showForm = true;
    this.editingId = null;
    this.salleForm.reset({
      capacite: 30
    });
  }

  openEditForm(salle: Salle): void {
    this.showForm = true;
    this.editingId = salle.id ?? null;
    this.salleForm.patchValue({
      nom: salle.nom,
      capacite: salle.capacite,
      batiment: salle.batiment,
      equipements: salle.equipements
    });
  }

  closeForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.salleForm.reset();
    this.error = '';
  }

  onSubmit(): void {
    if (this.salleForm.invalid) {
      Object.keys(this.salleForm.controls).forEach(key => {
        this.salleForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.error = '';

    const salleData = this.salleForm.value;

    if (this.editingId) {
      // Update
      this.salleService.update(this.editingId, salleData).subscribe({
        next: () => {
          this.loading = false;
          this.closeForm();
          this.loadSalles();
        },
        error: (err) => {
          this.error = 'Erreur lors de la modification de la salle';
          this.loading = false;
          console.error(err);
        }
      });
    } else {
      // Create
      this.salleService.create(salleData).subscribe({
        next: () => {
          this.loading = false;
          this.closeForm();
          this.loadSalles();
        },
        error: (err) => {
          this.error = 'Erreur lors de la création de la salle';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  deleteSalle(id: number, nom: string): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer la salle "${nom}" ?`)) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.salleService.delete(id).subscribe({
      next: () => {
        this.loading = false;
        this.loadSalles();
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression de la salle';
        this.loading = false;
        console.error(err);
      }
    });
  }

  deleteAllSalles(): void {
    const confirmMessage = `⚠️ ATTENTION : Voulez-vous vraiment supprimer TOUTES les salles ?\n\n` +
                          `Cela supprimera également :\n` +
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

    this.salleService.deleteAll().subscribe({
      next: (response) => {
        this.loading = false;
        alert(`✅ ${response.sallesSupprimes} salles, ${response.sessionsSupprimes} sessions et ${response.affectationsSupprimes} affectations supprimées avec succès`);
        this.loadSalles();
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression de toutes les salles';
        this.loading = false;
        console.error(err);
        alert('❌ Erreur lors de la suppression : ' + (err.error?.message || err.message));
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }
}
