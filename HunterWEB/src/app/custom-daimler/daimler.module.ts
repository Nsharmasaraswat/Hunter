// Angular Imports
// This Module's Components
import { CommonModule } from "@angular/common";
import { NgModule } from '@angular/core';
import { LeafletMarkerClusterModule } from '@asymmetrik/ngx-leaflet-markercluster';
import { ReportModule } from "../report/report.module";
import { InternalSharedModule } from "../shared/shared.module";
import { DaimlerRoutingModule } from "./daimler-routing.module";
import { VolumeInventoryComponent } from "./report/volume-inventory.component";

@NgModule({
  imports: [DaimlerRoutingModule, ReportModule, InternalSharedModule, CommonModule, LeafletMarkerClusterModule],
  declarations: [VolumeInventoryComponent],
  providers: [DaimlerRoutingModule, LeafletMarkerClusterModule]
})

export class DaimlerModule { }
