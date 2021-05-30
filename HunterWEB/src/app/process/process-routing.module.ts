import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DocumentContainerCreateComponent } from './components/documentcontainer/documentcontainer-create.component';
import { DocumentContainerListComponent } from './components/documentcontainer/documentcontainer-list.component';
import { DroneControllerComponent } from './components/drone-controller.component';
import { DroneInventoryProcessComponent } from './process/droneinventoryprocess.component';
import { StatusChangeComponent } from './components/status-change.component';
import { StatusOriginsComponent } from './components/status-origins.component';
import { ViewDocumentComponent } from './components/view-document.component';
import { ViewOriginComponent } from './components/view-origin.component';
import { ViewProductsComponent } from './components/view-products.component';
import { ViewTasksComponent } from './components/view-tasks.component';
import { VolumeInventory } from './components/volume-inventory.component';
import { ActionListComponent } from './config/action/action-list.component';
import { ActionComponent } from './config/action/action.component';
import { DocumentComponent } from './config/document/document.component';
import { DocumentModelListComponent } from './config/document/documentmodel-list.component';
import { DocumentModelComponent } from './config/document/documentmodel.component';
import { DocumentThingListComponent } from './config/document/documentthing-list.component';
import { FieldComponent } from './config/document/field.component';
import { ItemComponent } from './config/document/item.component';
import { ModelFieldListComponent } from './config/document/model-field-list.component';
import { ModelFieldComponent } from './config/document/model-field.component';
import { PickingReturnComponent } from './config/document/pickingreturn.component';
import { ThingComponent } from './config/document/thing.component';
import { ViewDocumentsComponent } from './config/document/view-documents.component';
import { FeatureListComponent } from './config/feature/feature-list.component';
import { FeatureComponent } from './config/feature/feature.component';
import { NewManagerAddressComponent } from './config/newmanageraddress/new-manager-address.component';
import { ProductComponent } from './config/product/product.component';
import { OriginListComponent } from './config/origin/origin-list.component';
import { OriginComponent } from './config/origin/origin.component';
import { ProcessListComponent } from './config/process/process-list.component';
import { ProcessComponent } from './config/process/process.component';
import { ProcessFilterListComponent } from './config/processfilter/processfilter-list.component';
import { ProcessFilterComponent } from './config/processfilter/processfilter.component';
import { ProductModelListComponent } from './config/productmodel/productmodel-list.component';
import { ProductModelComponent } from './config/productmodel/productmodel.component';
import { PropertyModelListComponent } from './config/propertymodel/propertymodel-list.component';
import { PropertyModelComponent } from './config/propertymodel/propertymodel.component';
import { PropertyModelFieldListComponent } from './config/propertymodelfield/propertymodelfield-list.component';
import { PropertyModelFieldComponent } from './config/propertymodelfield/propertymodelfield.component';
import { PurposeListComponent } from './config/purpose/purpose-list.component';
import { PurposeComponent } from './config/purpose/purpose.component';
import { TaskDefListComponent } from './config/taskdef/taskdef-list.component';
import { TaskDefComponent } from './config/taskdef/taskdef.component';
import { WorkflowListComponent } from './config/workflow/workflow-list.component';
import { WorkflowComponent } from './config/workflow/workflow.component';
import { CheckingProcessComponent } from './process/checkingprocess.component';
import { ContinuousLocationComponent } from './process/continuous-location.component';
import { ContinuousPickingComponent } from './process/continuous-picking.component';
import { PickingProcessComponent } from './process/pickingprocess.component';
import { PrintDocumentComponent } from './components/print-document.component';
import { LockedTasksComponent } from './components/locked-tasks.component';

