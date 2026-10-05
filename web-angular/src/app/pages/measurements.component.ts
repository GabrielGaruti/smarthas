import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService, Measurement } from '../services/api.service';

@Component({
  selector: 'app-measurements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h1>Medicoes</h1>

      <div class="card">
        <h2>Nova medicao</h2>
        <div class="grid">
          <div style="flex:1; min-width:120px;">
            <label>Sistolica</label>
            <input type="number" [(ngModel)]="form.systolic" />
          </div>
          <div style="flex:1; min-width:120px;">
            <label>Diastolica</label>
            <input type="number" [(ngModel)]="form.diastolic" />
          </div>
          <div style="flex:1; min-width:140px;">
            <label>Data</label>
            <input type="date" [(ngModel)]="form.date" />
          </div>
          <div style="flex:1; min-width:120px;">
            <label>Hora</label>
            <input type="time" [(ngModel)]="form.time" />
          </div>
        </div>
        <label>Observacoes</label>
        <input type="text" [(ngModel)]="form.notes" placeholder="opcional" />
        <div style="margin-top:16px;">
          <button (click)="add()">Adicionar</button>
        </div>
        <p class="error" *ngIf="error">{{ error }}</p>
        <p *ngIf="lastAlert" class="alert-box">
          <b>Alerta {{ lastAlert.severity }}:</b> {{ lastAlert.message }}
        </p>
      </div>

      <div class="card">
        <h2>Historico</h2>
        <table>
          <thead>
            <tr><th>Pressao</th><th>Data</th><th>Hora</th><th>Classificacao</th><th>Origem</th><th>Notas</th><th></th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let m of measurements">
              <td>{{ m.systolic }}/{{ m.diastolic }} mmHg</td>
              <td>{{ m.date }}</td>
              <td>{{ m.time }}</td>
              <td><span class="badge" [style.background]="m.colorHex">{{ m.classificationLabel }}</span></td>
              <td>{{ m.source === 'SENSOR' ? 'Sensor IoT' : 'Manual' }}</td>
              <td>{{ m.notes }}</td>
              <td><button class="danger" (click)="remove(m.id)">Excluir</button></td>
            </tr>
            <tr *ngIf="measurements.length === 0">
              <td colspan="7" style="color:#6b7280;">Nenhuma medicao registrada.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class MeasurementsComponent implements OnInit {
  measurements: Measurement[] = [];
  error = '';
  lastAlert: { severity: string; message: string } | null = null;
  form = {
    systolic: 120,
    diastolic: 80,
    date: new Date().toISOString().substring(0, 10),
    time: '08:00',
    notes: '',
  };

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.getMeasurements().subscribe({
      next: (list) => (this.measurements = list),
      error: () => (this.error = 'Faca login para ver as medicoes.'),
    });
  }

  add(): void {
    this.error = '';
    this.lastAlert = null;
    this.api.createMeasurement(this.form).subscribe({
      next: (m) => {
        // alerta gerado no back-end pela procedure PRC_SHAS_REGISTRAR_ALERTA
        if (m.alertSeverity && m.alertMessage) {
          this.lastAlert = { severity: m.alertSeverity, message: m.alertMessage };
        }
        this.load();
      },
      error: (err: HttpErrorResponse) => (this.error = err.error?.detail ?? 'Erro ao salvar.'),
    });
  }

  remove(id: number): void {
    this.api.deleteMeasurement(id).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => (this.error = err.error?.detail ?? 'Erro ao excluir.'),
    });
  }
}
