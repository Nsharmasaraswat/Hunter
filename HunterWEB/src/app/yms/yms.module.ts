import { CommonModule, DecimalPipe } from "@angular/common";
import { NgModule } from "@angular/core";
import { InternalSharedModule } from "../shared/shared.module";
import { NFChoserComponent } from "./components/nfchoser.component";
import { VehicleChoserComponent } from "./components/vehiclechoser.component";
import { LobbyComponent } from "./process/lobby.component";
import { ManageTransportComponent } from "./process/manage-transport.component";
import { YmsRoutingModule } from "./yms-routing.module";

@NgModule({
  imports: [CommonModule, YmsRoutingModule, InternalSharedModule],
  declarations: [ManageTransportComponent, LobbyComponent, NFChoserComponent, VehicleChoserComponent],
  providers: [DecimalPipe]
})

export class YmsModule { }