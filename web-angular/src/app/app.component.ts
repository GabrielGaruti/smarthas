import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  template: `
    <nav class="navbar" *ngIf="auth.isLoggedIn">
      <span class="brand">Smart HAS &bull; Admin</span>
      <a routerLink="/dashboard">Dashboard</a>
      <a routerLink="/measurements">Medicoes</a>
      <a routerLink="/units">Unidades</a>
      <span>Ola, {{ auth.user?.fullName }}</span>
      <a href="javascript:void(0)" (click)="logout()">Sair</a>
    </nav>
    <router-outlet></router-outlet>
  `,
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
