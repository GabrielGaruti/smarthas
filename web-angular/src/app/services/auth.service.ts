import { Injectable } from '@angular/core';

export interface AppUser {
  id: number;
  email: string;
  fullName: string;
  role: string;
}

/** Guarda o token JWT e o usuario logado no localStorage. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'smarthas_token';
  private readonly userKey = 'smarthas_user';

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get user(): AppUser | null {
    const raw = localStorage.getItem(this.userKey);
    return raw ? (JSON.parse(raw) as AppUser) : null;
  }

  get isLoggedIn(): boolean {
    return !!this.token;
  }

  get isAdmin(): boolean {
    return this.user?.role === 'ADMIN';
  }

  setSession(token: string, user: AppUser): void {
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.userKey, JSON.stringify(user));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }
}
