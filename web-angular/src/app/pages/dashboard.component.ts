import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, Recommendation } from '../services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <h1>Dashboard</h1>

      <div class="card" *ngIf="rec">
        <h2>Resumo do paciente</h2>
        <div class="grid">
          <div class="stat"><div class="num">{{ rec.totalMeasurements }}</div><div class="lbl">Medicoes</div></div>
          <div class="stat"><div class="num">{{ rec.normalCount }}</div><div class="lbl">Normais</div></div>
          <div class="stat"><div class="num">{{ rec.elevatedCount }}</div><div class="lbl">Elevadas</div></div>
          <div class="stat"><div class="num">{{ rec.hypertensionCount }}</div><div class="lbl">Hipertensao</div></div>
        </div>

        <div style="margin-top:20px;">
          Nivel de risco:
          <span class="badge" [style.background]="riskColor(rec.riskLevel)">{{ rec.riskLevel }}</span>
        </div>
      </div>

      <div class="card" *ngIf="rec">
        <h2>Recomendacoes (AI Logistics Extension)</h2>
        <ul class="rec-list">
          <li *ngFor="let r of rec.recommendations">&bull; {{ r }}</li>
        </ul>
        <p *ngIf="rec.nearestUnit" style="margin-top:12px; color:#6b7280;">
          Unidade mais proxima: <b>{{ rec.nearestUnit.name }}</b> ({{ rec.nearestUnit.type }})
        </p>
      </div>

      <p class="error" *ngIf="error">{{ error }}</p>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  rec: Recommendation | null = null;
  error = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getRecommendations().subscribe({
      next: (r) => (this.rec = r),
      error: () => (this.error = 'Faca login para ver o dashboard.'),
    });
  }

  riskColor(level: string): string {
    switch (level) {
      case 'ALTO': return '#dc2626';
      case 'MODERADO': return '#f59e0b';
      case 'BAIXO': return '#16a34a';
      default: return '#6b7280';
    }
  }
}
