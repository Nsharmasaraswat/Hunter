import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { DocumentSummaryComponent } from "./components/document-summary.component";
import { LotPositionComponent } from "./components/lotposition.component";
import { PrintTagsExternalComponent } from "./components/print-tags-external.component";
import { PrintTagsComponent } from "./components/print-tags.component";
import { StateChangeComponent } from "./components/state-change.component";
import { ThingSummaryComponent } from "./components/thing-summary.component";

export const routes: Routes = [
    { path: 'printTags/:doc/:device', component: PrintTagsComponent, runGuardsAndResolvers: 'always' },
    { path: 'printSupplier/:doc/:device', component: PrintTagsExternalComponent, runGuardsAndResolvers: 'always' },
    { path: 'statechange', component: StateChangeComponent, runGuardsAndResolvers: 'always' },
    { path: 'documentSummary', component: DocumentSummaryComponent, runGuardsAndResolvers: 'always' },
    { path: 'thingSummary', component: ThingSummaryComponent, runGuardsAndResolvers: 'always' },
    { path: 'lotPosition', component: LotPositionComponent, runGuardsAndResolvers: 'always' }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})

export class DescarpackRoutingModule { }