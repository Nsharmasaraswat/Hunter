import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import { FeatureGroup, icon, imageOverlay, LatLng, latLng, marker, Marker, popup } from 'leaflet';
import { MessageService } from 'primeng/components/common/messageservice';
import { Observable } from 'rxjs/Rx';
import { environment } from '../../../environments/environment';
import { HunterLocation } from '../../shared/model/HunterLocation';


@Injectable()
export class LocationRestService {

  public map: L.Map;

  warehouse = imageOverlay(
    // '../assets/imgs/videoio_overview.svg',
    'http://www.anteprojectos.com.pt/wp-content/uploads/2015/03/Planta-de-Projeto-Model.jpg',
    [[-20.00008, 20.00008], [20.00008, -20.00008]]
  )
  center: L.LatLng = latLng(48.699915252914806, 9.699884679069456);//Daimler
  //center = latLng([-23.577609, -46.720393]) //GTP

  circle = {
    shape: 'Circle',
    ref: 'RD05',
    latLng: [-13.00000, 3.00000],
    radius: 99999.99999
  }

  rec = {
    shape: 'Polygon',
    ref: 'Q104',
    latLng: [[8.00002, -3.00002], [-8.00002, -3.00002], [-8.00002, 3.00002], [8.00002, 3.00002]],
  }

  poly = {
    shape: 'Polygon',
    ref: 'D30',
    latLng: [[4.915832801313164, 10.239257812500002], [0.9228116626857066, 6.987304687500001],
    [-5.178482088522876, 10.678710937500002], [0.7909904981540058, 13.403320312500002]],
  }

  pins = [
    marker([-2.00004, -4.00003], {
      icon: icon({
        iconSize: [20, 20],
        iconUrl: '../assets/imgs/target.svg'
      })
    }).bindPopup(popup().setContent(
      'Center'
    )),
    marker([-1.2002, 3.5002], {
      icon: icon({
        iconSize: [20, 20],
        iconUrl: '../assets/imgs/profile.jpg'
      })
    }).bindPopup(popup().setContent(
      'South West'
    )),
    marker([0, 0], {
      icon: icon({
        iconSize: [20, 20],
        iconUrl: '../assets/imgs/truck.svg'
      })
    }).bindPopup(popup().setContent(
      'North West'
    )),
    marker([48.70013964862771, 9.700407910807602], {
      icon: icon({
        iconSize: [20, 20],
        iconUrl: '../assets/imgs/target.svg'
      })
    }).bindPopup(popup().setContent(
      'North East'
    )),
    marker([48.6996845360823, 9.700401596290511], {
      icon: icon({
        iconSize: [20, 20],
        iconUrl: '../assets/imgs/target.svg'
      })
    }).bindPopup(popup().setContent(
      'South East'
    ))
  ];

  constructor(
    private http: HttpClient,
    private msgSvc: MessageService,
  ) { }

  getLocation(locId: string): Observable<HunterLocation> {
    return this.http.get(environment.processserver + 'location/' + locId).map((t: HunterLocation) => t);
  }

  getLocationZones(locId: string): Observable<any> {
    return this.http.get(environment.processserver + 'address/bylocation/' + locId);
  }

  listLocationZonesByType(locId: string, type: string): Observable<any> {
    return this.http.get(environment.processserver + 'address/bytypeandlocation/' + type + '/' + locId);
  }

  getCenter(): Observable<LatLng> {
    return Observable.of(this.center)
  }

  getPins(): Observable<Marker[]> {
    return Observable.of(this.pins)
  }

  getOrigins() {
    return this.http.get(environment.processserver + "origin")
  }

  saveZones(zones: FeatureGroup) {
    console.log(zones)
  }

  loadData() {
    return this.http.get(environment.processserver + "address")
  }

  saveZone(id: string, wkt: string): Observable<Response> {
    return this.http.put(environment.processserver + "address/wkt/" + id, wkt).map((r: Response) => r);
  }
}
