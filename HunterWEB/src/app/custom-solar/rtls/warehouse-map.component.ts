import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, ElementRef, Input, OnChanges, OnDestroy, OnInit, SimpleChange, SimpleChanges } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import "@babel/polyfill";
import * as L from 'leaflet';
import 'leaflet-draw-drag';
import 'leaflet-easybutton';
import FreeDraw, { NONE } from 'leaflet-freedraw';
import 'leaflet-path-drag';
import 'leaflet-path-transform';
import 'leaflet.fullscreen';
import 'leaflet.markercluster';
import { MessageService } from "primeng/components/common/messageservice";
import { Table } from 'primeng/table';
import { Subscription } from "rxjs";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { GisTools } from '../../gis/tools/GisTools';
import { LocationRestService } from '../../process/services/location-rest.service';
import { RawData } from '../../shared/classes/RawData';
import { RawDataType } from '../../shared/model/enum/RawDataType';
import { HunterAddress } from '../../shared/model/HunterAddress';
import { SocketService } from "../../shared/services/socket.service";

const MAX_TRAIL_POINTS: number = 10;
const MAP_WIDTH: number = 98.6 * 200;//197.6m - 197.15
const MAP_HEIGHT: number = 84.5 * 200;//171.8m - 168.95

@Component({
  selector: 'warehouse-map',
  templateUrl: './warehouse-map.component.html',
  styleUrls: ['warehouse-map.component.scss']
})

export class WarehouseMapComponent implements OnInit, OnDestroy, OnChanges {
  private map: L.Map;
  private tags: Map<String, L.Marker> = new Map<String, L.Marker>();
  private trail: Map<String, L.CircleMarker[]> = new Map<String, L.CircleMarker[]>();
  private gisTools: GisTools;
  private socketSubscription: Subscription;
  private locationSubscription: Subscription;
  private addressSubscription: Subscription;
  private originSubscription: Subscription;
  private routeSubscription: Subscription;
  private stream: Observable<any>;
  private locationChildren: any[];

  clickList: L.Point[] = [];
  pathList: L.LatLng[] = [];
  displayPathDialog: boolean = false;
  leaveTrail: boolean = false;
  centerOnEvent: boolean = false;
  showOptions: boolean = true;
  moveEnabled: boolean = false;
  iconSize: number = 30;
  msgs: any[] = [];
  itens: any = {};
  origin: string;
  feature: string;
  @Input("center") center: [number, number];
  @Input("locationId") locationId: string = '95f31d9f-4cd9-11e9-a948-0266c0e70a8c'; //TODO: component receive locationId Solar
  @Input("autoConnectOrigin") autoConnectOrigin: boolean = true;
  @Input("autoLoadZones") autoLoadZones: boolean = true;

  freeDraw = new FreeDraw({
    mode: NONE,
    leaveModeAfterCreate: true,
    strokeWidth: 1
  });

  // Grupo de layers das zonas.
  zoneGroup = new L.FeatureGroup();

  // Grupo de layers dos caminhos.
  pathGroup = new L.FeatureGroup();

  // Grupo de layers das zonas.
  markerClusterGroup: L.MarkerClusterGroup;
  markers: L.Marker[] = [];
  // Grupo de layers dos rastros.
  trailGroup = new L.FeatureGroup();

  // Todos os layers que vão ser carregados no mapa
  layers = new L.LayerGroup()
    // .addLayer(this.things)
    .addLayer(this.zoneGroup)
    .addLayer(this.pathGroup)
    .addLayer(this.trailGroup);


  public options: any = {
    preferCanvas: true,
    zoom: 4,
    minZoom: 0,
    maxZoom: 5,
    crs: L.CRS.Simple,
    center: L.latLng(0, 0),
    fullscreenControl: true,
    fullscreenControlOptions: {
      position: 'topleft'
    },
    scale: {
      metric: true
    },
    keyboard: true,
    inertia: true
  };

  public markerClusterOptions: L.MarkerClusterGroupOptions = {
    disableClusteringAtZoom: 4
  };

