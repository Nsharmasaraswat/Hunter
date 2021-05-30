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
import { Subscription } from "rxjs";
import { GisTools } from '../../gis/tools/GisTools';
import { LocationRestService } from '../../process/services/location-rest.service';
import { HunterAddress } from '../../shared/model/HunterAddress';
import { SocketService } from "../../shared/services/socket.service";

const MAX_TRAIL_POINTS: number = 10;

@Component({
  selector: 'visual-ocupation',
  templateUrl: './visual-ocupation.component.html',
  styleUrls: ['visual-ocupation.component.scss']
})

export class VisualOcupationComponent implements OnInit, OnDestroy, OnChanges {
  private map: L.Map;
  private gisTools: GisTools;
  private locationSubscription: Subscription;
  private addressSubscription: Subscription;
  private routeSubscription: Subscription;

  showOptions: boolean = true;
  moveEnabled: boolean = false;
  msgs: any[] = [];
  itens: any = {};
  locationId: string;
  @Input("center") center: [number, number];
  @Input("autoLoadZones") autoLoadZones: boolean = true;

  freeDraw = new FreeDraw({
    mode: NONE,
    leaveModeAfterCreate: true,
    strokeWidth: 1
  });

  // Grupo de layers das zonas.
  zoneGroup = new L.FeatureGroup();

  // Grupo de layers das zonas.
  markerClusterGroup: L.MarkerClusterGroup;
  markers: L.Marker[] = [];
  // Grupo de layers dos rastros.
  trailGroup = new L.FeatureGroup();

  // Todos os layers que vão ser carregados no mapa
  layers = new L.LayerGroup()
    // .addLayer(this.things)
    .addLayer(this.zoneGroup)
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
    edit: {
      featureGroup: this.zoneGroup,
      remove: false,
      transform: false
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
      }
    });

    this.map.on('draw:editresize', (event) => {
      console.log('Edit Resize');
    });

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
    this.locSvc.getLocation(this.locationId).subscribe(location => {
      let width = 98.6 * 200;
      let height = 84.5 * 200;
      var southWestImg = this.map.unproject([width / 2 * -1, height / 2 * -1], this.map.getMaxZoom() - 1);
      var northEastImg = this.map.unproject([width / 2, height / 2], this.map.getMaxZoom() - 1);
      var southWestMap = this.map.unproject([width * -1, height * -1], this.map.getMaxZoom() - 1);
      var northEastMap = this.map.unproject([width, height], this.map.getMaxZoom() - 1);
      var boundsImg = new L.LatLngBounds(southWestImg, northEastImg);
      var boundsMap = new L.LatLngBounds(southWestMap, northEastMap);
      var url = 'assets/maps/' + location.mapfile;

      console.log(url);
      // add the image overlay, so that it covers the entire map
      this.layers.addLayer(L.imageOverlay(url, boundsImg));
      this.map.setMaxBounds(boundsMap);
      if (this.autoLoadZones) {
        this.tempLoadZonesFromRest(this.locationId);
      }
    });
  }

  ngOnInit() {
    console.log('LocationMap Init');
    this.routeSubscription = this.route.params.subscribe(data => {
      console.log('LocationMap Route Subscription');
      this.locationId = data.locationId;
      if (this.options.center == null && this.center == null)
        this.options.center = L.latLng([0, 0])
    });
  }

  ngOnDestroy() {
    if (this.addressSubscription != null)
      this.addressSubscription.unsubscribe();
    if (this.locationSubscription != null)
      this.locationSubscription.unsubscribe();
    if (this.routeSubscription != null)
      this.routeSubscription.unsubscribe();
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
      draggable: false
    };

    zoneObjects.forEach((z: HunterAddress) => {
      let ft = this.gisTools.parseWKT(z.wkt);
      let polygons = new Array<L.Polygon<any>>();

      if (ft.type === 'Polygon') {
        polygons.push(L.polygon(this.gisTools.getPolygonCoords(ft.coordinates[0]), options).bindTooltip(z.name));
      } else if (ft.type === 'MultiPolygon') {
        for (let j = 0; j < ft.coordinates.length; j++) {
          polygons.push(L.polygon(this.gisTools.getPolygonCoords(ft.coordinates[j]), options).bindTooltip(z.name));
        }
      }

      for (let zone of polygons) {
        zone.feature = zone.feature || ft; // Intialize layer.feature
        zone.feature.properties = zone.feature.properties || {}; // Intialize feature.properties
        zone.feature.properties.id = z.id;
        zone.feature.properties.wkt = z.wkt;
        // if (zone.dragging)
        //   zone.dragging.disable();
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
    this.addressSubscription = this.locSvc.listLocationZonesByType(locationId, 'ADDRESS').subscribe((msg: HunterAddress[]) => {
      this.loadZones(msg);
    });
  }
}