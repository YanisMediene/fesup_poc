import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { IdentificationComponent } from './components/identification/identification.component';
import { ConfirmationIdentiteComponent } from './components/confirmation-identite/confirmation-identite.component';
import { FormulaireVoeuxComponent } from './components/formulaire-voeux/formulaire-voeux.component';
import { RecapitulatifVoeuxComponent } from './components/recapitulatif-voeux/recapitulatif-voeux.component';
import { ConfirmationSoumissionComponent } from './components/confirmation-soumission/confirmation-soumission.component';
import { LoginComponent } from './components/login/login.component';

import { VoeuxService } from './services/voeux.service';
import { ActiviteService } from './services/activite.service';
import { AuthService } from './services/auth.service';
import { VoeuxGuard } from './guards/voeux.guard';
import { AuthGuard } from './guards/auth.guard';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { DemiJourneeLabelPipe } from './pipes/demi-journee-label.pipe';

@NgModule({
  declarations: [
    AppComponent,
    IdentificationComponent,
    ConfirmationIdentiteComponent,
    FormulaireVoeuxComponent,
    RecapitulatifVoeuxComponent,
    ConfirmationSoumissionComponent,
    LoginComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    AppRoutingModule,
    DemiJourneeLabelPipe
  ],
  providers: [
    VoeuxService,
    ActiviteService,
    AuthService,
    VoeuxGuard,
    AuthGuard,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
