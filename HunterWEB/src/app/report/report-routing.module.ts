import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FixedTableComponent } from './fixed/fixed-table';
import { GroupTableComponent } from './group/group-table';
import { LocalStorageReportList } from './localstorage-list.component';
import { ReportBuilderComponent } from './report-builder/report-builder.component';
import { UserWidgetComponent } from "./widgets/user-widget.component";
import { SelectChartComponent } from "./widgets/charts/select-chart.component";
import { EditChartComponent } from "./widgets/charts/edit-chart.component";


export const routes: Routes = [
  { path: 'builder', component: ReportBuilderComponent, runGuardsAndResolvers: 'always' },
  { path: 'localstoragelist', component: LocalStorageReportList, runGuardsAndResolvers: 'always' },
  { path: 'fixed/:fileName', component: FixedTableComponent, runGuardsAndResolvers: 'always' },
  { path: 'group/:fileName', component: GroupTableComponent, runGuardsAndResolvers: 'always' },
  { path: 'userwidget', component: UserWidgetComponent, runGuardsAndResolvers: 'always' },
  { path: 'selectwidget', component: SelectChartComponent, runGuardsAndResolvers: 'always' },
  { path: 'editchart', component: EditChartComponent, runGuardsAndResolvers: 'always', data : {data : 'data'} },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportRoutingModule { }