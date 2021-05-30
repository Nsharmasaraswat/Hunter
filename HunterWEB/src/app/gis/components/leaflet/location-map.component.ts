import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, ElementRef, Input, OnChanges, OnDestroy, OnInit, SimpleChange, SimpleChanges } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import "@babel/polyfill";
import * as L from 'leaflet';
import { tileLayer } from 'leaflet';
import 'leaflet-draw-drag';
import 'leaflet-easybutton';
import 'leaflet-path-drag';
import 'leaflet-path-transform';
import 'leaflet.fullscreen';
import 'leaflet.markercluster';
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../../environments/environment";
import { LocationRestService } from '../../../process/services/location-rest.service';
import { RawData } from '../../../shared/classes/RawData';
import { RawDataType } from '../../../shared/model/enum/RawDataType';
import { HunterAddress } from '../../../shared/model/HunterAddress';
import { SocketService } from "../../../shared/services/socket.service";
import { GisTools } from '../../tools/GisTools';

declare var require: any;
const MAX_TRAIL_POINTS: number = 15;

@Component({
  selector: 'location-map',
  templateUrl: './location-map.component.html',
  styleUrls: ['location-map.component.scss']
})

export class LocationMapComponent implements OnInit, OnDestroy, OnChanges {
  private map: L.Map;
  private tags: Map<String, L.Marker> = new Map<String, L.Marker>();
  private trail: Map<String, L.CircleMarker[]> = new Map<String, L.CircleMarker[]>();
  private gisTools;
  private socketSubscription: Subscription;
  private locationSubscription: Subscription;
  private addressSubscription: Subscription;
  private originSubscription: Subscription;
  private routeSubscription: Subscription;
  private stream: Observable<any>;
  private locationChildren: any[];

  leaveTrail: boolean = true;
  centerOnEvent: boolean = false;
  showOptions: boolean = true;
  iconSize: number = 15;
  msgs: any[] = [];
  itens: any = {};
  origin: string;
  feature: string;
  mapType: string = "Satellite";
  mapStyle: string = "";
  @Input("center") center: [number, number];
  @Input("locationId") locationId: string = '3d69771f-4c59-11e9-a948-0266c0e70a8c'; //TODO: component receive locationId Solar
  @Input("autoConnectOrigin") autoConnectOrigin: boolean = true;
  @Input("autoLoadZones") autoLoadZones: boolean = true;


  // Grupo de layers das zonas.
  zoneGroup = new L.FeatureGroup();

  // Grupo de layers dos markers.
  // markerGroup = new L.FeatureGroup();
  markerClusterGroup: L.MarkerClusterGroup;
  markers: L.Marker[] = [];

  // Grupo de layers dos rastros.
  trailGroup = new L.FeatureGroup();

  // Todos os layers que vão ser carregados no mapa
  layers = new L.LayerGroup()
    .addLayer(this.zoneGroup)
    .addLayer(this.trailGroup);


  public options: any = {
    preferCanvas: true,
    zoom: 18,
    minZoom: 0,
    maxZoom: 20,
    fullscreenControl: true,
    fullscreenControlOptions: {
      position: 'topleft'
    },
    scale: {
      metric: true
    },
    worldCopyJump: true,
    keyboard: true,
    inertia: true,
    maxBounds: L.latLngBounds(L.latLng(-90, -180), L.latLng(90, 180))
  };

  public markerClusterOptions: L.MarkerClusterGroupOptions = {
    disableClusteringAtZoom: 18
  };

  constructor(private msgSvc: MessageService, private http: HttpClient, private socket: SocketService,
    private changeDetector: ChangeDetectorRef, private locSvc: LocationRestService, private el: ElementRef, private route: ActivatedRoute) {
    console.log('LocationMap Constructor');
  }

  onMapReady(map: L.Map) {
    console.log('LocationMap MapReady');
    this.gisTools = new GisTools(map);
    this.map = map;
    this.map.addLayer(this.layers);
  }

  markerClusterReady(markerCluster: L.MarkerClusterGroup) {
    console.log('LocationMap MarkerCluster Ready');
    this.markerClusterGroup = markerCluster;
    this.layers.addLayer(this.markerClusterGroup);
    this.reloadMap();
  }

  testBearingDistance() {
    let first = L.latLng(-5.876171, -35.315834);
    this.markers.push(this.createMarker(first, 'target.svg', 'TESTE1'));
    this.markers.push(this.createMarker(this.gisTools.getPointAtBearingAndDistance(first.lat, first.lng, 0, 50), 'target.svg', 'TESTE2'));
    this.markers.push(this.createMarker(this.gisTools.getPointAtBearingAndDistance(first.lat, first.lng, 90, 50), 'target.svg', 'TESTE3'));
    this.markers.push(this.createMarker(this.gisTools.getPointAtBearingAndDistance(first.lat, first.lng, 180, 50), 'target.svg', 'TESTE4'));
    this.markers.push(this.createMarker(this.gisTools.getPointAtBearingAndDistance(first.lat, first.lng, 270, 50), 'target.svg', 'TESTE5'));
  }

