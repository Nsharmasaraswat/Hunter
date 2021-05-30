import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AlertListComponent } from './components/alert-list.component';
import { ChangePasswordComponent } from './components/change-password.component';
import { DeviceListComponent } from './components/device-list.component';
import { DeviceComponent } from './components/device.component';
import { GroupListComponent } from './components/group-list.component';
import { GroupPermissionComponent } from './components/group-permission.component';
import { GroupComponent } from './components/group.component';
import { LocationListComponent } from './components/location-list.component';
import { LocationComponent } from './components/location.component';
import { PermissionListComponent } from './components/permission-list.component';
import { PermissionComponent } from './components/permission.component';
import { PersonListComponent } from './components/person-list.component';
import { PersonComponent } from './components/person.component';
import { PortListComponent } from './components/port-list.component';
import { PortComponent } from './components/port.component';
import { RawdataListComponent } from './components/rawdata-list.component';
import { RawdataSimulatorComponent } from './components/rawdata-simulator.component';
import { RtlsComponent } from './components/rtlsws.component';
import { SourceListComponent } from './components/source-list.component';
import { SourceComponent } from './components/source.component';
import { UserListComponent } from './components/user-list.component';
import { UserComponent } from './components/user.component';
import { DemoRFIDComponent } from './rfid/demo-rfid.component';
import { TmpCambuhyComponent } from './tmp/tmp-cambuhy.component';


export const routes: Routes = [
  { path: 'viewRtls', component: RtlsComponent, runGuardsAndResolvers: 'always' },
  { path: 'demoRFID', component: DemoRFIDComponent, runGuardsAndResolvers: 'always' },
  { path: 'deviceList', component: DeviceListComponent, runGuardsAndResolvers: 'always' },
  { path: 'device/:id', component: DeviceComponent, runGuardsAndResolvers: 'always' },
  { path: 'sourceList', component: SourceListComponent, runGuardsAndResolvers: 'always' },
  { path: 'source/:id', component: SourceComponent, runGuardsAndResolvers: 'always' },
  { path: 'portList', component: PortListComponent, runGuardsAndResolvers: 'always' },
  { path: 'port/:id', component: PortComponent, runGuardsAndResolvers: 'always' },
  { path: 'locationList', component: LocationListComponent, runGuardsAndResolvers: 'always' },
  { path: 'location/:id', component: LocationComponent, runGuardsAndResolvers: 'always' },
  { path: 'groupList', component: GroupListComponent, runGuardsAndResolvers: 'always' },
  { path: 'group/:id', component: GroupComponent, runGuardsAndResolvers: 'always' },
  { path: 'grouppermission', component: GroupPermissionComponent, runGuardsAndResolvers: 'always' },
  { path: 'personList', component: PersonListComponent, runGuardsAndResolvers: 'always' },
  { path: 'person/:id', component: PersonComponent, runGuardsAndResolvers: 'always' },
  { path: 'permissionList', component: PermissionListComponent, runGuardsAndResolvers: 'always' },
  { path: 'permission/:id', component: PermissionComponent, runGuardsAndResolvers: 'always' },
  { path: 'userList', component: UserListComponent, runGuardsAndResolvers: 'always' },
  { path: 'user/:id', component: UserComponent, runGuardsAndResolvers: 'always' },
  { path: 'changePassword/:id', component: ChangePasswordComponent, runGuardsAndResolvers: 'always' },
  { path: 'alertList', component: AlertListComponent, runGuardsAndResolvers: 'always' },
  { path: 'rawdata', component: RawdataListComponent, runGuardsAndResolvers: 'always' },
  { path: 'rawdatasimulator', component: RawdataSimulatorComponent, runGuardsAndResolvers: 'always' },
  { path: 'gps-file-processor', component: TmpCambuhyComponent, runGuardsAndResolvers: 'always' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CoreRoutingModule { }
