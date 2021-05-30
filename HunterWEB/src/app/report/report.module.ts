import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { InternalSharedModule } from "../shared/shared.module";
import { DynamicReportTableComponent } from "./components/dynamic-table.component";
import { LocalStorageReportList } from "./localstorage-list.component";
import { ReportBuilderComponent } from "./report-builder/report-builder.component";
import { ReportRoutingModule } from "./report-routing.module";
import { FixedTableComponent } from "./fixed/fixed-table";
import { GroupTableComponent } from "./group/group-table";
import { UserWidgetComponent } from "./widgets/user-widget.component";
import { TabMenuModule } from "primeng/tabmenu";
import { InputSwitchModule } from 'primeng/inputswitch';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { ChartTypeComponent } from "./widgets/charts/chart-type.component";
import { EditChartComponent } from "./widgets/charts/edit-chart.component";
import { SelectChartComponent } from "./widgets/charts/select-chart.component";
import { ChartComponent } from "./widgets/charts/chart.component";
import { CookieService } from "angular2-cookie/services/cookies.service"
import { WidgetService } from "./services/widget.service";



@NgModule({
  imports: [
    CommonModule,
    ReportRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    InternalSharedModule,
    TabMenuModule,
    InputSwitchModule,
    NgxChartsModule
  ],
  declarations: [
    ReportBuilderComponent,
    DynamicReportTableComponent,
    LocalStorageReportList,
    FixedTableComponent,
    GroupTableComponent,
    UserWidgetComponent,
    ChartTypeComponent,
    EditChartComponent,
    SelectChartComponent,
    ChartComponent
  ],
  providers: [ReportRoutingModule, CookieService, WidgetService],
  exports: [
    FixedTableComponent,
    DynamicReportTableComponent,
    GroupTableComponent,
    UserWidgetComponent,
    ChartTypeComponent
  ],
})
export class ReportModule { }
