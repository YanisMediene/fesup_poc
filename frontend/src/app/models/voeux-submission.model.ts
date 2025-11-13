export interface VoeuxSubmission {
  eleveId: number;
  conferenceVoeu1: number;
  conferenceVoeu2: number;
  activiteVoeu3: number;
  activiteVoeu4: number;
  activiteVoeu5: number;
}

export interface VoeuxSubmissionResponse {
  success: boolean;
  message: string;
  dateSoumission: string;
}
