import { HttpClient } from '@angular/common/http';
import { Component, HostListener, NgZone, OnChanges, OnDestroy, OnInit, SimpleChange, SimpleChanges } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import "@babel/polyfill";
import * as L from 'leaflet';
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
import { ProcessMessage } from '../../shared/classes/ProcessMessage';
import { RawData } from '../../shared/classes/RawData';
import { RawDataType } from '../../shared/model/enum/RawDataType';
import { HunterAddress } from '../../shared/model/HunterAddress';
import { HunterDocument } from '../../shared/model/HunterDocument';
import { HunterLocation } from '../../shared/model/HunterLocation';
import { HunterThing } from '../../shared/model/HunterThing';
import { SocketService } from "../../shared/services/socket.service";

const MAX_TRAIL_POINTS: number = 10;
const MAP_WIDTH: number = 98.6 * 200;//197.6m - 197.15
const MAP_HEIGHT: number = 84.5 * 200;//171.8m - 168.95
const DRONE_HEIGHT: number = 600;
const DIST_ERROR: number = 20;

class TagInfo {
    constructor(public marker: L.Marker, public thing: HunterThing) {
    }
}

class DisplayInfo {
    name: string;
    status: string;
    position: string;
}

class InventoryItem {
    address: HunterAddress;
    thing: HunterThing;
    count: number;

    constructor(init) {
        if (init !== undefined) {
            this.address = new HunterAddress(init.address);
            this.thing = new HunterThing(init.thing);
            this.count = init.count;
        }
    }
}

@Component({
    selector: 'droneInventoryProcessComponent',
    templateUrl: './droneinventoryprocess.component.html',
    styleUrls: ['./droneinventoryprocess.component.scss']
})
export class DroneInventoryProcessComponent implements OnInit, OnDestroy, OnChanges {
    private map: L.Map;
    private tags: Map<String, TagInfo> = new Map<String, TagInfo>();
    private trailers: Map<String, L.CircleMarker[]> = new Map<String, L.CircleMarker[]>();
    private gisTools: GisTools;
    private socketSubscription: Subscription;
    private locationSubscription: Subscription;
    private addressSubscription: Subscription;
    private routeSubscription: Subscription;
    private navigationSubscription: Subscription;
    private stream: Observable<any>;
    private docId: string;
    private droneId: string;
    private procId: string;

    heightOk: boolean = false;
    displayInfo: boolean = false;
    info: DisplayInfo;
    leaveTrail: boolean = true;
    centerOnEvent: boolean = true;
    iconSize: number = 100;
    measured_height: number;
    measured_dist: number;
    msgs: any[] = [];
    document: HunterDocument;
    thing: HunterThing;
    center: [number, number];

    imgLayer: L.ImageOverlay;
    // Grupo de layers das zonas.
    zoneGroup = new L.FeatureGroup();
    // Grupo de layers das zonasInventariadas.
    inventoryGroup = new L.FeatureGroup();
    //Grupo de layers dos markers
    markerGroup = new L.FeatureGroup();
    // Grupo de layers dos rastros.
    trailGroup = new L.FeatureGroup();

    // Todos os layers que vão ser carregados no mapa
    layers = new L.LayerGroup()
        .addLayer(this.zoneGroup)
        .addLayer(this.inventoryGroup)
        .addLayer(this.markerGroup)
        .addLayer(this.trailGroup);


