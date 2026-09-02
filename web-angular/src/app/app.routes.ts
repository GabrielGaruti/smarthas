import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login.component';
import { DashboardComponent } from './pages/dashboard.component';
import { MeasurementsComponent } from './pages/measurements.component';
import { UnitsComponent } from './pages/units.component';

/** Rotas simples de navegacao entre telas. */
export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'measurements', component: MeasurementsComponent },
  { path: 'units', component: UnitsComponent },
  { path: '**', redirectTo: 'login' },
];
