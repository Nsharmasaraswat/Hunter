// Angular Imports
import { NgModule } from '@angular/core';

// This Module's Components
import { CommonModule } from '@angular/common';
import { InternalSharedModule } from '../shared/shared.module';
import { CustomIpRoutingModule } from './ip-routing.module';
import { GisModule } from '../gis/gis.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RegisterTruckComponent } from './temp/register-truck.component';

@NgModule({
    declarations: [RegisterTruckComponent],
    imports: [CommonModule, FormsModule, ReactiveFormsModule, InternalSharedModule, GisModule, CustomIpRoutingModule],
    providers: [CustomIpRoutingModule]
})

export class CustomIpModule { }