import { HttpClient } from "@angular/common/http";
import { Component, OnChanges, OnDestroy, OnInit, SimpleChange, SimpleChanges } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import * as L from 'leaflet';
import { tileLayer } from 'leaflet';
import 'leaflet-draw-drag';
import 'leaflet-easybutton';
import 'leaflet-path-drag';
import 'leaflet-path-transform';
import 'leaflet.fullscreen';
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { GisTools } from "../../gis/tools/GisTools";
import { TokenService } from "../../security/services/token.service";
import { LocationData } from "../../shared/classes/LocationData";
import { LocationInit } from "../../shared/classes/LocationInit";
import { ProcessMessage } from "../../shared/classes/ProcessMessage";
import { HunterAddress } from "../../shared/model/HunterAddress";
import { SocketService } from "../../shared/services/socket.service";

declare var require: any;

@Component({
    templateUrl: './continuous-location.component.html'
})
export class ContinuousLocationComponent implements OnInit, OnDestroy, OnChanges {
    private map: L.Map;
    private tags: Map<String, L.Marker> = new Map<String, L.Marker>();
    private socketSubscription: Subscription;
    private stream: Observable<any>;
    private gisTools: GisTools;

    connected: Boolean;
    locationName: String;
    mapType: string = "Satellite";
    mapStyle: string = "";
    locData: LocationData[];
    iconSize: number = 15;

    // Grupo de layers das zonas.
    zones = new L.FeatureGroup();
    // Grupo de layers das zonas.
    markers = new L.FeatureGroup();
    // Todos os layers que vão ser carregados no mapa
    layers = new L.LayerGroup()
        // .addLayer(this.things)
        .addLayer(this.zones)
        .addLayer(this.markers)
    public options: any = {
        preferCanvas: true,
        zoom: 18,
        minZoom: 0,
        maxZoom: 18,
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

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) { }

    onMapReady(map: L.Map) {
        console.log('LocationMap MapReady');
        this.gisTools = new GisTools(map);
        this.map = map;
        this.map.addLayer(this.layers);
    }

    ngOnChanges(changes: SimpleChanges) {
        const ctr: SimpleChange = changes.center;
        if (!ctr.firstChange) {
            this.options.center = L.latLng(ctr.currentValue);
        }
    }

    mapClick(ev) {
        console.log(ev);
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.locData = new Array();
            this.locData.length = 0;
            this.stream = this.socket.connect(environment.wsprocess + 'process/' + params.process);
            this.socketSubscription = this.stream.subscribe(
                (msgStr: any) => { //TODO: StringMODAFOCA
                    let msg: ProcessMessage = JSON.parse(msgStr);
                    if (msg.command === 'LocationInit') {
                        let data: LocationInit = msg.data;
                        console.log(data);
                        this.locationName = data.name;
                        this.options.center = L.latLng(data.center);
                        this.connected = true;
                        this.loadLocationZones(data.addresses);
                    } else if (msg.command === 'LocationData') {
                        let data: LocationData = msg.data;
                        console.log(data);
                        let tagId = data.tagId;
                        let pos = L.latLng(data.latitude, data.longitude);

                        if (this.tags.has(tagId)) {
                            this.tags.get(tagId).setLatLng(pos);
                        } else
                            this.tags.set(tagId, this.createMarker(pos, data.icon, tagId).addTo(this.markers));
                        this.markers.bringToFront();
                        this.locData.push(data);
                    } else
                        console.log("Action " + msg.command + " não definido");
                }, (error: any) => {
                    console.log("SocketSubscription Error: ") + error;
                }, () => {
                    console.log("SocketSubscription Complete");
                }
            );
        });
        this.layers.addLayer(tileLayer('http://api.tiles.mapbox.com/v4/mapbox.satellite/{z}/{x}/{y}@2x.png256?style=mapbox://styles/mapbox/satellite-streets-v11@11&access_token=pk.eyJ1IjoibWF0ZXVzdG9ybWluIiwiYSI6ImNqdGE3cTltbjA3ems0NG51dW4zY2F3am0ifQ.-_T_cUTzFaPhX6oDrD3kBw', {
            attribution: '&copy; GTP AUTOMATION'
        }));
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
            this.zones.addLayer(zone);
            this.zones.bringToFront();
        });
    }

    loadLocationZones(addresses: HunterAddress[]) {
        let addArr = new Array<HunterAddress>();

        addresses.forEach(m => {
            let add = new HunterAddress({
                name: m.name,
                wkt: m.wkt
            });

            addArr.push(add);
        });

        console.log('Address: ' + addArr.length);
        this.loadZones(false, addArr);
    }

    convertToWGS84(latM: number, lngM: number, rot: number = 0): L.LatLng {
        let mapCenter = [-5.876181, -35.315814];
        let bH = ((lngM > 0 ? 90 : 270) + rot) % 360;
        let bV = ((latM > 0 ? 0 : 180) + rot) % 360;
        let transLng = this.gisTools.getPointAtBearingAndDistance(mapCenter[0], mapCenter[1], bH, Math.abs(lngM));

        return this.gisTools.getPointAtBearingAndDistance(transLng.lat, transLng.lng, bV, Math.abs(latM));
    }

    reload() {
        this.ngOnDestroy();
        this.ngOnInit();
    }

    ngOnDestroy() {
        if (this.socketSubscription != null)
            this.socketSubscription.unsubscribe();
    }
}