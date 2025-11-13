import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { VoeuxService } from '../services/voeux.service';

@Injectable({
  providedIn: 'root'
})
export class VoeuxGuard implements CanActivate {
  
  constructor(
    private voeuxService: VoeuxService,
    private router: Router
  ) {}
  
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    const eleve = this.voeuxService.getEleveConnecte();
    
    // Si pas d'élève connecté, rediriger vers identification
    if (!eleve) {
      this.router.navigate(['/voeux/identification']);
      return false;
    }
    
    // Si vœux déjà soumis, rediriger vers confirmation
    if (eleve.voeuxDejasoumis && !state.url.includes('confirmation')) {
      this.router.navigate(['/voeux/confirmation']);
      return false;
    }
    
    return true;
  }
}