  public drawOptions = {
    position: 'topleft',
    // draw: {
    //   polyline: false,
    //   marker: false,
    //   circlemarker: false,
    //   polygon: {
    //     //allowIntersection: false, // Restricts shapes to simple polygons
    //     allowIntersection: true,
    //     showArea: true,
    //     showLength: true,
    //     /* drawError: {
    //       color: '#e1e100', // Color the shape will turn when intersects
    //       message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
    //     },*/
    //     icon: new L.DivIcon({
    //       iconSize: new L.Point(8, 8, false),
    //       className: 'leaflet-div-icon leaflet-editing-icon'
    //     }),
    //     touchIcon: new L.DivIcon({
    //       iconSize: new L.Point(10, 10, false),
    //       className: 'leaflet-div-icon leaflet-editing-icon leaflet-touch-icon'
    //     }),
    //     shapeOptions: {
    //       color: 'green',
    //       stroke: true,
    //       clickable: true,
    //       transform: true,
    //       weight: 1,
    //       draggable: true
    //     },
    //     metric: true
    //   },
    //   circle: {
    //     shapeOptions: {
    //       color: 'green',
    //       stroke: true,
    //       clickable: true,
    //       transform: true,
    //       weight: 1,
    //     },
    //     showRadius: true,
    //     metric: true
    //   },
    //   rectangle: {
    //     shapeOptions: {
    //       color: 'green',
    //       stroke: true,
    //       clickable: true,
    //       transform: true,
    //       weight: 1,
    //       draggable: true
    //     },
    //     metric: true
    //   }
    // },
    edit: {
      featureGroup: this.zoneGroup,
      remove: true,
      transform: true
    }
  };

  constructor(private msgSvc: MessageService, private http: HttpClient, private socket: SocketService,
    private changeDetector: ChangeDetectorRef, private locSvc: LocationRestService,
    private el: ElementRef, private route: ActivatedRoute) {
    console.log('LocationMap Constructor');
  }

  onMapReady(map: L.Map) {
    console.log('LocationMap MapReady');
    this.gisTools = new GisTools(map);
    this.map = map;
    this.map.addLayer(this.layers);
    this.map.on('click', (event: L.LeafletMouseEvent) => {
      if (this.pathList.length > 0) {
        let last = this.pathList[this.pathList.length - 1];

        this.pathGroup.addLayer(L.polyline([last, event.latlng]))
      }
      this.pathList.push(event.latlng);
      this.clickList.push(this.map.project(event.latlng, this.map.getMaxZoom() - 1));
    });
  }

  onDrawReady(drawControl: L.Control.Draw) {
    var self = this;

    // Ao clicar em Editar do leaflet-draw, todos os layers ficam verdes.
    this.map.on('draw:editstart', (event) => {
      let layers = event['target']['_renderer']['_layers'];

      for (let key in layers) {
        let layer = layers[key]
        layer['options']['color'] = 'green'
        layer.off('click', onclick)
        this.zoneGroup.bringToFront();
        //** Apply Changes */
        // this.zoneGroup.removeLayer(layer);
        // this.zoneGroup.addLayer(layer);
      }
    });

    this.map.on('draw:editresize', (event) => {
      console.log('Edit Resize');
    });

    this.map.on('draw:created', (e) => {
      let polyline: L.Polyline = e['layer'];
      let i = 0;
      for (let latlng of polyline.getLatLngs()) {
        let pos: L.Point = this.map.project([latlng['lat'], latlng['lng']], this.map.getMaxZoom() - 1);

        console.log(i++ + ': ', '(' + pos.x.toFixed(0) + ',' + -pos.y.toFixed(0) + ')');
      }
    })

    this.map.on('draw:edited', function (e) {
      var layers = e['layers'];
      layers.eachLayer(function (layer) {
        console.log(layer);
        let id = layer.feature.properties.id
        //do whatever you want; most likely save back to db
        let newCoords = self.gisTools.moveZoneEvent(layer);
        // this.locSvc.saveZone(z.id, this.gisTools.toWKT(projection)).subscribe((msg: Response) => {
        self.locSvc.saveZone(id, newCoords).subscribe((msg: Response) => {
          console.log('saved');
        });
        // zone.setStyle(zone.options);
        self.zoneGroup.bringToFront();
      });
    });
  }

  markerClusterReady(markerCluster: L.MarkerClusterGroup) {
    console.log('LocationMap MarkerCluster Ready');
    this.markerClusterGroup = markerCluster;
    this.layers.addLayer(this.markerClusterGroup);
    this.reloadMap();
    window.setInterval(() => this.markerClusterGroup.refreshClusters(), 10000);
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
    console.log(ctr);
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
    this.locSvc.getLocation(this.locationId).subscribe(location => {

      var southWestImg = this.map.unproject([MAP_WIDTH / 2 * -1, MAP_HEIGHT / 2 * -1], this.map.getMaxZoom() - 1);
      var northEastImg = this.map.unproject([MAP_WIDTH / 2, MAP_HEIGHT / 2], this.map.getMaxZoom() - 1);
      var southWestMap = this.map.unproject([MAP_WIDTH * -1, MAP_HEIGHT * -1], this.map.getMaxZoom() - 1);
      var northEastMap = this.map.unproject([MAP_WIDTH, MAP_HEIGHT], this.map.getMaxZoom() - 1);
      var boundsImg = new L.LatLngBounds(southWestImg, northEastImg);
      var boundsMap = new L.LatLngBounds(southWestMap, northEastMap);
      var url = 'assets/maps/' + location.mapfile;

      console.log(url);
      // add the image overlay, so that it covers the entire map
      this.layers.addLayer(L.imageOverlay(url, boundsImg));
      this.map.setMaxBounds(boundsMap);
    });
  }

