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
}
