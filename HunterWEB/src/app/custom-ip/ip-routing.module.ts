import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { RegisterTruckComponent } from "./temp/register-truck.component";

export const routes: Routes = [
    { path: 'registerTruck/:productModel', component: RegisterTruckComponent, runGuardsAndResolvers: 'always' }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class CustomIpRoutingModule { }