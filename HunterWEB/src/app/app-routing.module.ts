import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './security/guards/auth.guard';
import { SecurityModule } from './security/security.module';
import { HomeComponent } from './_home/home.component';


export const routes: Routes = [
  { path: '', redirectTo: 'security/login', pathMatch: 'full' },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'gis', loadChildren: 'app/gis/gis.module#GisModule' },
      { path: 'yms', loadChildren: 'app/yms/yms.module#YmsModule' },
      { path: 'core', loadChildren: 'app/core/core.module#CoreModule' },
      { path: 'report', loadChildren: 'app/report/report.module#ReportModule' },
      { path: 'process', loadChildren: 'app/process/process.module#ProcessModule' },
      { path: 'custom-ip', loadChildren: 'app/custom-ip/ip.module#CustomIpModule' },
      { path: 'custom-solar', loadChildren: 'app/custom-solar/solar.module#SolarModule' },
      { path: 'custom-daimler', loadChildren: 'app/custom-daimler/daimler.module#DaimlerModule' },
      { path: 'custom-descarpack', loadChildren: 'app/custom-descarpack/descarpack.module#DescarpackModule' },
      { path: 'custom-eurofarma', loadChildren: 'app/custom-eurofarma/eurofarma.module#CustomEurofarmaModule' }
    ],
    runGuardsAndResolvers: 'always'
  },
  { path: 'security', loadChildren: 'app/security/security.module#SecurityModule' }
];

@NgModule({
  imports: [SecurityModule, RouterModule.forRoot(routes, { onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }