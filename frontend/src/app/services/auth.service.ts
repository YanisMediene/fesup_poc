import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, UserInfo } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly USER_KEY = 'user_info';
  
  private currentUserSubject = new BehaviorSubject<UserInfo | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }
  
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          this.saveToken(response.token);
          const userInfo: UserInfo = {
            email: response.email,
            nom: response.nom,
            prenom: response.prenom,
            roles: response.roles
          };
          this.saveUserInfo(userInfo);
          this.currentUserSubject.next(userInfo);
        })
      );
  }
  
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }
  
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
  
  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }
  
  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.roles.includes('ROLE_ADMIN') : false;
  }
  
  isSuperAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.roles.includes('ROLE_SUPERADMIN') : false;
  }
  
  getCurrentUser(): UserInfo | null {
    return this.currentUserSubject.value;
  }
  
  private saveToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }
  
  private saveUserInfo(userInfo: UserInfo): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(userInfo));
  }
  
  private loadUserFromStorage(): void {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSubject.next(user);
      } catch (e) {
        localStorage.removeItem(this.USER_KEY);
      }
    }
  }
}
