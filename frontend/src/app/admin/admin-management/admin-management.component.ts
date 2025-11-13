import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminManagementService, Admin } from '../../services/admin-management.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-management.component.html',
  styleUrls: ['./admin-management.component.scss']
})
export class AdminManagementComponent implements OnInit {
  admins: Admin[] = [];
  adminForm: FormGroup;
  loading = false;
  error = '';
  showForm = false;
  editingId: number | null = null;

  constructor(
    private adminService: AdminManagementService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router
  ) {
    // Vérifier que l'utilisateur est SuperAdmin
    if (!this.authService.isSuperAdmin()) {
      this.router.navigate(['/admin/dashboard']);
    }

    this.adminForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      isSuperAdmin: [false]
    });
  }

  ngOnInit(): void {
    this.loadAdmins();
  }

  loadAdmins(): void {
    this.loading = true;
    this.error = '';
    
    this.adminService.getAllAdmins().subscribe({
      next: (data) => {
        this.admins = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des administrateurs';
        this.loading = false;
        console.error(err);
      }
    });
  }

  openCreateForm(): void {
    this.showForm = true;
    this.editingId = null;
    this.adminForm.reset({ isSuperAdmin: false });
    this.adminForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.adminForm.get('password')?.updateValueAndValidity();
  }

  openEditForm(admin: Admin): void {
    this.showForm = true;
    this.editingId = admin.id ?? null;
    this.adminForm.patchValue({
      email: admin.email,
      nom: admin.nom,
      prenom: admin.prenom,
      password: '',
      isSuperAdmin: admin.isSuperAdmin
    });
    // Le mot de passe devient optionnel en édition
    this.adminForm.get('password')?.clearValidators();
    this.adminForm.get('password')?.updateValueAndValidity();
  }

  closeForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.adminForm.reset();
    this.error = '';
  }

  onSubmit(): void {
    if (this.adminForm.invalid) {
      Object.keys(this.adminForm.controls).forEach(key => {
        this.adminForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.error = '';

    const adminData = this.adminForm.value;

    if (this.editingId) {
      // Mise à jour
      const updateData: Partial<Admin> = {
        email: adminData.email,
        nom: adminData.nom,
        prenom: adminData.prenom,
        isSuperAdmin: adminData.isSuperAdmin
      };
      
      // N'inclure le mot de passe que s'il est fourni
      if (adminData.password) {
        updateData.password = adminData.password;
      }

      this.adminService.updateAdmin(this.editingId, updateData).subscribe({
        next: () => {
          this.loading = false;
          this.closeForm();
          this.loadAdmins();
        },
        error: (err) => {
          this.error = err.error?.error || 'Erreur lors de la mise à jour';
          this.loading = false;
        }
      });
    } else {
      // Création
      this.adminService.createAdmin(adminData).subscribe({
        next: () => {
          this.loading = false;
          this.closeForm();
          this.loadAdmins();
        },
        error: (err) => {
          this.error = err.error?.error || 'Erreur lors de la création';
          this.loading = false;
        }
      });
    }
  }

  deleteAdmin(admin: Admin): void {
    if (!admin.id) return;

    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.email === admin.email) {
      alert('❌ Vous ne pouvez pas supprimer votre propre compte !');
      return;
    }

    if (confirm(`Voulez-vous vraiment supprimer l'administrateur ${admin.prenom} ${admin.nom} ?`)) {
      this.loading = true;
      
      this.adminService.deleteAdmin(admin.id).subscribe({
        next: () => {
          this.loading = false;
          this.loadAdmins();
        },
        error: (err) => {
          this.error = 'Erreur lors de la suppression';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }
}
