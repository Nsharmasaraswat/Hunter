import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { FailedConferenceComponent } from "./conference/failed-conference.component";
import { RomaneioComponent } from './conference/romaneio.component';
import { UnlockCheckingComponent } from "./conference/unlock-checking.component";
import { CheckInventoryComponent } from './inventory/check-inventory.component';
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
import { CreateTransferOldComponent } from "./transfer/create-transfer-old.component";
import { CreateTransferComponent } from "./transfer/create-transfer.component";
import { AddressBlockComponent } from './warehouse/address-block.component';
import { GenerateTransportComponent } from './warehouse/generate-transport.component';
import { ManageAddressComponent } from './warehouse/manage-address.component';
import { ProductShortageComponent } from "./warehouse/product-shortage.component";
import { ResupplyComponent } from "./warehouse/resupply.component";
import { StorageTemplateComponent } from "./warehouse/storage-template.component";
import { ViewPalletHistoryComponent } from "./warehouse/view-pallet-history.component";
import { VisualOcupationComponent } from "./warehouse/visual-ocupation.component";
import { ProductionQualityComponent } from "./quality/production-quality.component";

export const routes: Routes = [
    { path: 'failed-conference/:docId', component: FailedConferenceComponent, runGuardsAndResolvers: 'always' },
    { path: 'print-inbound/:docId/:devId', component: PrintInboundComponent, runGuardsAndResolvers: 'always' },
    { path: 'print-inventory/:docId/:devId', component: PrintInventoryComponent, runGuardsAndResolvers: 'always' },
    { path: 'reprint', component: ReprintComponent, runGuardsAndResolvers: 'always' },
    { path: 'createtransferOld', component: CreateTransferOldComponent, runGuardsAndResolvers: 'always' },
    { path: 'createtransfer', component: CreateTransferComponent, runGuardsAndResolvers: 'always' },
    { path: 'rtls/:origin/:feature', component: WarehouseMapComponent, runGuardsAndResolvers: 'always' },
    { path: 'saptest', component: SapTest, runGuardsAndResolvers: 'always' },
    { path: 'qc/:docId', component: QualityControlComponent, runGuardsAndResolvers: 'always' },
    { path: 'creaternc/:modPrd', component: CreateRNCComponent, runGuardsAndResolvers: 'always' },
    { path: 'romaneio/:docId', component: RomaneioComponent, runGuardsAndResolvers: 'always' },
    { path: 'productionprocess/:process/:document', component: ProductionProcessComponent, runGuardsAndResolvers: 'always' },
    { path: 'address-block', component: AddressBlockComponent, runGuardsAndResolvers: 'always' },
    { path: 'manage-address', component: ManageAddressComponent, runGuardsAndResolvers: 'always' },
    { path: 'visualstorage/:locationId', component: VisualOcupationComponent, runGuardsAndResolvers: 'always' },
    { path: 'productshortage/:docId', component: ProductShortageComponent, runGuardsAndResolvers: 'always' },
    { path: 'generate-transport', component: GenerateTransportComponent, runGuardsAndResolvers: 'always' },
    { path: 'resupply/:destId/:movPrefix/:movStatus', component: ResupplyComponent, runGuardsAndResolvers: 'always' },
    { path: 'storage-template/:locId', component: StorageTemplateComponent, runGuardsAndResolvers: 'always' },
    { path: 'create-inventory', component: CreateInventoryComponent, runGuardsAndResolvers: 'always' },
    { path: 'check-inventory/:docId', component: CheckInventoryComponent, runGuardsAndResolvers: 'always' },
    { path: 'loadrp', component: RealPickingComponent, runGuardsAndResolvers: 'always' },
    { path: 'sapinventory/:docId', component: SAPInventoryComponent, runGuardsAndResolvers: 'always' },
    { path: 'unlockchecking', component: UnlockCheckingComponent, runGuardsAndResolvers: 'always' },
    { path: 'viewPallet/:thId', component: ViewPalletHistoryComponent, runGuardsAndResolvers: 'always' },
    { path: 'traceability', component: TraceabilityComponent, runGuardsAndResolvers: 'always' },
    { path: 'production-quality', component: ProductionQualityComponent, runGuardsAndResolvers: 'always' }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class SolarRoutingModule { }