import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppUser } from './auth.service';

export interface Measurement {
  id: number;
  systolic: number;
  diastolic: number;
  date: string;
  time: string;
  notes?: string;
  createdAt: string;
  classification: string;
  classificationLabel: string;
  colorHex: string;
  heartRate?: number;
  source?: string;            // MANUAL | SENSOR
  alertSeverity?: string;     // preenchido quando a procedure gera alerta
  alertMessage?: string;
}

export interface HealthUnit {
  id: number;
  name: string;
  type: string;
  latitude: number;
  longitude: number;
  address?: string;
  active: boolean;
}

export interface Recommendation {
  totalMeasurements: number;
  normalCount: number;
  elevatedCount: number;
  hypertensionCount: number;
  riskLevel: string;
  recommendations: string[];
  nearestUnit?: HealthUnit;
  controlRate30d?: number | null;  // FN_SHAS_TAXA_CONTROLE
  patientSummary?: string;         // FN_SHAS_RESUMO_PACIENTE
  openAlerts: number;
  rulesEngine: string;             // ORACLE_PLSQL | JAVA_FALLBACK
}

export interface ClinicalAlert {
  id: number;
  userId: number;
  userName: string;
  measurementId: number;
  type: string;
  severity: string;
  message: string;
  status: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface SummaryReport {
  id: number;
  userId: number;
  userName: string;
  startDate: string;
  endDate: string;
  measurementCount: number;
  avgSystolic?: number;
  avgDiastolic?: number;
  maxSystolic?: number;
  controlRate?: number;
  openAlerts: number;
  riskLevel: string;
  summary: string;
  generatedAt: string;
}

export interface ReportRun {
  engine: string;
  days: number;
  generated: number;
  elapsedMs: number;
  reports: SummaryReport[];
}

export interface PatientIndicator {
  userId: number;
  fullName: string;
  controlRate?: number | null;
  summary: string;
}

export interface LoginResponse {
  token: string;
  user: AppUser;
}

/** Servico central que consome a API REST (Spring Boot) via HttpClient. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  // Ajuste aqui se o backend estiver em outra maquina/porta.
  private readonly base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/auth/login`, { email, password });
  }

  register(fullName: string, email: string, password: string): Observable<unknown> {
    return this.http.post(`${this.base}/auth/register`, { fullName, email, password });
  }

  getMeasurements(): Observable<Measurement[]> {
    return this.http.get<Measurement[]>(`${this.base}/measurements`);
  }

  createMeasurement(payload: Partial<Measurement>): Observable<Measurement> {
    return this.http.post<Measurement>(`${this.base}/measurements`, payload);
  }

  deleteMeasurement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/measurements/${id}`);
  }

  getUnits(): Observable<HealthUnit[]> {
    return this.http.get<HealthUnit[]>(`${this.base}/units`);
  }

  createUnit(payload: Partial<HealthUnit>): Observable<HealthUnit> {
    return this.http.post<HealthUnit>(`${this.base}/units`, payload);
  }

  deleteUnit(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/units/${id}`);
  }

  getRecommendations(): Observable<Recommendation> {
    return this.http.get<Recommendation>(`${this.base}/recommendations`);
  }

  // ---------- Fase 6: alertas, relatorios e indicadores (Oracle PL/SQL) ----------

  getAllAlerts(status?: string): Observable<ClinicalAlert[]> {
    const q = status ? `?status=${status}` : '';
    return this.http.get<ClinicalAlert[]>(`${this.base}/admin/alerts${q}`);
  }

  resolveAlert(id: number): Observable<ClinicalAlert> {
    return this.http.patch<ClinicalAlert>(`${this.base}/alerts/${id}/resolve`, {});
  }

  generateReports(days: number): Observable<ReportRun> {
    return this.http.post<ReportRun>(`${this.base}/admin/reports/summary?days=${days}`, {});
  }

  getReports(): Observable<SummaryReport[]> {
    return this.http.get<SummaryReport[]>(`${this.base}/admin/reports`);
  }

  getIndicators(days: number): Observable<PatientIndicator[]> {
    return this.http.get<PatientIndicator[]>(`${this.base}/admin/patients/indicators?days=${days}`);
  }
}
