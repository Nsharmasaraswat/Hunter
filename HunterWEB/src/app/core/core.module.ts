import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { LeafletMarkerClusterModule } from '@asymmetrik/ngx-leaflet-markercluster';
import { OriginWsService } from '../process/services/origin-ws.service';
import { InternalSharedModule } from '../shared/shared.module';
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
import { CoreRoutingModule } from './core-routing.module';
import { DemoRFIDComponent } from './rfid/demo-rfid.component';
import { TmpCambuhyComponent } from './tmp/tmp-cambuhy.component';


@NgModule({
  imports: [CommonModule, CoreRoutingModule, InternalSharedModule, LeafletMarkerClusterModule],
  declarations: [RtlsComponent, SourceComponent, SourceListComponent, ChangePasswordComponent, DeviceComponent, DeviceListComponent,
    PortComponent, PortListComponent, LocationListComponent, GroupPermissionComponent, TmpCambuhyComponent,
    LocationComponent, GroupListComponent, GroupComponent, DemoRFIDComponent,
    PersonListComponent, PersonComponent, PermissionListComponent, PermissionComponent,
    UserListComponent, UserComponent, RawdataListComponent, AlertListComponent, RawdataSimulatorComponent],
  providers: [CoreRoutingModule, OriginWsService, LeafletMarkerClusterModule]
})

export class CoreModule { }
