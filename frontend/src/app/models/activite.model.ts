export interface Activite {
  id: number;
  titre: string;
  description: string;
  type: string;
  demiJournee: string;
  capaciteMax: number;
}

export interface ActivitesGroupees {
  conferences: Activite[];
  tablesRondes: Activite[];
  flashsMetiers: Activite[];
}
