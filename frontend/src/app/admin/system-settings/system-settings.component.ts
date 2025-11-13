import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SystemSettingsService, SystemStats } from '../../services/system-settings.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-system-settings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './system-settings.component.html',
  styleUrls: ['./system-settings.component.scss']
})
export class SystemSettingsComponent implements OnInit {
  stats: SystemStats | null = null;
  loading = false;
  error = '';

  constructor(
    private systemService: SystemSettingsService,
    private authService: AuthService,
    private router: Router
  ) {
    // Vérifier que l'utilisateur est SuperAdmin
    if (!this.authService.isSuperAdmin()) {
      this.router.navigate(['/admin/dashboard']);
    }
  }

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.error = '';
    
    this.systemService.getSystemStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des statistiques';
        this.loading = false;
        console.error(err);
      }
    });
  }

  purgeAllData(): void {
    const confirmMessage = `⚠️ DANGER ABSOLU ⚠️\n\n` +
                          `Vous êtes sur le point de SUPPRIMER TOUTES LES DONNÉES :\n\n` +
                          `- Tous les vœux des élèves\n` +
                          `- Toutes les affectations\n` +
                          `- Toutes les sessions\n` +
                          `- Tous les élèves\n` +
                          `- Toutes les activités\n` +
                          `- Toutes les salles\n` +
                          `- Tous les créneaux\n` +
                          `- Tous les lycées\n\n` +
                          `⚠️ LES COMPTES ADMINISTRATEURS SERONT PRÉSERVÉS ⚠️\n\n` +
                          `Cette action est ABSOLUMENT IRRÉVERSIBLE !!\n\n` +
                          `Tapez OK pour confirmer.`;
    
    if (!confirm(confirmMessage)) {
      return;
    }

    const secondConfirm = prompt(
      'Pour confirmer la suppression totale, tapez exactement : SUPPRIMER TOUT'
    );
    
    if (secondConfirm !== 'SUPPRIMER TOUT') {
      alert('❌ Annulation : texte de confirmation incorrect');
      return;
    }

    this.loading = true;
    this.error = '';

    this.systemService.purgeAllData().subscribe({
      next: (response) => {
        this.loading = false;
        const counts = response.counts;
        alert(
          `✅ Toutes les données ont été supprimées !\n\n` +
          `Détails :\n` +
          `- ${counts.voeux} vœux\n` +
          `- ${counts.affectations} affectations\n` +
          `- ${counts.sessions} sessions\n` +
          `- ${counts.eleves} élèves\n` +
          `- ${counts.activites} activités\n` +
          `- ${counts.salles} salles\n` +
          `- ${counts.creneaux} créneaux\n` +
          `- ${counts.lycees} lycées`
        );
        this.loadStats();
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression : ' + (err.error?.message || err.message);
        this.loading = false;
        console.error(err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }

  // Méthodes d'export
  exportEleves(): void {
    this.systemService.exportEleves();
  }

  exportActivites(): void {
    this.systemService.exportActivites();
  }

  exportSalles(): void {
    this.systemService.exportSalles();
  }

  exportCreneaux(): void {
    this.systemService.exportCreneaux();
  }

  exportLycees(): void {
    this.systemService.exportLycees();
  }

  exportVoeux(): void {
    this.systemService.exportVoeux();
  }

  exportSessions(): void {
    this.systemService.exportSessions();
  }

  exportAffectations(): void {
    this.systemService.exportAffectations();
  }

  exportAll(): void {
    this.systemService.exportAll();
  }
}
