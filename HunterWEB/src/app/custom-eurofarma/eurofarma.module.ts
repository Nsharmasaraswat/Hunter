// Angular Imports
// This Module's Components
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { InternalSharedModule } from '../shared/shared.module';
import { CustomEurofarmaRoutingModule } from './eurofarma-routing.module';
import { ViewPortalComponent } from './portal/view-portal.component';

@NgModule({
    declarations: [ViewPortalComponent],
    imports: [CommonModule, FormsModule, ReactiveFormsModule, InternalSharedModule, CustomEurofarmaRoutingModule],
    providers: [CustomEurofarmaRoutingModule]
})

export class CustomEurofarmaModule { }