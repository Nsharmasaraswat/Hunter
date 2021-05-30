// Angular Imports
// This Module's Components
import { CommonModule } from "@angular/common";
import { NgModule } from '@angular/core';
import { LeafletMarkerClusterModule } from '@asymmetrik/ngx-leaflet-markercluster';
import { ReportModule } from "../report/report.module";
import { InternalSharedModule } from "../shared/shared.module";
import { FailedConferenceComponent } from "./conference/failed-conference.component";
import { RomaneioComponent } from './conference/romaneio.component';
import { UnlockCheckingComponent } from "./conference/unlock-checking.component";
import { CheckInventoryComponent } from "./inventory/check-inventory.component";
import { CreateInventoryComponent } from "./inventory/create-inventory.component";
import { SAPInventoryComponent } from "./inventory/sap-inventory.component";
import { RealPickingComponent } from "./picking/realpicking.component";
import { PrintInboundComponent } from "./print/print-inbound.component";
import { PrintInventoryComponent } from "./print/print-inventory.component";
import { ReprintComponent } from "./print/reprint.component";
import { ProductionProcessComponent } from './process/production-process.component';
import { CreateRNCComponent } from "./quality/create-rnc.component";
import { QualityControlComponent } from "./quality/quality-control.component";
import { SapTest } from './quality/sap-test-cq.component';
import { TraceabilityComponent } from "./quality/traceability.component";
import { WarehouseMapComponent } from "./rtls/warehouse-map.component";
import { SolarRoutingModule } from "./solar-routing.module";
import { CreateTransferOldComponent } from "./transfer/create-transfer-old.component";
import { CreateTransferComponent } from "./transfer/create-transfer.component";
import { AddressBlockComponent } from './warehouse/address-block.component';
import { GenerateTransportComponent } from './warehouse/generate-transport.component';
import { ManageAddressComponent } from './warehouse/manage-address.component';
import { ProductShortageComponent } from './warehouse/product-shortage.component';
import { ResupplyComponent } from "./warehouse/resupply.component";
import { StorageTemplateComponent } from "./warehouse/storage-template.component";
import { ViewPalletHistoryComponent } from "./warehouse/view-pallet-history.component";
import { VisualOcupationComponent } from "./warehouse/visual-ocupation.component";
import { ProductionQualityComponent } from "./quality/production-quality.component";

@NgModule({
  imports: [SolarRoutingModule, ReportModule, InternalSharedModule, CommonModule, LeafletMarkerClusterModule],
  declarations: [FailedConferenceComponent, PrintInboundComponent, PrintInventoryComponent, SapTest, ResupplyComponent, RealPickingComponent,
    CreateTransferOldComponent, CreateTransferComponent, WarehouseMapComponent, QualityControlComponent, ReprintComponent, GenerateTransportComponent,
    CreateRNCComponent, RomaneioComponent, ProductionProcessComponent, AddressBlockComponent, ManageAddressComponent, ProductShortageComponent, SAPInventoryComponent,
    VisualOcupationComponent, StorageTemplateComponent, CreateInventoryComponent, CheckInventoryComponent, UnlockCheckingComponent, ViewPalletHistoryComponent, TraceabilityComponent, 
    ProductionQualityComponent],
  providers: [SolarRoutingModule, LeafletMarkerClusterModule]
})

export class SolarModule { }