  ngOnChanges(changes: SimpleChanges) {
    const ctr: SimpleChange = changes.center;
    if (!ctr.firstChange) {
      console.log('LocationMap Changes');
      console.log(ctr);
      this.options.center = L.latLng(ctr.currentValue);
      console.log("Reloading Map");
      this.reloadMap();
    } else if (this.center != null)
      this.options.center = L.latLng(this.center);
  }

  reloadMap() {
    this.zoneGroup.clearLayers();
    this.markerClusterGroup.clearLayers();
    this.trailGroup.clearLayers();
    this.markers = Array.from([]);
    this.markers.length = 0;
    this.map.panTo(this.options.center);
    if (this.socketSubscription != null) {
      this.socketSubscription.unsubscribe();
    }
    if (this.autoConnectOrigin) {
      this.connectOrigin();
    }
    if (this.autoLoadZones) {
      this.tempLoadZonesFromRest(this.locationId);
    }
  }

  ngOnInit() {
    console.log('LocationMap Init');
    this.routeSubscription = this.route.params.subscribe(data => {
      console.log('LocationMap Route Subscription');
      if (this.options.center == null && this.center == null){
        
        this.options.center = L.latLng([-5.876181, -35.315814])
      }
      this.origin = data.origin;
      this.feature = data.feature;
    });
    // this.layers.addLayer(tileLayer('https://api.tiles.mapbox.com/v4/mapbox.mapbox-streets-v8/{z}/{x}/{y}@2x.png256?style=mapbox://styles/mapbox/outdoors-v11@00&access_token=pk.eyJ1IjoibWF0ZXVzdG9ybWluIiwiYSI6ImNqdGE3cTltbjA3ems0NG51dW4zY2F3am0ifQ.-_T_cUTzFaPhX6oDrD3kBw', {
    //   attribution: '&copy; GTP AUTOMATION',
    //   maxZoom: 20
    // }));
    this.layers.addLayer(tileLayer('https://api.mapbox.com/styles/v1/mateustormin/cjzbav04t12uc1cufkuulbfgm/tiles/256/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoibWF0ZXVzdG9ybWluIiwiYSI6ImNqdGE3cDdjOTAya280NG81cGF2dWk0eTIifQ.-pRx4DU2G5eZRycT6SPHKQ', {
      attribution: '&copy; GTP AUTOMATION',
      maxZoom: 20
    }));
    this.originSubscription = this.http.get(environment.processserver + "origin").subscribe(data => {
      console.log(data);
      this.itens = data;
    });
  }

  connectOrigin() {
    this.conectWebsocket();
    this.startTrailEraser();
  }

  conectWebsocket() {
    if (this.origin != null) {
      console.log('Conenctando no  origin: ' + this.origin);
      this.msgs.length = 0;
      this.stream = this.socket.connect(environment.wsprocess + 'origin/' + this.origin);
      this.socket.hideLoadIndicator();
      this.socketSubscription = this.stream.subscribe(
        (message: RawData) => {
          console.log('SocketSubscription Next: ', message);
          this.msgs.push(message);
          if (this.msgs.length > 1000)
            this.msgs.shift();
          let payload = JSON.parse(message.payload);
          if (message.type === RawDataType.LOCATION) {
            let tagId = message.tagId;
            let lat = payload['latitude'];
            let lng = payload['longitude'];
            let pos = L.latLng(lat, lng);

            if (this.tags.has(tagId)) {
              if (this.leaveTrail) {
                let lastPos = this.tags.get(tagId).getLatLng();
                let trailPoint = this.createTrailMarker(lastPos, tagId).addTo(this.trailGroup);

                if (this.trail.has(tagId)) {
                  let trailHistory = this.trail.get(tagId);

                  if (trailHistory.length > MAX_TRAIL_POINTS) {
                    this.trailGroup.removeLayer(trailHistory.pop());
                  }
                  trailHistory.push(trailPoint);
                  this.trail.set(tagId, trailHistory);
                } else {
                  let trailHistory = new Array<L.CircleMarker>();

                  trailHistory.push(trailPoint);
                  this.trail.set(tagId, trailHistory);
                }
              }
              this.tags.get(tagId).setLatLng(pos);
            } else {
              let marker = this.createMarker(pos, "truck-red.svg", tagId).bindPopup(tagId);

              this.tags.set(tagId, marker);
              this.markers.push(marker);
              this.markerClusterGroup.addLayer(marker);
            }
            if (this.centerOnEvent)
              this.map.panTo(pos);
          } else if (message.type === RawDataType.SENSOR) {

          } else if (message.type === RawDataType.STATUS) {
            let tagId = message.tagId;
            if (this.tags.has(tagId)) {
              // this.tags.get(tagId).bindPopup('TESTE')
            }
          }
          this.markerClusterGroup.refreshClusters();
        }, (error: any) => {
          console.log("SocketSubscription Error: ") + error;
          this.socket.hideLoadIndicator();
        }, () => {
          console.log("SocketSubscription Complete");
        }
      );
    }
  }

