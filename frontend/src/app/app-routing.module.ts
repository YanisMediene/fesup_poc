import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IdentificationComponent } from './components/identification/identification.component';
import { ConfirmationIdentiteComponent } from './components/confirmation-identite/confirmation-identite.component';
import { FormulaireVoeuxComponent } from './components/formulaire-voeux/formulaire-voeux.component';
import { RecapitulatifVoeuxComponent } from './components/recapitulatif-voeux/recapitulatif-voeux.component';
import { ConfirmationSoumissionComponent } from './components/confirmation-soumission/confirmation-soumission.component';
import { LoginComponent } from './components/login/login.component';
import { VoeuxGuard } from './guards/voeux.guard';

const routes: Routes = [
  { 
    path: '', 
    redirectTo: '/voeux/identification', 
    pathMatch: 'full' 
  },
  { 
    path: 'login', 
    component: LoginComponent 
  },
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule)
  },
  { 
    path: 'voeux/identification', 
    component: IdentificationComponent 
  },
  { 
    path: 'voeux/confirmation-identite', 
    component: ConfirmationIdentiteComponent,
    canActivate: [VoeuxGuard]
  },
  { 
    path: 'voeux/formulaire', 
    component: FormulaireVoeuxComponent,
    canActivate: [VoeuxGuard]
  },
  { 
    path: 'voeux/recapitulatif', 
    component: RecapitulatifVoeuxComponent,
    canActivate: [VoeuxGuard]
  },
  { 
    path: 'voeux/confirmation', 
    component: ConfirmationSoumissionComponent
  },
  {
    path: '**',
    redirectTo: '/voeux/identification'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
