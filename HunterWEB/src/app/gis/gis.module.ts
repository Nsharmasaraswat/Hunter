
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { InternalSharedModule } from '../shared/shared.module';
import { SecurityModule } from '../security/security.module';
import { GisRoutingModule } from './gis-routing.module';
import { LeafletGisComponent } from './components/leaflet/leaflet-gis.component';
import { LocationMapComponent } from './components/leaflet/location-map.component';
import { LocationChoserComponent } from './components/leaflet/location-choser.component';
import { LeafletMarkerClusterModule } from '@asymmetrik/ngx-leaflet-markercluster';
import { UploadImagesComponent } from './components/upload-images.component';
import { StreetsComponent } from './components/leaflet/streets.component';
import { LeafletPositionComponent } from './components/leaflet/leaflet-position.component';
import { OriginWsService } from '../process/services/origin-ws.service';
import { LocationRestService } from '../process/services/location-rest.service';

@NgModule({
  imports: [CommonModule, FormsModule, ReactiveFormsModule, GisRoutingModule, InternalSharedModule, SecurityModule, LeafletMarkerClusterModule],
  declarations: [LeafletGisComponent, LocationMapComponent, LocationChoserComponent, UploadImagesComponent, LeafletPositionComponent, StreetsComponent],
  exports: [LocationMapComponent, LocationChoserComponent, UploadImagesComponent],
  providers: [GisRoutingModule, LeafletMarkerClusterModule, LocationRestService, OriginWsService]
})

export class GisModule { }
