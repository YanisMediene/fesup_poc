import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LyceeAdminService, Lycee } from '../../services/lycee-admin.service';

@Component({
  selector: 'app-lycees',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './lycees.component.html',
  styleUrls: ['./lycees.component.scss']
})
export class LyceesComponent implements OnInit {
  lycees: Lycee[] = [];
  loading = false;
  error = '';
  showForm = false;
  editingId: number | null = null;
  lyceeForm: FormGroup;

  constructor(
    private lyceeService: LyceeAdminService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.lyceeForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3)]],
      ville: ['', Validators.required],
      codePostal: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]]
    });
  }

  ngOnInit(): void {
    this.loadLycees();
  }

  loadLycees(): void {
    this.loading = true;
    this.error = '';
    
    this.lyceeService.getAll().subscribe({
      next: (data) => {
        this.lycees = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des lycées';
        this.loading = false;
        console.error(err);
      }
    });
  }

  openCreateForm(): void {
    this.showForm = true;
    this.editingId = null;
    this.lyceeForm.reset();
  }

  openEditForm(lycee: Lycee): void {
    this.showForm = true;
    this.editingId = lycee.id ?? null;
    this.lyceeForm.patchValue({
      nom: lycee.nom,
      ville: lycee.ville,
      codePostal: lycee.codePostal
    });
  }

  closeForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.lyceeForm.reset();
    this.error = '';
  }

  onSubmit(): void {
    if (this.lyceeForm.invalid) {
      Object.keys(this.lyceeForm.controls).forEach(key => {
        this.lyceeForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.error = '';

    const lyceeData = this.lyceeForm.value;

    if (this.editingId) {
      // Update
      this.lyceeService.update(this.editingId, lyceeData).subscribe({
        next: () => {
          this.loading = false;
          this.closeForm();
          this.loadLycees();
        },
        error: (err) => {
          this.error = 'Erreur lors de la modification du lycée';
          this.loading = false;
          console.error(err);
        }
      });
    } else {
      // Create
      this.lyceeService.create(lyceeData).subscribe({
        next: () => {
          this.loading = false;
          this.closeForm();
          this.loadLycees();
        },
        error: (err) => {
          this.error = 'Erreur lors de la création du lycée';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  deleteLycee(id: number, nom: string): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le lycée "${nom}" ?`)) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.lyceeService.delete(id).subscribe({
      next: () => {
        this.loading = false;
        this.loadLycees();
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression du lycée';
        this.loading = false;
        console.error(err);
      }
    });
  }

  deleteAllLycees(): void {
    const confirmMessage = `⚠️ ATTENTION : Voulez-vous vraiment supprimer TOUS les lycées ?\n\n` +
                          `Cela supprimera également :\n` +
                          `- Tous les élèves associés\n` +
                          `- Tous leurs vœux\n\n` +
                          `Cette action est IRRÉVERSIBLE !`;
    
    if (!confirm(confirmMessage)) {
      return;
    }

    if (!confirm('Êtes-vous ABSOLUMENT sûr ? Tapez OK pour confirmer.')) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.lyceeService.deleteAll().subscribe({
      next: (response) => {
        this.loading = false;
        alert(`✅ ${response.lyceesSupprimes} lycées, ${response.elevesSupprimes} élèves et ${response.voeuxSupprimes} vœux supprimés avec succès`);
        this.loadLycees();
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression de tous les lycées';
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