  ngOnInit() {
    console.log('LocationMap Init');
    this.routeSubscription = this.route.params.subscribe(data => {
      console.log('LocationMap Route Subscription');
      if (this.options.center == null && this.center == null)
        this.options.center = L.latLng([0, 0])
      this.origin = data.origin;
      this.feature = data.feature;
    });
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
            let x = payload['x'];
            let y = payload['y'] * -1;
            let pos = this.map.unproject([x, y], this.map.getMaxZoom() - 1);

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
              let marker = this.createMarker(pos, tagId === '00257984' ? 'drone.svg' : "forklift-red.svg", tagId);

              this.tags.set(tagId, marker);
              this.markers.push(marker.bindPopup(tagId, { closeOnClick: false, autoClose: false }));
              this.markerClusterGroup.addLayer(marker);
            }
            if (this.centerOnEvent)
              this.map.panTo(pos);
          } else if (message.type === RawDataType.SENSOR) {

          } else if (message.type === RawDataType.STATUS) {
            let tagId = message.tagId;
            if (this.tags.has(tagId)) {
              this.tags.get(tagId).bindPopup('TESTE')
            }
          }
        }, (error: any) => {
          console.log("SocketSubscription Error: ") + error;
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
        iconAnchor: [(this.iconSize) / 2, (this.iconSize) / 2]
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
        m.setRadius(e.value / 7.5);
      });
    });
  }

  //TODO: Make trailer object with this
  startTrailEraser() {
    setInterval(() => {
      this.trail.forEach(t => {
        this.trailGroup.removeLayer(t.shift());
      });
    }, 1000 * 1);
  }

  loadZones(zoneObjects: HunterAddress[]) {
    let options = {
      stroke: true,
      color: 'green',
      weight: 1,
      opacity: 1,
      fill: true,
      fillColor: 'blue',
      fillOpacity: 0.3,
      draggable: true
    };

    zoneObjects.forEach((z: HunterAddress) => {
      let ft = this.gisTools.parseWKT(z.wkt);
      let polygons = new Array<L.Polygon<any>>();

      if (ft.type === 'Polygon') {
        polygons.push(L.polygon(this.gisTools.getPolygonCoords(ft.coordinates[0]), options).bindTooltip(z.name));
      } else if (ft.type === 'MultiPolygon') {
        if (false) {
          for (let j = 0; j < ft.coordinates.length; j++) {
            polygons.push(L.polygon(this.gisTools.getPolygonCoords(ft.coordinates[j][0]), options).bindTooltip(z.name));
          }
        }
      }

      for (let zone of polygons) {
        zone.feature = zone.feature || ft; // Intialize layer.feature
        zone.feature.properties = zone.feature.properties || {}; // Intialize feature.properties
        zone.feature.properties.id = z.id;
        zone.feature.properties.wkt = z.wkt;
        if (zone.dragging !== undefined) zone.dragging.disable();
        this.zoneGroup.addLayer(zone);
        this.zoneGroup.bringToFront();
      }
    });
  }

  enableMove(checked) {
    this.moveEnabled = checked;
    this.zoneGroup.getLayers().forEach((zone: L.Polygon) => {
      let id = zone.feature.properties.id;

      if (this.moveEnabled) {
        zone.dragging.enable();
        zone.options.fillColor = 'red';
        zone.on('dragstart', () => this.zoneGroup.bringToFront());
        zone.on('dragend', (e) => {
          let newCoords = this.gisTools.moveZoneEvent(e.target);

          this.locSvc.saveZone(id, newCoords).subscribe((msg: Response) => {
            console.log('saved');
          });
          this.zoneGroup.bringToFront();
        });
      } else {
        zone.options.fillColor = 'blue';
        zone.dragging.disable();
      }
    });
  }

  tempLoadZonesFromRest(locationId: string) {
    this.addressSubscription = this.locSvc.getLocationZones(locationId).subscribe((msg: HunterAddress[]) => {
      this.loadZones(msg);
    });
  }

  loadChildren(locationId: string) {
    this.locationChildren = [];
    this.locationChildren.length = 0;
    this.locationSubscription = this.locSvc.getLocationZones(locationId).subscribe((msg: HunterAddress[]) => {
      if (msg != undefined) {
        console.log(msg);
        this.locationChildren.push(msg);
      }
    });
  }

  copyTable(inputElement: Table) {
    let select = window.getSelection();
    let range = document.createRange();

    range.selectNodeContents(inputElement.el.nativeElement);
    select.removeAllRanges();
    select.addRange(range);
    document.execCommand('copy');
    select.empty();
  }

  clearPath() {
    this.clickList = this.clickList.slice(this.clickList.length);
    this.pathList = this.pathList.slice(this.pathList.length);
    this.pathGroup.clearLayers();
  }
}