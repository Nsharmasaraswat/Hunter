import { HttpClient } from "@angular/common/http";
import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChange, SimpleChanges } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import "@babel/polyfill";
import * as shp from "shpjs";
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
import { environment } from "../../../environments/environment";
import { GisTools } from '../../gis/tools/GisTools';
import { TokenService } from "../../security/services/token.service";
import { RawData } from '../../shared/classes/RawData';
import { RawDataType } from '../../shared/model/enum/RawDataType';
import { HunterAddress } from '../../shared/model/HunterAddress';
import { SocketService } from "../../shared/services/socket.service";

declare var require: any;
const MAX_TRAIL_POINTS: number = 15;

@Component({
    selector: 'cambuhy',
    templateUrl: './tmp-cambuhy.component.html',
    styleUrls: ['./tmp-cambuhy.component.scss']
})

export class TmpCambuhyComponent implements OnInit, OnDestroy, OnChanges {

    private map: L.Map;
    private tags: Map<string, L.Marker> = new Map<string, L.Marker>();
    private colors: Map<string, string> = new Map<string, string>();
    private gisTools;
    private socketSubscription: Subscription;
    private locationSubscription: Subscription;
    private addressSubscription: Subscription;
    private originSubscription: Subscription;
    private routeSubscription: Subscription;
    private stream: Observable<any>;
    private locationChildren: any[];

    centerOnEvent: boolean = true;
    showOptions: boolean = true;
    iconSize: number = 15;
    msgs: any[] = [];
    origin: string = "a4d90b6b-8a27-4eb3-9451-332fd684eb2b";
    feature: string = "FLOCATE";
    mapType: string = "Satellite";
    mapStyle: string = "";
    @Input("center") center: [number, number];
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
        // .addLayer(this.things)
        .addLayer(this.zoneGroup)
        // .addLayer(this.markerGroup)
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
    fileUploadREST = environment.coreserver + 'file/cambuhy';

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    onMapReady(map: L.Map) {
        console.log('LocationMap MapReady');
        this.gisTools = new GisTools(map);
        this.map = map;
        this.map.addLayer(this.layers);
        const geo = new L.GeoJSON(
			{
                "type": "Feature",
            },
			{
				onEachFeature: function popUp(f, l) {
					const out = [];
					if (f.properties) {
						for (const key of Object.keys(f.properties)) {
							out.push(key + " : " + f.properties[key]);
						}
						l.bindPopup(out.join("<br />"));
					}
				}
			}
        ).addTo(this.map);
        const base = "assets/shapefiles/Cambuhy_Ruas_900913.zip";
		shp(base).then(function(data) {
			geo.addData(data);
		});
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
            this.tempLoadZonesCambuhy();
        }
    }

    ngOnInit(): void {
        console.log('LocationMap Init');
        this.routeSubscription = this.route.params.subscribe(data => {
            console.log('LocationMap Route Subscription');
            if (this.options.center == null && this.center == null)
                this.options.center = L.latLng([-21.641076100000003, -48.498271700000004])
        });
        this.layers.addLayer(tileLayer('https://api.tiles.mapbox.com/v4/mapbox.satellite/{z}/{x}/{y}@2x.png256?style=mapbox://styles/mapbox/satellite-streets-v11@11&access_token=pk.eyJ1IjoibWF0ZXVzdG9ybWluIiwiYSI6ImNqdGE3cTltbjA3ems0NG51dW4zY2F3am0ifQ.-_T_cUTzFaPhX6oDrD3kBw', {
            attribution: '&copy; GTP AUTOMATION',
            maxZoom: 20
        }));
    }

    upload(event) {
        let size = event.files.length;
        let summary = size + (size == 1 ? ' Arquivo Enviado' : ' Arquivos Enviados');

        this.msgSvc.add({ severity: 'info', summary: summary, detail: 'Iniciando Processamento' });
    }

    beforeUpload(event) {
        console.log(event.xhr);
        console.log(event.xhr.prototype);
    }

    connectOrigin() {
        this.conectWebsocket();
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
                            let lastPos = this.tags.get(tagId).getLatLng();
                            let popupContent = "<div class='cambuhy_content'>";
                            popupContent += '<div class="cambuhy_item">CD_EQUIPTO\t: ' + payload.tagId + '</div>';
                            popupContent += '<div class="cambuhy_item">TP_EQUIPTO\t: ' + payload.tipoEq + '</div>';
                            popupContent += '<div class="cambuhy_item">NM_EQUIPTO\t: ' + payload.desc + '</div>';
                            popupContent += '<div class="cambuhy_item">DH_EVENTO\t: ' + payload.eventTime + '</div>';
                            popupContent += '<div class="cambuhy_item">CD_ATIVIDADE\t: ' + payload.codAtiv + '</div>';
                            popupContent += '<div class="cambuhy_item">NM_ATIVIDADE\t: ' + payload.descAtiv + '</div>';
                            popupContent += '<div class="cambuhy_item">VA_TEMPO_TRABALHO_SS\t: ' + payload.tempoTrabalho + '</div>';
                            popupContent += '<div class="cambuhy_item">VA_LATITUDE\t: ' + payload.latitude + '</div>';
                            popupContent += '<div class="cambuhy_item">VA_LONGITUDE\t: ' + payload.longitude + '</div>';
                            popupContent += '<div class="cambuhy_item">CD_COLABORADOR\t: ' + payload.codColab + '</div>';
                            popupContent += '<div class="cambuhy_item">NM_COLABORADOR\t: ' + payload.descColab + '</div>';
                            popupContent += '<div class="cambuhy_item">CD_FRENTE\t: ' + payload.cdFrente + '</div>';
                            popupContent += '<div class="cambuhy_item">NM_FRENTE\t: ' + payload.descFrente + '</div>';
                            popupContent += '<div class="cambuhy_item">VA_RPM\t: ' + payload.rpm + '</div>';
                            popupContent += '<div class="cambuhy_item">VA_VELOCIDADE\t: ' + payload.speed + '</div>';
                            popupContent += '</div>';
                            this.createTrailMarker(lastPos, tagId).bindPopup(popupContent, { className: 'cambuhy_tooltip', autoPan: true }).addTo(this.trailGroup);
                            this.tags.get(tagId).setLatLng(pos);
                        } else {
                            let marker = this.createMarker(pos, "truck-white.svg", tagId);

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
        let color: string = this.colors.has(tooltip) ? this.colors.get(tooltip) : this.getRandomColor();

        this.colors.set(tooltip, color);
        return L.circleMarker(position, {
            color: color,
            fillColor: color,
            fillOpacity: 0.8,
            radius: this.iconSize / 5
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

    tempLoadZonesCambuhy() {
        //this.loadZones(false, addArr);
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

    getRandomColor(): string {
        var color = Math.floor(0x1000000 * Math.random()).toString(16);
        return '#' + ('000000' + color).slice(-6);
    }
}