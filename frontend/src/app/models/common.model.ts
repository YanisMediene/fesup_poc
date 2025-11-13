/**
 * Enum des demi-journées (synchronisé avec le backend)
 */
export enum DemiJournee {
  JOUR1_MATIN = 'JOUR1_MATIN',
  JOUR1_APRES_MIDI = 'JOUR1_APRES_MIDI',
  JOUR2_MATIN = 'JOUR2_MATIN',
  JOUR2_APRES_MIDI = 'JOUR2_APRES_MIDI'
}

/**
 * Option de sélection pour les dropdowns de demi-journée
 */
export interface DemiJourneeOption {
  value: DemiJournee;
  label: string;
}

/**
 * Liste des options de demi-journée pour les dropdowns
 */
export const DEMI_JOURNEE_OPTIONS: DemiJourneeOption[] = [
  { value: DemiJournee.JOUR1_MATIN, label: 'Jour 1 - Matin' },
  { value: DemiJournee.JOUR1_APRES_MIDI, label: 'Jour 1 - Après-midi' },
  { value: DemiJournee.JOUR2_MATIN, label: 'Jour 2 - Matin' },
  { value: DemiJournee.JOUR2_APRES_MIDI, label: 'Jour 2 - Après-midi' }
];

/**
 * Obtenir le label lisible d'une demi-journée
 * @param demiJournee La valeur de l'enum DemiJournee
 * @returns Le label formaté ou la valeur brute si non trouvée
 */
export function getDemiJourneeLabel(demiJournee: DemiJournee | string): string {
  if (!demiJournee) return '';
  
  const option = DEMI_JOURNEE_OPTIONS.find(o => o.value === demiJournee);
  return option ? option.label : String(demiJournee);
}
