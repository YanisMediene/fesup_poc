import { Pipe, PipeTransform } from '@angular/core';
import { getDemiJourneeLabel, DemiJournee } from '../models/common.model';

/**
 * Pipe pour afficher le label lisible d'une demi-journée
 * 
 * Utilisation :
 * {{ eleve.demiJournee | demiJourneeLabel }}
 * {{ 'JOUR1_MATIN' | demiJourneeLabel }} -> "Jour 1 - Matin"
 */
@Pipe({
  name: 'demiJourneeLabel',
  standalone: true
})
export class DemiJourneeLabelPipe implements PipeTransform {
  transform(value: DemiJournee | string): string {
    return getDemiJourneeLabel(value as DemiJournee);
  }
}
