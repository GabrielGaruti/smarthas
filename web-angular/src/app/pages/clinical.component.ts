import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ApiService, ClinicalAlert, PatientIndicator, ReportRun, SummaryReport,
} from '../services/api.service';

/**
 * Fase 6 - Tela administrativa ligada a camada Oracle:
 *  - Indicadores por paciente: functions FN_SHAS_TAXA_CONTROLE / FN_SHAS_RESUMO_PACIENTE
 *  - Relatorio: botao executa PRC_SHAS_GERAR_RELATORIO_RESUMO (Angular -> REST -> Java -> JDBC -> Oracle)
 *  - Alertas: registros gerados por PRC_SHAS_REGISTRAR_ALERTA
 */
@Component({
  selector: 'app-clinical',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h1>Alertas &amp; Relatorios</h1>

      <div class="card">
        <h2>Indicadores por paciente</h2>
        <div class="toolbar">
          <div>
            <label>Periodo</label>
            <select [(ngModel)]="days" (change)="loadIndicators()">
              <option *ngFor="let d of periodOptions" [ngValue]="d">{{ d }} dias</option>
            </select>
          </div>
        </div>
        <table>
          <thead><tr><th>Paciente</th><th>Na meta</th><th>Resumo (FN_SHAS_RESUMO_PACIENTE)</th></tr></thead>
          <tbody>
            <tr *ngFor="let p of indicators">
              <td>{{ p.fullName }}</td>
              <td><span class="badge" [style.background]="rateColor(p.controlRate)">
                {{ p.controlRate != null ? (p.controlRate | number:'1.1-1') + '%' : 'sem dados' }}</span></td>
              <td class="muted">{{ p.summary }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card">
        <h2>Relatorio resumido (procedure)</h2>
        <div class="toolbar">
          <button (click)="runReport()" [disabled]="running">
            {{ running ? 'Executando...' : 'Gerar relatorio dos ultimos ' + days + ' dias' }}
          </button>
          <span class="muted" *ngIf="lastRun">
            {{ lastRun.generated }} relatorio(s) em {{ lastRun.elapsedMs }} ms &bull; motor: <b>{{ lastRun.engine }}</b>
          </span>
        </div>
        <table>
          <thead>
            <tr><th>#</th><th>Paciente</th><th>Leituras</th><th>Media</th><th>Max. sis.</th>
                <th>Na meta</th><th>Alertas</th><th>Risco</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of reports">
              <td>{{ r.id }}</td>
              <td>{{ r.userName }}</td>
              <td>{{ r.measurementCount }}</td>
              <td>{{ r.avgSystolic ?? '-' }}/{{ r.avgDiastolic ?? '-' }}</td>
              <td>{{ r.maxSystolic ?? '-' }}</td>
              <td>{{ r.controlRate != null ? r.controlRate + '%' : '-' }}</td>
              <td>{{ r.openAlerts }}</td>
              <td><span class="badge" [style.background]="riskColor(r.riskLevel)">{{ r.riskLevel }}</span></td>
            </tr>
            <tr *ngIf="reports.length === 0"><td colspan="8" class="muted">Nenhum relatorio gerado ainda.</td></tr>
          </tbody>
        </table>
      </div>

      <div class="card">
        <h2>Alertas clinicos</h2>
        <div class="toolbar">
          <div>
            <label>Status</label>
            <select [(ngModel)]="status" (change)="loadAlerts()">
              <option value="ABERTO">Abertos</option>
              <option value="RESOLVIDO">Resolvidos</option>
              <option value="">Todos</option>
            </select>
          </div>
        </div>
        <table>
          <thead><tr><th>Severidade</th><th>Paciente</th><th>Mensagem</th><th>Quando</th><th></th></tr></thead>
          <tbody>
            <tr *ngFor="let a of alerts">
              <td><span class="badge" [ngClass]="'sev-' + a.severity">{{ a.severity }}</span></td>
              <td>{{ a.userName }}</td>
              <td>{{ a.message }}</td>
              <td>{{ a.createdAt | date:'dd/MM HH:mm' }}</td>
              <td>
                <button *ngIf="a.status === 'ABERTO'" (click)="resolve(a)">Resolver</button>
                <span *ngIf="a.status !== 'ABERTO'" class="ok">Resolvido</span>
              </td>
            </tr>
            <tr *ngIf="alerts.length === 0"><td colspan="5" class="muted">Nenhum alerta.</td></tr>
          </tbody>
        </table>
      </div>

      <p class="error" *ngIf="error">{{ error }}</p>
    </div>
  `,
})
export class ClinicalComponent implements OnInit {
  periodOptions = [7, 30, 90];
  days = 30;
  status = 'ABERTO';
  indicators: PatientIndicator[] = [];
  reports: SummaryReport[] = [];
  alerts: ClinicalAlert[] = [];
  lastRun: ReportRun | null = null;
  running = false;
  error = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadIndicators();
    this.api.getReports().subscribe({ next: (r) => (this.reports = r), error: (e) => this.fail(e) });
    this.loadAlerts();
  }

  loadIndicators(): void {
    this.api.getIndicators(this.days).subscribe({ next: (l) => (this.indicators = l), error: (e) => this.fail(e) });
  }

  loadAlerts(): void {
    this.api.getAllAlerts(this.status || undefined).subscribe({ next: (l) => (this.alerts = l), error: (e) => this.fail(e) });
  }

  runReport(): void {
    this.running = true;
    this.error = '';
    this.api.generateReports(this.days).subscribe({
      next: (run) => {
        this.lastRun = run;
        this.reports = run.reports;
        this.running = false;
      },
      error: (e) => { this.running = false; this.fail(e); },
    });
  }

  resolve(a: ClinicalAlert): void {
    this.api.resolveAlert(a.id).subscribe({ next: () => this.loadAlerts(), error: (e) => this.fail(e) });
  }

  rateColor(rate?: number | null): string {
    if (rate == null) return '#6b7280';
    if (rate < 50) return '#dc2626';
    if (rate < 80) return '#f59e0b';
    return '#16a34a';
  }

  riskColor(level: string): string {
    switch (level) {
      case 'ALTO': return '#dc2626';
      case 'MODERADO': return '#f59e0b';
      case 'BAIXO': return '#16a34a';
      default: return '#6b7280';
    }
  }

  private fail(err: HttpErrorResponse): void {
    this.error = err.status === 403
      ? 'Area restrita ao perfil ADMIN (entre com admin@smarthas.com).'
      : err.error?.detail ?? 'Erro ao comunicar com a API.';
  }
}
