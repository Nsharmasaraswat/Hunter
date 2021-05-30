import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { ViewPortalComponent } from "./portal/view-portal.component";

export const routes: Routes = [
    { path: 'view-portal/:procId', component: ViewPortalComponent, runGuardsAndResolvers: 'always' }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class CustomEurofarmaRoutingModule { }