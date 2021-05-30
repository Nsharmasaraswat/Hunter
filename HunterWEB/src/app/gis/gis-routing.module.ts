import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LeafletGisComponent } from './components/leaflet/leaflet-gis.component';
import { LocationMapComponent } from './components/leaflet/location-map.component';
import { LocationChoserComponent } from './components/leaflet/location-choser.component';
import { LeafletPositionComponent } from './components/leaflet/leaflet-position.component';
import { StreetsComponent } from './components/leaflet/streets.component';


export const routes: Routes = [
  { path: 'leafletGis', component: LeafletGisComponent },
  { path: 'locationMap/:origin/:feature', component: LocationMapComponent },
  { path: 'locationChoser/:origin/:feature/:location', component: LocationChoserComponent, runGuardsAndResolvers: 'always' },
  { path: 'leaflet', component: LeafletPositionComponent, runGuardsAndResolvers: 'always' },
  { path: 'streets', component: StreetsComponent, runGuardsAndResolvers: 'always' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GisRoutingModule { }
