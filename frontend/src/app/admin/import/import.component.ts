import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AdminImportService, ImportReport } from '../../services/admin-import.service';

type ImportType = 'eleves' | 'activites' | 'salles';

@Component({
  selector: 'app-import',
  templateUrl: './import.component.html',
  styleUrls: ['./import.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class ImportComponent {
  selectedType: ImportType | null = null;
  selectedFile: File | null = null;
  loading = false;
  report: ImportReport | null = null;
  error = '';

  constructor(
    private importService: AdminImportService,
    private router: Router
  ) {}

  selectType(type: ImportType) {
    this.selectedType = type;
    this.selectedFile = null;
    this.report = null;
    this.error = '';
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type === 'text/csv') {
      this.selectedFile = file;
      this.error = '';
    } else {
      this.error = 'Veuillez sélectionner un fichier CSV valide';
      this.selectedFile = null;
    }
  }

  uploadFile() {
    if (!this.selectedFile || !this.selectedType) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.report = null;

    let uploadObservable;
    switch (this.selectedType) {
      case 'eleves':
        uploadObservable = this.importService.importEleves(this.selectedFile);
        break;
      case 'activites':
        uploadObservable = this.importService.importActivites(this.selectedFile);
        break;
      case 'salles':
        uploadObservable = this.importService.importSalles(this.selectedFile);
        break;
    }

    uploadObservable.subscribe({
      next: (report) => {
        this.report = report;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'import';
        this.loading = false;
      }
    });
  }

  reset() {
    this.selectedType = null;
    this.selectedFile = null;
    this.report = null;
    this.error = '';
  }

  goBack() {
    this.router.navigate(['/admin/dashboard']);
  }
}