  createMarker(position: L.LatLng, icon: string, tooltip: string): L.Marker {
    return L.marker(position, {
      icon: L.icon({
        iconUrl: 'assets/icons/' + icon,
        iconSize: [this.iconSize, this.iconSize],
        iconAnchor: [this.iconSize / 2, this.iconSize / 2]
      }),
      clickable: true,
      draggable: false,
      title: tooltip,
      riseOnHover: true
    });
  }

  createTrailMarker(position: L.LatLng, tooltip: string): L.CircleMarker {
    return L.circleMarker(position, {
      radius: this.iconSize / 7
    });
  }

  ngOnDestroy() {
    if (this.socketSubscription != null)
      this.socketSubscription.unsubscribe();
    if (this.addressSubscription != null)
      this.addressSubscription.unsubscribe();
    if (this.originSubscription != null)
      this.originSubscription.unsubscribe();
    if (this.locationSubscription != null)
      this.locationSubscription.unsubscribe();
    if (this.routeSubscription != null)
      this.routeSubscription.unsubscribe();
  }

  resize(e) {
    this.tags.forEach(m => {
      let curIcon = m.options.icon;
      curIcon.options.iconSize = e.value;
      curIcon.options.iconAnchor = [e.value / 2, e.value / 2];
      m.setIcon(curIcon);
    });
    this.trail.forEach(t => {
      t.forEach(m => {
        m.setRadius(e.value / 7);
      });
    });
  }

  //TODO: Make trailer object with this
  startTrailEraser() {
    setInterval(() => {
      this.trail.forEach(t => {
        this.trailGroup.removeLayer(t.shift());
      });
    }, 1000 * 25);
  }

  loadZones(convertoToWSG: boolean, zoneObjects: HunterAddress[]) {
    let zone: L.Polygon;
    var parse = require('wellknown');
    let options = {
      stroke: true,
      color: 'green',
      weight: 1,
      opacity: 1,
      fill: true,
      fillColor: 'blue',
      fillOpacity: 0.3
    };

    zoneObjects.forEach((z: HunterAddress) => {
      let coordsSimple = parse(z.wkt).coordinates[0];
      let coordsPolygon: L.LatLngExpression[] = new Array<L.LatLngTuple>();

      for (let i = 0; i < coordsSimple.length; i++) {
        let coord = coordsSimple[i];
        if (convertoToWSG) {
          let coordWSG84: L.LatLng = this.convertToWGS84(coord[1] / 100, coord[0] / 100, 181.27);
          coordsPolygon.push(coordWSG84);
        } else {
          let coordSimple: L.LatLng = L.latLng(coord[1], coord[0]);
          coordsPolygon.push(coordSimple);
        }
      }
      zone = L.polygon(coordsPolygon, options).bindTooltip(z.name);
      if (zone.dragging)
        zone.dragging.enable();
      this.zoneGroup.addLayer(zone);
      this.zoneGroup.bringToFront();
    });
  }

  tempLoadZonesFromRest(locationId: string) {
    //this.locSvc.getLocation('95f31d9f-4cd9-11e9-a948-0266c0e70a8c').subscribe(location => this.layers.addLayer(L.imageOverlay(location.mapfile,[[0,0],[0,0]])))
    this.locSvc.getLocationZones(locationId).subscribe((addArr: HunterAddress[]) => {
      console.log('Address: ' + addArr.length);
      this.loadZones(false, addArr);
    });
  }

  convertToWGS84(latM: number, lngM: number, rot: number = 0): L.LatLng {
    let mapCenter = [-5.876181, -35.315814];
    let bH = ((lngM > 0 ? 90 : 270) + rot) % 360;
    let bV = ((latM > 0 ? 0 : 180) + rot) % 360;
    let transLng = this.gisTools.getPointAtBearingAndDistance(mapCenter[0], mapCenter[1], bH, Math.abs(lngM));

    return this.gisTools.getPointAtBearingAndDistance(transLng.lat, transLng.lng, bV, Math.abs(latM));
  }

  loadChildren(locationId: String) {
    this.locationChildren = [];
    this.locationChildren.length = 0;
    this.locationSubscription = this.http.get(environment.processserver + 'location/children/' + locationId).subscribe((msg: any[]) => {
      if (msg != undefined) {
        console.log(msg);
        this.locationChildren.push(msg);
      }
    });
  }
}