export const routes: Routes = [
  { path: 'viewOrigin', component: ViewOriginComponent, runGuardsAndResolvers: 'always' },
  { path: 'action', component: ActionComponent, runGuardsAndResolvers: 'always' },
  { path: 'taskDef', component: TaskDefComponent, runGuardsAndResolvers: 'always' },
  { path: 'origin', component: OriginComponent, runGuardsAndResolvers: 'always' },
  { path: 'workflow', component: WorkflowComponent, runGuardsAndResolvers: 'always' },
  { path: 'process', component: ProcessComponent, runGuardsAndResolvers: 'always' },
  { path: 'propertyModel', component: PropertyModelComponent, runGuardsAndResolvers: 'always' },
  { path: 'processFilter', component: ProcessFilterComponent, runGuardsAndResolvers: 'always' },
  { path: 'feature', component: FeatureComponent, runGuardsAndResolvers: 'always' },
  { path: 'viewTasks/:taskdef', component: ViewTasksComponent, runGuardsAndResolvers: 'always' },
  { path: 'statusChange/:taskdef', component: StatusChangeComponent, runGuardsAndResolvers: 'always' },
  { path: 'checkingprocess/:origin/:document', component: CheckingProcessComponent, runGuardsAndResolvers: 'always' },
  { path: 'pickingprocess/:origin', component: PickingProcessComponent, runGuardsAndResolvers: 'always' },
  { path: 'continuouspicking/:process/:document', component: ContinuousPickingComponent, runGuardsAndResolvers: 'always' },
  { path: 'locationprocess/:process', component: ContinuousLocationComponent, runGuardsAndResolvers: 'always' },
  { path: 'listProducts/:type', component: ViewProductsComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocumentContainer/:containertype/:containerstatus', component: DocumentContainerListComponent, runGuardsAndResolvers: 'always' },
  { path: 'createDocumentContainer', component: DocumentContainerCreateComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments', component: DocumentModelListComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments/:type/edit', component: DocumentModelComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments/:type/edit/modelfields', component: ModelFieldListComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments/:type/edit/modelfields/:id', component: ModelFieldComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments/:type', component: ViewDocumentsComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments/:type/:id', component: DocumentComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments/:type/:id/item', component: ItemComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments/:type/:id/field', component: FieldComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocuments/:type/:id/thing', component: ThingComponent, runGuardsAndResolvers: 'always' },
  { path: 'listDocumentThings', component: DocumentThingListComponent, runGuardsAndResolvers: 'always' },
  { path: 'originList', component: OriginListComponent, runGuardsAndResolvers: 'always' },
  { path: 'origin/:id', component: OriginComponent, runGuardsAndResolvers: 'always' },
  { path: 'featureList', component: FeatureListComponent, runGuardsAndResolvers: 'always' },
  { path: 'feature/:id', component: FeatureComponent, runGuardsAndResolvers: 'always' },
  { path: 'purposeList', component: PurposeListComponent, runGuardsAndResolvers: 'always' },
  { path: 'purpose/:id', component: PurposeComponent, runGuardsAndResolvers: 'always' },
  { path: 'taskdefList', component: TaskDefListComponent, runGuardsAndResolvers: 'always' },
  { path: 'taskdef/:id', component: TaskDefComponent, runGuardsAndResolvers: 'always' },
  { path: 'workflowList', component: WorkflowListComponent, runGuardsAndResolvers: 'always' },
  { path: 'workflow/:id', component: WorkflowComponent, runGuardsAndResolvers: 'always' },
  { path: 'processList', component: ProcessListComponent, runGuardsAndResolvers: 'always' },
  { path: 'process/:id', component: ProcessComponent, runGuardsAndResolvers: 'always' },
  { path: 'actionList', component: ActionListComponent, runGuardsAndResolvers: 'always' },
  { path: 'action/:id', component: ActionComponent, runGuardsAndResolvers: 'always' },
  { path: 'propertymodelList', component: PropertyModelListComponent, runGuardsAndResolvers: 'always' },
  { path: 'propertymodel/:id', component: PropertyModelComponent, runGuardsAndResolvers: 'always' },
  { path: 'productmodelList', component: ProductModelListComponent, runGuardsAndResolvers: 'always' },
  { path: 'productmodel/:id', component: ProductModelComponent, runGuardsAndResolvers: 'always' },
  { path: 'processfilterList', component: ProcessFilterListComponent, runGuardsAndResolvers: 'always' },
  { path: 'processfilter/:id', component: ProcessFilterComponent, runGuardsAndResolvers: 'always' },
  { path: 'propertymodelfieldList/:metaname', component: PropertyModelFieldListComponent, runGuardsAndResolvers: 'always' },
  { path: 'propertymodelfield/:metaname/:id', component: PropertyModelFieldComponent, runGuardsAndResolvers: 'always' },
  { path: 'statusorigin', component: StatusOriginsComponent, runGuardsAndResolvers: 'always' },
  { path: 'viewDocument/:id', component: ViewDocumentComponent, runGuardsAndResolvers: 'always' },
  { path: 'pickingReturn', component: PickingReturnComponent, runGuardsAndResolvers: 'always' },
  { path: 'volumeInventory', component: VolumeInventory, runGuardsAndResolvers: 'always' },
  { path: 'droneController', component: DroneControllerComponent, runGuardsAndResolvers: 'always' },
  { path: 'product', component: ProductComponent, runGuardsAndResolvers: 'always' },
  { path: 'newManagerAddress', component: NewManagerAddressComponent, runGuardsAndResolvers: 'always' },
  { path: 'printdocument/:docId/:devId', component: PrintDocumentComponent, runGuardsAndResolvers: 'always' },
  { path: 'droneinventoryprocess/:process/:document/:thing/:tag', component: DroneInventoryProcessComponent, runGuardsAndResolvers: 'always'},
  { path: 'lockedtasks', component: LockedTasksComponent, runGuardsAndResolvers: 'always'}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProcessRoutingModule { }
