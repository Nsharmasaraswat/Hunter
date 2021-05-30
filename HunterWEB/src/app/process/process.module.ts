import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { GisModule } from '../gis/gis.module';
import { InternalSharedModule } from '../shared/shared.module';
import { DocumentContainerCreateComponent } from './components/documentcontainer/documentcontainer-create.component';
import { DocumentContainerListComponent } from './components/documentcontainer/documentcontainer-list.component';
import { DroneControllerComponent } from './components/drone-controller.component';
import { LocationDataComponent } from './components/location-data.component';
import { LockedTasksComponent } from './components/locked-tasks.component';
import { ManageFieldsComponent } from './components/manage-fields.component';
import { PrintDocumentComponent } from './components/print-document.component';
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
import { ProcessRoutingModule } from './process-routing.module';
import { CheckingProcessComponent } from './process/checkingprocess.component';
import { ContinuousLocationComponent } from './process/continuous-location.component';
import { ContinuousPickingComponent } from './process/continuous-picking.component';
import { DroneInventoryProcessComponent } from './process/droneinventoryprocess.component';
import { PickingProcessComponent } from './process/pickingprocess.component';
import { AddressService } from './services/address.service';
import { NewManagerProductService } from './services/new-manager-product.service';

@NgModule({
  imports: [CommonModule, ProcessRoutingModule, ReactiveFormsModule, InternalSharedModule, GisModule],
  declarations: [ViewOriginComponent, ViewTasksComponent, StatusChangeComponent, ActionComponent,
    DocumentModelComponent, TaskDefComponent, OriginComponent, WorkflowComponent,
    ProcessComponent, PropertyModelComponent, DocumentThingListComponent, DocumentContainerListComponent, DocumentContainerCreateComponent, DocumentComponent, CheckingProcessComponent,
    ManageFieldsComponent, FieldComponent, ItemComponent, ThingComponent, DroneInventoryProcessComponent,
    ViewProductsComponent, ViewDocumentsComponent, DocumentModelListComponent, ModelFieldComponent,
    FeatureComponent, ModelFieldListComponent, OriginListComponent, LockedTasksComponent,
    OriginComponent, FeatureListComponent, PurposeListComponent, PurposeComponent,
    TaskDefListComponent, TaskDefComponent, WorkflowListComponent, WorkflowComponent,
    ProcessListComponent, ProcessComponent, ActionListComponent, ActionComponent, PickingReturnComponent,
    PropertyModelListComponent, PropertyModelComponent, ProductModelListComponent, ProductModelComponent,
    ProcessFilterListComponent, ProcessFilterComponent, PropertyModelFieldListComponent, PropertyModelFieldComponent,
    PickingProcessComponent, ContinuousPickingComponent, ContinuousLocationComponent, StatusOriginsComponent, VolumeInventory, LocationDataComponent, DroneControllerComponent,
    ProductComponent, ViewDocumentComponent, NewManagerAddressComponent, PrintDocumentComponent
  ],
  providers: [
    ProcessRoutingModule,
    PickingProcessComponent,
    ContinuousPickingComponent,
    StatusOriginsComponent,
    ViewDocumentComponent,
    ProductComponent,
    NewManagerProductService,
    AddressService
  ]
})

export class ProcessModule { }
