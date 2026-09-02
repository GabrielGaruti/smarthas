import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../services/api.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-wrap">
      <div class="card">
        <h1>Smart HAS</h1>
        <p style="color:#6b7280; margin-bottom:8px;">Painel administrativo</p>

        <label for="email">E-mail</label>
        <input id="email" type="email" [(ngModel)]="email" placeholder="admin@smarthas.com" />

        <label for="password">Senha</label>
        <input id="password" type="password" [(ngModel)]="password" placeholder="admin123" />

        <div style="margin-top:16px;">
          <button (click)="onLogin()" [disabled]="loading">
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>
        </div>

        <p class="error" *ngIf="error">{{ error }}</p>

        <p style="margin-top:16px; font-size:13px; color:#6b7280;">
          Demo: <b>admin&#64;smarthas.com / admin123</b> (admin) ou
          <b>paciente&#64;smarthas.com / 123456</b> (usuario).
        </p>
      </div>
    </div>
  `,
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private router: Router,
  ) {}

  onLogin(): void {
    this.error = '';
    this.loading = true;
    this.api.login(this.email, this.password).subscribe({
      next: (res) => {
        this.auth.setSession(res.token, res.user);
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.error = err.error?.detail ?? 'Falha ao entrar. Verifique suas credenciais.';
      },
    });
  }
}
