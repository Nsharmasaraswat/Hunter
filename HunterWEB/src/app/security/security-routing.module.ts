import { NgModule }             from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { LoginComponent }        from './components/login.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SecurityRoutingModule {}