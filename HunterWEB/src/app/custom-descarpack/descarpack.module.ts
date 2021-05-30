import { NgModule } from "@angular/core";
import { InternalSharedModule } from "../shared/shared.module";
import { DocumentSummaryComponent } from './components/document-summary.component';
import { LotPositionComponent } from "./components/lotposition.component";
import { ManageDocByTypeComponent } from "./components/managedocbytype.component";
import { PrintTagsExternalComponent } from "./components/print-tags-external.component";
import { PrintTagsComponent } from "./components/print-tags.component";
import { StateChangeComponent } from "./components/state-change.component";
import { ThingSummaryComponent } from './components/thing-summary.component';
import { DescarpackRoutingModule } from './descarpack-routing.module';



@NgModule({
    imports: [DescarpackRoutingModule, InternalSharedModule],
    declarations: [PrintTagsComponent, ManageDocByTypeComponent, PrintTagsExternalComponent, StateChangeComponent, DocumentSummaryComponent, 
        ThingSummaryComponent, LotPositionComponent],
    providers: [DescarpackRoutingModule]
})

export class DescarpackModule { }