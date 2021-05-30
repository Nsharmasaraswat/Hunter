import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { VolumeInventoryComponent } from "./report/volume-inventory.component";

export const routes: Routes = [
    { path: 'volume/:docId', component: VolumeInventoryComponent, runGuardsAndResolvers: 'always' }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})

export class DaimlerRoutingModule { }