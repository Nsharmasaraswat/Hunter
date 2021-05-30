import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { LobbyComponent } from "./process/lobby.component";
import { ManageTransportComponent } from "./process/manage-transport.component";

export const routes: Routes = [
    { path: 'manageTransport', component: ManageTransportComponent },
    { path: 'manageTransport/:docId', component: ManageTransportComponent },
    { path: 'manageTransport/:restricted/', component: ManageTransportComponent },
    { path: 'manageTransport/:restricted/:docId', component: ManageTransportComponent },
    { path: 'lobby', component: LobbyComponent }

];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class YmsRoutingModule { }