    public options: any = {
        preferCanvas: true,
        zoom: 3,
        minZoom: 0,
        maxZoom: 6,
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

    constructor(private msgSvc: MessageService, private http: HttpClient, private socket: SocketService,
        private router: Router, private route: ActivatedRoute, private zone: NgZone) {
        console.log('LocationMap Constructor');
    }

    ngOnInit() {
        console.log('LocationMap Init');
        this.routeSubscription = this.route.params.subscribe(data => {
            console.log('LocationMap Route Subscription');
            this.docId = data.document;
            this.droneId = data.thing;
            this.procId = data.process;
            if (this.options.center == null && this.center == null)
                this.options.center = L.latLng([0, 0]);

            this.markerGroup.on('click',
                (data: any) => {
                    this.zone.run(() => {
                        let tagInfo: TagInfo = this.tags.get(data.layer.options.title);//Marker and Thing

                        this.displayInfo = !this.displayInfo;
                    });
                });
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        const ctr: SimpleChange = changes.center;
        if (!ctr.firstChange) {
            console.log('LocationMap Changes', ctr);
            this.options.center = L.latLng(ctr.currentValue);
            console.log("Reloading Map");
            this.reloadMap();
        } else if (this.center != null)
            this.options.center = L.latLng(this.center);
        console.log(ctr);
    }

    @HostListener('window:beforeunload', ['$event'])
    beforeUnloadHander(event) {
        console.log("beforeUnload");
        this.unsubscribeObservables();
    }

    ngOnDestroy() {
        console.log("destroy");
        this.unsubscribeObservables();
    }

    onMapReady(map: L.Map) {
        console.log('LocationMap MapReady');
        this.gisTools = new GisTools(map);
        this.map = map;
        this.map.on('zoomend', (ev: L.LeafletEvent) => {
            this.zone.run(() => {
                this.resize();
                this.trailers.clear();
            });
        });
        this.map.addLayer(this.layers);

        this.navigationSubscription = this.http.get(environment.processserver + 'document/' + this.docId).subscribe(
            (document: HunterDocument) => {
                this.document = new HunterDocument(document);
            }, (error: any) => {
                console.log("NavigationSubscription Error: ", error);
            }, () => {
                console.log("NavigationSubscription Complete:", this.document);
            }
        );
        this.navigationSubscription = this.http.get(environment.processserver + 'thing/' + this.droneId).subscribe(
            (thing: HunterThing) => {
                this.thing = new HunterThing(thing);
            }, (error: any) => {
                console.log("NavigationSubscription Error: ", error);
            }, () => {
                console.log("NavigationSubscription Complete", this.thing);
            }
        );

        this.conectWebsocket(this.procId);

        // const legend = new L.Control({ position: 'topright' });

        // legend.onAdd = map => {
        //     let div = L.DomUtil.create('div', 'drone_options');
        //     let html = '<p-panel>';
        //     html += '<div class="row">';
        //     html += '<div class="col">';
        //     html += '<span [ngClass]="(\'center_row \') + (heightOk ? \'dot_ok\' : \'dot_nok\')"></span>';
        //     html += '</div>';
        //     html += '</div>';
        //     html += '<div class="row">';
        //     html += '<div class="col">';
        //     html += '<h3>H:{{measured_height}}</h3>';
        //     html += '</div>';
        //     html += '</div>';
        //     html += '<div class="row">';
        //     html += '<div class="col">';
        //     html += '<h3>F:{{measured_dist}}</h3>';
        //     html += '</div>';
        //     html += '</div>';
        //     html += '<div class="row">';
        //     html += '<div class="col">';
        //     html += '<p>Ícone {{iconSize}}%</p>';
        //     html += '<p-slider [(ngModel)]="iconSize" [min]="1" [max]="100" (onChange)="resize()"></p-slider>';
        //     html += '</div>';
        //     html += '</div>';
        //     html += '<p-footer>';
        //     html += '<p-button label="Finalizar" (click)="close();" [ngClass]="\'center_row\'"></p-button>';
        //     html += '</p-footer>';
        //     html += '</p-panel>';

        //     div.innerHTML = html;
        //     return div;
        // };
        // legend.addTo(map);
        this.startTrailEraser();
    }

    reloadMap() {
        this.zoneGroup.clearLayers();
        this.trailGroup.clearLayers();
        this.markerGroup.clearLayers();
        this.inventoryGroup.clearLayers();
        this.map.panTo(this.options.center);
        this.unsubscribeObservables();
    }

    conectWebsocket(processId: string): void {
        if (processId != null) {
            console.log('Conenctando no Process: ' + processId);
            this.msgs.length = 0;
            this.stream = this.socket.connect(environment.wsprocess + 'process/' + processId);
            this.socket.hideLoadIndicator();
            this.socketSubscription = this.stream.subscribe(
                (message: ProcessMessage) => {
                    try {
                        switch (message.command) {
                            case 'LoadLocation':
                                this.loadLocation(new HunterLocation(message.data));
                                break;
                            case 'DisplayAddress':
                                this.loadZones(message.data.map(a => new HunterAddress(a)).filter(a => a.model.metaname !== 'RACK'));
                                break;
                            case 'MarkerData':
                                this.processRawData(new HunterThing(message.data));
                                break;
                            case 'ShowInventory':
                                this.inventoryGroup.clearLayers();
                                console.log(message);
                                message.data.forEach(a => this.displayInventory(new InventoryItem(a)));
                                break;
                            case 'COMPELTE':
                                console.log('Check inventory 1');
                                this.checkInventory();
                                break;
                        }
                    } catch (ex) {
                        console.log(ex);
                    }
                }, (error: any) => {
                    console.log("SocketSubscription Error: ") + error;
                }, () => {
                    console.log("SocketSubscription Complete ");
                }
            );
        }
    }

    displayInventory(item: InventoryItem): void {
        let color = 'white';
        switch (item.count) {
            case 1:
                color = 'orange';
                break;
            case 2:
                color = 'green';
                break;
        }
        let options = {
            stroke: true,
            color: 'green',
            weight: 1,
            opacity: 1,
            fill: true,
            fillColor: color,
            fillOpacity: 0.4,
            draggable: false
        };

        this.addZone(item.address, options, this.inventoryGroup);
    }

    loadLocation(location: HunterLocation): void {
        var southWestImg = this.map.unproject([-MAP_WIDTH / 2, -MAP_HEIGHT / 2], this.map.getMaxZoom() - 1);
        var northEastImg = this.map.unproject([MAP_WIDTH / 2, MAP_HEIGHT / 2], this.map.getMaxZoom() - 1);
        var southWestMap = this.map.unproject([-MAP_WIDTH / 2, -MAP_HEIGHT / 2], this.map.getMaxZoom() - 1);
        var northEastMap = this.map.unproject([MAP_WIDTH / 2, MAP_HEIGHT / 2], this.map.getMaxZoom() - 1);
        var boundsImg = new L.LatLngBounds(southWestImg, northEastImg);
        var boundsMap = new L.LatLngBounds(southWestMap, northEastMap);
        var url = 'assets/maps/' + location.mapfile;

        console.log('Map: ', this.map);
        this.imgLayer = L.imageOverlay(url, boundsImg);
        // add the image overlay, so that it covers the entire map
        this.layers.addLayer(this.imgLayer);
        this.map.setMaxBounds(boundsMap);
    }

    loadZones(zoneObjects: HunterAddress[]): void {
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

        zoneObjects.forEach((z: HunterAddress) => this.addZone(z, options, this.zoneGroup));
    }

    addZone(z: HunterAddress, options: any, group: L.FeatureGroup): void {
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
            group.addLayer(zone);
            group.bringToFront();
        }
    }

    processRawData(thing: HunterThing): void {
        //console.log('SocketSubscription Next: ', thing);
        if (thing.payload !== undefined) {
            let rawData = new RawData(JSON.parse(thing.payload));
            let payload = JSON.parse(rawData.payload);
            let tagId = rawData.tagId;

            this.msgs.push(rawData);
            if (this.msgs.length > 1000)
                this.msgs.shift();
            if (rawData.type === RawDataType.LOCATION) {
                let lat = payload['x'];
                let lng = payload['y'] * -1;
                let pos = this.map.unproject([lat, lng], this.map.getMaxZoom() - 1);

                if (this.tags.has(tagId)) {
                    if (this.leaveTrail) {
                        let lastPos = this.tags.get(tagId).marker.getLatLng();
                        let trailPoint = this.createTrailMarker(lastPos).addTo(this.trailGroup);
                        let trailHistory = this.trailers.has(tagId) ? this.trailers.get(tagId) : new Array<L.CircleMarker>();

                        trailHistory.push(trailPoint);
                        this.trailers.set(tagId, trailHistory);
                        if (trailHistory.length > MAX_TRAIL_POINTS) {
                            this.trailGroup.removeLayer(trailHistory.shift());
                        }
                    }
                    this.tags.get(tagId).marker.setLatLng(pos);
                    this.tags.get(tagId).thing = thing;
                } else {
                    let marker = this.createMarker(pos, "drone.svg", tagId);

                    marker.bindTooltip(tagId);
                    this.tags.set(tagId, new TagInfo(marker, thing));
                    this.markerGroup.addLayer(marker);
                }
                if (this.centerOnEvent)
                    this.map.panTo(pos);
            } else if (rawData.type === RawDataType.SENSOR) {
                let color = 'white';
                let dist = +payload.value;

                if (rawData.port === 0) {
                    this.heightOk = dist >= (DRONE_HEIGHT - DIST_ERROR) && dist <= (DRONE_HEIGHT + DIST_ERROR);
                    if (dist < 1000)
                        this.measured_height = dist;
                } else if (dist < 1000)
                    this.measured_dist = dist;
                switch (dist) {
                    case 1:
                        color = 'orange';
                        break;
                    case 2:
                        color = 'green';
                        break;
                }
                if (this.tags.has(tagId))
                    this.createDistanceMarker(this.tags.get(tagId).marker.getLatLng(), color);
            } else if (rawData.type === RawDataType.STATUS) {
                let tagId = rawData.tagId;
                if (this.tags.has(tagId))
                    this.tags.get(tagId).marker.bindPopup('TESTE')
            }
        }
    }

    createMarker(position: L.LatLng, icon: string, tooltip: string): L.Marker {
        let sz = this.calcIconSize();

        return L.marker(position, {
            icon: L.icon({
                iconUrl: 'assets/icons/' + icon,
                iconSize: [sz, sz],
                iconAnchor: [(sz) / 2, (sz) / 2]
            }),
            clickable: true,
            draggable: false,
            title: tooltip,
            riseOnHover: true
        });
    }

    createDistanceMarker(position: L.LatLng, color: string): L.CircleMarker {
        return L.circleMarker(position, {
            radius: this.calcIconSize() / 9,
            fillOpacity: 1,
            color: color
        });
    }

    createTrailMarker(position: L.LatLng): L.CircleMarker {
        return this.createDistanceMarker(position, 'blue');
    }

    resize() {
        let newSize = this.calcIconSize();

        this.tags.forEach(ti => {
            let m = ti.marker;
            let curIcon = m.options.icon;


            curIcon.options.iconSize = [newSize, newSize];
            curIcon.options.iconAnchor = [newSize / 2, newSize / 2];
            m.setIcon(curIcon);
        });
        this.trailers.forEach(t => {
            t.forEach(m => {
                m.setRadius(newSize / 7.5);
            });
        });
    }

    calcIconSize(): number {
        let fn = (3 * Math.pow(this.map.getZoom(), 6)
            - 47 * Math.pow(this.map.getZoom(), 5)
            + 275 * Math.pow(this.map.getZoom(), 4)
            - 625 * Math.pow(this.map.getZoom(), 3)
            + 802 * Math.pow(this.map.getZoom(), 2)
            - 168 * this.map.getZoom()
            + 240)
            / 120;
        let newSize = (this.iconSize / 100) * fn;


        return newSize;
    }

    //TODO: Make trailer object with this
    startTrailEraser() {
        setInterval(() => {
            this.trailers.forEach((t, tagId) => {
                if (t.length > 0) {
                    let trail = t.shift();

                    //console.log('Removing:' + tagId, trail);
                    this.trailGroup.removeLayer(trail);
                    //console.log('Left', t.length);
                } else
                    this.trailers.delete(tagId);
            });
        }, 1000 * 1);
    }

    close(): void {
        console.log('Check inventory 0 (close)');
        this.socket.send({ "command": "FINISH", "data": "" });
        this.checkInventory();
    }

    unsubscribeObservables() {
        if (this.socketSubscription != null)
            this.socketSubscription.unsubscribe();
        if (this.addressSubscription != null)
            this.addressSubscription.unsubscribe();
        if (this.navigationSubscription != null)
            this.navigationSubscription.unsubscribe();
        if (this.locationSubscription != null)
            this.locationSubscription.unsubscribe();
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
        if (this.socket != null)
            this.socket.disconnect();
    }

    checkInventory() {
        console.log('Check inventory');
        this.unsubscribeObservables();
        this.router.navigate(['home', 'custom-solar', 'check-inventory', this.document.id]);
    }
}