import { Component, OnInit } from '@angular/core';
import { layerGroup, tileLayer } from 'leaflet';
import 'leaflet.fullscreen';
import { LocationRestService } from '../../../process/services/location-rest.service';


@Component({
  templateUrl: './streets.component.html'
})
export class StreetsComponent implements OnInit {

  //streetMaps = tileLayer('http://maps.wikimedia.org/osm-intl/{z}/{x}/{y}.png', {
  streetMaps = tileLayer('https://api.tiles.mapbox.com/v4/mapbox.streets/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {
    detectRetina: true,
    maxZoom: 28,
    attribution: 'GTP Automation'
  });

  origins: any = {};

  pins = layerGroup();

  layers = layerGroup().addLayer(this.streetMaps).addLayer(this.pins);

  options = {
    layers: this.layers,
    zoom: 18,
    minZoom: 3,
    maxZoom: 20,
    fullscreenControl: true,
    fullscreenControlOptions: {
      position: 'topleft'
    },
    scale: {
      metric: true
    }
  };

  constructor(
    private restService: LocationRestService,
    //private webSocket: OriginWsService
  ) { }

  ngOnInit(): void {
    this.restService.getCenter().subscribe(latlng => this.options['center'] = latlng)
    this.restService.getPins().subscribe(ps => ps.forEach(p => this.pins.addLayer(p)));
    //this.restService.getOrigins().subscribe(origins => this.origins = origins)
    //this.webSocket.conecta()
  }

}