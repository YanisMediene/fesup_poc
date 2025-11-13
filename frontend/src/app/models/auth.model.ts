export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  nom: string;
  prenom: string;
  roles: string[];
}

export interface UserInfo {
  email: string;
  nom: string;
  prenom: string;
  roles: string[];
}
