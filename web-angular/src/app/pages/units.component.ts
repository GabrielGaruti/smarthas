import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService, HealthUnit } from '../services/api.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-units',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h1>Unidades de saude</h1>

      <div class="card">
        <h2>Cadastradas</h2>
        <table>
          <thead>
            <tr><th>Nome</th><th>Tipo</th><th>Endereco</th><th>Status</th><th *ngIf="auth.isAdmin"></th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let u of units">
              <td>{{ u.name }}</td>
              <td>{{ u.type }}</td>
              <td>{{ u.address }}</td>
              <td>
                <span class="badge" [style.background]="u.active ? '#16a34a' : '#6b7280'">
                  {{ u.active ? 'Ativa' : 'Inativa' }}
                </span>
              </td>
              <td *ngIf="auth.isAdmin">
                <button class="danger" (click)="remove(u.id)">Excluir</button>
              </td>
            </tr>
            <tr *ngIf="units.length === 0">
              <td colspan="5" style="color:#6b7280;">Nenhuma unidade cadastrada.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card" *ngIf="auth.isAdmin">
        <h2>Nova unidade (somente admin)</h2>
        <div class="grid">
          <div style="flex:2; min-width:180px;">
            <label>Nome</label>
            <input type="text" [(ngModel)]="form.name" />
          </div>
          <div style="flex:1; min-width:140px;">
            <label>Tipo</label>
            <select [(ngModel)]="form.type">
              <option value="HOSPITAL">HOSPITAL</option>
              <option value="CLINIC">CLINIC</option>
              <option value="SENSOR">SENSOR</option>
            </select>
          </div>
        </div>
        <div class="grid">
          <div style="flex:1; min-width:140px;">
            <label>Latitude</label>
            <input type="number" [(ngModel)]="form.latitude" />
          </div>
          <div style="flex:1; min-width:140px;">
            <label>Longitude</label>
            <input type="number" [(ngModel)]="form.longitude" />
          </div>
        </div>
        <label>Endereco</label>
        <input type="text" [(ngModel)]="form.address" />
        <div style="margin-top:16px;">
          <button (click)="add()">Cadastrar</button>
        </div>
        <p class="error" *ngIf="error">{{ error }}</p>
      </div>

      <p *ngIf="!auth.isAdmin" style="color:#6b7280;">
        Entre como administrador para cadastrar ou remover unidades.
      </p>
    </div>
  `,
})
export class UnitsComponent implements OnInit {
  units: HealthUnit[] = [];
  error = '';
  form = {
    name: '',
    type: 'HOSPITAL',
    latitude: -23.5505,
    longitude: -46.6333,
    address: '',
    active: true,
  };

  constructor(private api: ApiService, public auth: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.getUnits().subscribe({
      next: (list) => (this.units = list),
      error: () => (this.error = 'Erro ao carregar unidades.'),
    });
  }

  add(): void {
    this.error = '';
    this.api.createUnit(this.form).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => (this.error = err.error?.detail ?? 'Erro ao cadastrar.'),
    });
  }

  remove(id: number): void {
    this.api.deleteUnit(id).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => (this.error = err.error?.detail ?? 'Erro ao excluir.'),
    });
  }
}
