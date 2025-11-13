import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../guards/auth.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ImportComponent } from './import/import.component';
import { ActivitesComponent } from './activites/activites.component';
import { SallesComponent } from './salles/salles.component';
import { LyceesComponent } from './lycees/lycees.component';
import { ElevesComponent } from './eleves/eleves.component';
import { AdminCreneauxComponent } from './creneaux/admin-creneaux.component';
import { GestionSessionsComponent } from '../components/gestion-sessions/gestion-sessions.component';
import { ResultatsAffectationComponent } from '../components/resultats-affectation/resultats-affectation.component';
import { AdminManagementComponent } from './admin-management/admin-management.component';
import { SystemSettingsComponent } from './system-settings/system-settings.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'import', component: ImportComponent },
      { path: 'eleves', component: ElevesComponent },
      { path: 'activites', component: ActivitesComponent },
      { path: 'salles', component: SallesComponent },
      { path: 'lycees', component: LyceesComponent },
      { path: 'creneaux', component: AdminCreneauxComponent },
      { path: 'sessions', component: GestionSessionsComponent },
      { path: 'affectations', component: ResultatsAffectationComponent },
      { path: 'admin-management', component: AdminManagementComponent },
      { path: 'system-settings', component: SystemSettingsComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
