import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, Renderer2 } from "@angular/core";
import "@babel/polyfill";
// import { CustomCircle, CustomPolygon } from './custom-shapes.model';
import * as turf from '@turf/turf';
import * as L from 'leaflet';
// declare var turf: any;
import { Circle, Polygon, tileLayer } from 'leaflet';
import 'leaflet-draw-drag';
import 'leaflet-easybutton';
import FreeDraw, { NONE } from 'leaflet-freedraw';
import 'leaflet-path-drag';
import 'leaflet-path-transform';
import 'leaflet.fullscreen';
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { RawData } from "../../shared/classes/RawData";
import { RawDataType } from '../../shared/model/enum/RawDataType';
import { SocketService } from "../../shared/services/socket.service";

class CustomCircle extends Circle {
  ref: string
  shape: string = 'Circle'
}


class CustomPolygon extends Polygon {
  ref: string
  shape: string = 'Polygon'
}

class WeatherSensor {
  constructor(public name: string, public value: number, public unit: string, public variance: number) { }
}

export { CustomCircle, CustomPolygon };


@Component({
  selector: 'droneControllerComponent',
  templateUrl: './drone-controller.component.html',
  styleUrls: ['drone-controller.component.scss']
})

export class DroneControllerComponent implements OnInit, OnDestroy {

  public polylinesParalelas = []
  public pontosInterceptados = []
  public markerG
  public distanciaEntrePalelalas = 5
  private iconSize: number = 15;

  public map: L.Map;

  private center: L.LatLng = L.latLng(48.9956303, 9.2903244);
  private drone: L.Marker = L.marker(this.center, {
    icon: L.icon({
      iconUrl: 'assets/imgs/drone.svg',
      iconSize: [this.iconSize, this.iconSize],
      iconAnchor: [this.iconSize / 2, this.iconSize / 2]
    }),
    clickable: true,
    draggable: false,
    title: 'drone',
    riseOnHover: true
  });

  private socketSubscription: Subscription;
  msgs: any[] = [];
  itens: any = {};
  origin: string;
  feature: string;
  private stream: any;

  freeDraw = new FreeDraw({
    mode: NONE,
    leaveModeAfterCreate: true,
    strokeWidth: 1
  })

  // Para auxiliar o evento de criação do FreeDraw, que para cada zona criada emite 3 eventos iguais.
  freeDrawCount: number = 0

  // Grupo de layers das zonas.
  zones = L.featureGroup()

  // Todos os layers que vão ser carregados no mapa
  layers = L.layerGroup()
    // .addLayer(this.things)
    .addLayer(this.zones)
    .addLayer(this.freeDraw);

  // para dialog de referência da zona
  display: boolean = false
  currentLayer = new L.Layer()

  public options: any = {
    layers: this.layers,
    zoom: 18,
    minZoom: 3,
    maxZoom: 20,
    center: this.center,
    fullscreenControl: true,
    fullscreenControlOptions: {
      position: 'topleft'
    },
    scale: {
      metric: true
    }
  };

  drawOptions = {
    position: 'topleft',
    draw: {
      polyline: false,
      marker: false,
      circlemarker: false,
      polygon: {
        //allowIntersection: false, // Restricts shapes to simple polygons
        allowIntersection: true,
        showArea: true,
        showLength: true,
        /* drawError: {
          color: '#e1e100', // Color the shape will turn when intersects
          message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
        },*/
        icon: new L.DivIcon({
          iconSize: new L.Point(8, 8, false),
          className: 'leaflet-div-icon leaflet-editing-icon'
        }),
        touchIcon: new L.DivIcon({
          iconSize: new L.Point(10, 10, false),
          className: 'leaflet-div-icon leaflet-editing-icon leaflet-touch-icon'
        }),
        shapeOptions: {
          color: 'green',
          stroke: true,
          clickable: true,
          transform: true,
          weight: 1,
          draggable: true
        },
        metric: true
      },
      circle: {
        shapeOptions: {
          color: 'green',
          stroke: true,
          clickable: true,
          transform: true,
          weight: 1,
        },
        showRadius: true,
        metric: true
      },
      rectangle: {
        shapeOptions: {
          color: 'green',
          stroke: true,
          clickable: true,
          transform: true,
          weight: 1,
          draggable: true
        },
        metric: true
      }
    },
    edit: {
      featureGroup: this.zones,
      remove: true,
      transform: true
    }
  };


  onMapReady(map: L.Map) {
    this.map = map;
    this.drone.addTo(this.map);
    let droneCommands = [
      L.easyButton('<span class="fa fa-upload fa-lg" style="margin-top: 0.45rem"></span>', () => {
        this.uploadMission();
      }, 'Upload Mission'),
      L.easyButton('<span class="fa fa-play fa-lg" style="margin-top: 0.45rem"></span>', () => {
        this.startMission();
      }, 'Start Mission')
    ]

    L.easyBar(droneCommands).addTo(this.map)
  }

  onDrawReady() {
    let self = this

    L.easyButton('<span class="fa fa-check fa-lg" style="margin-top: 0.45rem"></span>', () => {
      self.savePolygonPlace()
    }, 'Save').addTo(this.map)

    // Criar nossos custom shapes ao criar uma zona pelo leaflet-draw
    this.map.on('draw:created', (event) => {
      if (event['layerType'] === 'polygon') {
        let polygon = new CustomPolygon(event['layer']['_latlngs'], event['layer']['options'])
        if (polygon.ref != null && polygon.ref != undefined)
          polygon.bindTooltip(polygon.ref, { interactive: true })
        this.iniciar(polygon)
        polygon.dragging.disable()
        self.zones.addLayer(polygon)
        self.zones.removeLayer(event['layer'])
        console.log(polygon.toGeoJSON())
      }
    })

    // Criar nosso custom polygon ao criar uma forma com o FreeDraw
    this.freeDraw.on('markers', event => {
      if (event.eventType === 'create') {
        if (self.freeDrawCount === 2) {
          let drawPoly = new CustomPolygon(event.latLngs, {
            color: 'green',
            stroke: true,
            clickable: true,
            transform: true,
            weight: 1
          })
          drawPoly.bindTooltip(drawPoly.ref, { interactive: true })
          self.zones.addLayer(drawPoly)
          self.freeDrawCount = 0
          self.freeDraw.all().forEach(draw => self.freeDraw.remove(draw));
        } else {
          self.freeDrawCount = self.freeDrawCount + 1
        }
      }
    });
  }
  // Adiciona uma referencia mudara  cor para azul.
  addRefsToZones() {
    this.zones.eachLayer((layer) => {
      layer.off('click', onclick)
      layer['options']['weight'] = 1
      layer['options']['color'] = 'blue'
      layer.on('click', () => {
        this.openDialog(layer)
      })
      this.zones.removeLayer(layer)
      this.zones.addLayer(layer)
    })
  }

  openDialog(area) {
    this.currentLayer = area
    this.display = true
    this.changeDetector.detectChanges()
  }

  saveNewRef(ref: string, layerId: number) {
    console.log(ref);
    console.log(layerId);

    let trueLayer = this.zones.getLayer(layerId) as CustomPolygon
    trueLayer.ref = ref
    this.display = false
    trueLayer.bindTooltip(trueLayer.ref, { interactive: true })
  }

  defNextRef(lastRef: string): string {
    let separator: number
    for (let i = lastRef.length - 1; i >= 0; i--) {
      if (isNaN(parseInt(lastRef[i]))) {
        separator = i + 1
        i = 0
      }
    }
    let ref = lastRef.slice(separator)
    let newRef = parseInt(ref) + 1
    let newRefString = newRef.toString()
    if (ref.length > newRefString.length) {
      newRefString = '0' + newRef
    }
    let startRef = lastRef.slice(0, separator)
    return startRef + newRefString
  }

  savePolygonPlace() {
    this.zones.eachLayer((layer) => {
      layer['options']['weight'] = 1
      layer['options']['color'] = 'green'
      layer.off('click', onclick)
      this.zones.removeLayer(layer)
      this.zones.addLayer(layer)
      this.testaBounds();
    })
  }

  notEmpty(obj): boolean {
    for (var key in obj) {
      if (obj.hasOwnProperty(key))
        return true;
    }
    return false;
  }


  constructor(private msgSvc: MessageService, private http: HttpClient, private socket: SocketService, private weatherSocket: SocketService,
    private changeDetector: ChangeDetectorRef,
    private el: ElementRef,
    private renderer: Renderer2,
  ) { }

  ngOnInit() {
    https://tile.openweathermap.org/map/{layer}/{z}/{x}/{y}.png?appid={api_key}
    this.layers.addLayer(tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; GTP AUTOMATION'
    }));
    this.layers.addLayer(this.weatherLayer);
    this.http.get(environment.processserver + "origin").subscribe(data => {
      this.itens = data;
      //TODO: Generic @Input
      this.origin = 'acba3437-5c57-46ca-aa6f-60d4e67c8f5b';
      this.feature = 'FTDRONE';
      this.conecta();
      this.connectWeather();
    });
  }

  testaBounds() {
    console.log(this.zones);
  }

  iniciar(oPoligono) {
    var bounds: any = Object.values(oPoligono.getBounds())


    /*
        Após o oPoligonoo ser gerado, temos que pega os 2 primeiros latlngs e transformar em points
        oPoligono._latlngs[0][0].lat, oPoligono._latlngs[0][0].lng   isso pega o primeiro latlng do oPoligono
        oPoligono._latlngs[0][1].lat, oPoligono._latlngs[0][1].lng   isso pega o segundo latlng do oPoligono
    */

    var pointLatlng1 = this.map.latLngToLayerPoint(L.latLng([oPoligono._latlngs[0][0].lat, oPoligono._latlngs[0][0].lng]))
    var pointLatlng2 = this.map.latLngToLayerPoint(L.latLng([oPoligono._latlngs[0][1].lat, oPoligono._latlngs[0][1].lng]))

    console.log(pointLatlng1)
    console.log(pointLatlng2)


    this.gerarPolylines(
      pointLatlng1,
      pointLatlng2,
      this.map.latLngToLayerPoint(bounds[0]),
      this.map.latLngToLayerPoint(bounds[1]),
      oPoligono
    )
  }



  gerarPolylines(ponto1, ponto2, bound1, bound2, polygonoGerado) {
    // quem é maior Y ?
    let maiorY = Math.max(bound1.y, bound2.y);
    let menorY = Math.min(bound1.y, bound2.y);

    // quem é o maior X ?
    let maiorX = Math.max(bound1.x, bound2.x);
    let menorX = Math.min(bound1.x, bound2.x);

    // Aumenta o valor Y1 ter um a observacao aqui WAL
    // diminui o valor Y2, ter cuidado aqui caso os valores sejam invertido,
    ponto1.y = ponto1.y - (maiorY - menorY)
    ponto2.y = ponto2.y + (maiorY - menorY)

    // isso é fazer que com que o ponto fique paralelo e 90º ao primeiro
    ponto2.x = ponto1.x + 70

    // gerar negativa fora da area PRECISA DE UM CONDICIONAL AQUI
    // for (let j = 0; j < (maiorX + menorX); j++) {

    //     // distancia entre retas parelelas
    //     ponto1.x = ponto1.x - distanciaEntrePalelalas
    //     ponto2.x = ponto2.x - distanciaEntrePalelalas

    //     // let markerG = L.polyline([this.map.layerPointToLatLng(ponto1), this.map.layerPointToLatLng(ponto2)], {
    //     //     color: 'green',
    //     //     weight: 1
    //     // }).addTo(this.map);
    // }

    // for (let j = 0; j < (maiorX - menorX); j++) {

    // distancia entre retas parelelas
    // ponto1.x = ponto1.x - this.distanciaEntrePalelalas
    // ponto2.x = ponto2.x - this.distanciaEntrePalelalas

    // let markerG = L.polyline([this.map.layerPointToLatLng(ponto1), this.map.layerPointToLatLng(ponto2)], {
    //     color: 'green',
    //     weight: 1
    // }).addTo(this.map);
    // }

    // gerar polylines dentro da area
    for (let i = 0; i < (maiorX - menorX) + (maiorX - menorX) + 4; i++) {

      // distancia entre retas parelelas
      ponto1.x = ponto1.x + this.distanciaEntrePalelalas
      ponto2.x = ponto2.x + this.distanciaEntrePalelalas

      this.markerG = L.polyline([this.map.layerPointToLatLng(ponto1), this.map.layerPointToLatLng(ponto2)], {
        color: 'red',
        weight: 1
      }).addTo(this.map);

      this.polylinesParalelas.push(this.markerG)
    }

    // adiciona retas paralelas horizontalmente..


    // console.log(this.polylinesParalelas)
    this.buscarIntercecao(polygonoGerado, this.polylinesParalelas)

  }



  buscarIntercecao(thePolygon, polylinesParallels) {
    var intersection

    polylinesParallels.map(paralela => {

      intersection = turf.lineIntersect(thePolygon.toGeoJSON(), paralela.toGeoJSON());
      if (intersection.features.length) {
        this.pontosInterceptados.push(intersection)
      }

      // remove retas paralelas do mapa
      this.map.removeLayer(paralela)
    })
    console.log(this.pontosInterceptados);

    this.mostrarNovaPolyline(this.pontosInterceptados)
  }

  // precisa melhorar isso daqui...
  mostrarNovaPolyline(arrayDePontosInterceptados) {

    console.log(arrayDePontosInterceptados)

    // console.log(arrayDePontosInterceptados)

    let latlgsTeste = []

    var desconveteu = L.geoJSON(arrayDePontosInterceptados, {
      onEachFeature: (feature) => {

        latlgsTeste.push([feature.geometry['coordinates'][1], feature.geometry['coordinates'][0]])
        //   // }
      }
    });
    console.log(desconveteu)
    // var desconveteu = L.geoJSON(arrayDePontosInterceptados, {

    //   coordsToLatLng:

    //   // coordsToLatLng: (teste) => {
    //   //   return latlgsTeste.push([teste[1], teste[0]])
    //   // }

    // })
    console.log(latlgsTeste)
    this.mudaArray(latlgsTeste)

  }

  mudaArray(latlgsTeste) {

    // console.log(latlgsTeste)

    let novoArrayPoint = []

    latlgsTeste.forEach(latlag => {
      var point = this.map.latLngToLayerPoint(latlag);
      novoArrayPoint.push(point)
    });

    console.log(novoArrayPoint)

    let variavelAuxiliar

    // for (let i = 1; i < novoArrayPoint.length; i++) {
    //     if (novoArrayPoint[i + 1]) {
    //         if (novoArrayPoint[i].x === novoArrayPoint[i + 1].x && novoArrayPoint[i - 1].y < novoArrayPoint[i].y) {
    //             variavelAuxiliar = novoArrayPoint[i];
    //             novoArrayPoint[i] = novoArrayPoint[i + 1];
    //             novoArrayPoint[i + 1] = variavelAuxiliar
    //         }
    //         //  else if (novoArrayPoint[i - 1].y > novoArrayPoint[i].y) {
    //         //     // variavelAuxiliar = novoArrayPoint[i];
    //         //     // novoArrayPoint[i] = novoArrayPoint[i + 1];
    //         //     // novoArrayPoint[i + 1] = variavelAuxiliar
    //         // }
    //         //  else if (novoArrayPoint[i].x === novoArrayPoint[i+1].x && novoArrayPoint[i+1].y > novoArrayPoint[i].y) {
    //         //     variavelAuxiliar = novoArrayPoint[i];
    //         //     novoArrayPoint[i] = novoArrayPoint[i-1];
    //         //     novoArrayPoint[i-1] = variavelAuxiliar
    //         // }
    //         // } else if(novoArrayPoint[i].x < novoArrayPoint[i-1].x) {
    //         //     variavelAuxiliar = novoArrayPoint[i];
    //         //     novoArrayPoint[i] = novoArrayPoint[i-1];
    //         //     novoArrayPoint[i-1] = variavelAuxiliar
    //         // } else if(novoArrayPoint[i].x > novoArrayPoint[i+1].x) {
    //         //     variavelAuxiliar = novoArrayPoint[i];
    //         //     novoArrayPoint[i] = novoArrayPoint[i+1];
    //         //     novoArrayPoint[i+1] = variavelAuxiliar
    //         // }
    //     }
    // }

    // novoArrayPoint.sort()

    for (let i = 0; i < novoArrayPoint.length; i = i + 4) {
      if (novoArrayPoint[i + 1]) {

        variavelAuxiliar = novoArrayPoint[i];
        novoArrayPoint[i] = novoArrayPoint[i + 1];
        novoArrayPoint[i + 1] = variavelAuxiliar

      }

    }

    let jaFormatado = []
    novoArrayPoint.forEach(latlag => {
      var point = this.map.layerPointToLatLng(latlag);
      jaFormatado.push(point)
    });

    L.polyline(jaFormatado, {
      color: 'blue',
      weight: 1
    }).addTo(this.map)

    this.polylinesParalelas = []
    this.pontosInterceptados = []
  }

  droneLatitude: number;
  droneLongitude: number;
  droneAltitude: number;
  droneRoll: number;
  dronePitch: number;
  droneYaw: number;
  droneSpeedX: number;
  droneSpeedY: number;
  droneSpeedZ: number;
  droneRollSpeed: number;
  dronePitchSpeed: number;
  droneYawSpeed: number;
  droneHeight: number;
  battery: number;
  battUnit: string;
  statusText: string;
  textSeverity: string;

  conecta() {
    this.stream = this.socket.connect(environment.wsprocess + 'origin/' + this.origin);

    this.socketSubscription = this.stream.subscribe(
      (message: RawData) => {
        this.msgs.push(message);
        if (this.msgs.length > 1000)
          this.msgs.shift();
        let payload = JSON.parse(message.payload);
        if (message.type === RawDataType.LOCATION) {
          let lat = payload['latitude'];
          let lng = payload['longitude'];
          this.droneLatitude = lat;
          if (payload['ground-speed-x'])
            this.droneSpeedX = payload['ground-speed-x'];
          this.droneLongitude = lng;
          if (payload['ground-speed-y'])
            this.droneSpeedY = payload['ground-speed-y'];
          if (payload['altitude'])
            this.droneAltitude = payload['altitude'];
          if (payload['ground-speed-z'])
            this.droneSpeedZ = payload['ground-speed-z'];
          if (payload['relative-altitude'])
            this.droneHeight = payload['relative-altitude'] / 1000;
          this.drone.setLatLng(L.latLng(lat, lng));
        } else if (message.type === RawDataType.SENSOR) {
          this.battery = payload['value'];
          this.battUnit = payload['unit'];
        } else if (message.type === RawDataType.STATUS) {
          if (payload['text'])
            this.statusText = payload['text'];
          if (payload['severity'])
            this.textSeverity = payload['severity'];
          if (payload['roll'])
            this.droneRoll = payload['roll'];
          if (payload['pitch'])
            this.dronePitch = payload['pitch'];
          if (payload['yaw'])
            this.droneYaw = payload['yaw'];
          if (payload['roll-speed'])
            this.droneRollSpeed = payload['roll-speed'];
          if (payload['pitch-speed'])
            this.dronePitchSpeed = payload['pitch-speed'];
          if (payload['yaw-speed'])
            this.droneYawSpeed = payload['yaw-speed'];
        }
      }
    );
  }

  uploadMission() {
    let command = { command: 'uploadMission' };
    console.log("Uploading Mission");
    this.sendOriginCommand(command);
  }

  startMission() {
    let command = { command: 'prepareForLaunch' };
    console.log("Starting Mission");
    this.sendOriginCommand(command);
  }

  sendOriginCommand(command: any) {
    this.http.post(environment.processserver + "origin/" + this.origin + '/executeon/' + this.feature, command, { responseType: 'json' })
      .subscribe(msg => {
        if (msg === 'Ok') {
          this.msgSvc.add({ severity: 'success', summary: 'FIELD SAVED', detail: 'Uploading Mission' });
        } else {
          this.msgSvc.add({ severity: 'error', summary: 'FIELD NOT SAVED', detail: 'Check console for more details' });
          console.log(msg);
        }
      });
  }

  notamService: string = 'https://v4p4sz5ijk.execute-api.us-east-1.amazonaws.com/anbdata/states/notams/notams-realtime-list';
  notams: any[];
  displayDialog: boolean = false;
  weatherOrigin: string = '21530fda-7ab9-11e9-a343-049226d943d2';
  weatherStream: Observable<RawData>;
  weatherSocketSubscription: Subscription;
  temperature: number;
  temperatureUnit: string;
  humidity: number;
  humidityUnit: string;
  pressure: number;
  pressureUnit: string;
  windSpeed: number;
  windSpeedUnit: string;
  windDir: number;
  winddirUnit: string;
  precipitation: number;
  precipitationUnit: string;
  clbattery: boolean = false;
  clblades: boolean = false;
  clmotors: boolean = false;
  cllanding: boolean = false;
  clhomepos: boolean = false;
  clsurroundings: boolean = false;
  clLegal: boolean = false;
  dialogChecklist: boolean = false;

  checkNOTAMs() {
    let headers = new HttpHeaders().set('Content-Type', 'application/json');
    headers = headers.append('X-Api-Key', '139021c0-7aab-11e9-aaa4-65e41445ef7d');
    // headers = headers.append('Referer', 'https://www.icao.int/safety/iStars/Pages/API-Data-Service.aspx');
    let params = new HttpParams().set('api_key', '139021c0-7aab-11e9-aaa4-65e41445ef7d');
    params = params.append('format', 'json');
    params = params.append('criticality', '');
    params = params.append('locations', 'EDDS');
    this.http.get(this.notamService, { headers: headers, params: params })
      .subscribe((msg: any[]) => {
        console.log(msg);
        this.notams = Array.of([]);
        for (let [key, value] of Object.entries(msg)) {
          this.notams.push(value);
        }
        this.displayDialog = true;
      });
  }

  notamColumns = [
    {
      header: 'ID',
      field: 'id'
    },
    {
      header: 'Message',
      field: 'message'
    },
    {
      header: 'Condition',
      field: 'Condition'
    },
    {
      header: 'State Code',
      field: 'StateCode'
    },
    {
      header: 'State Name',
      field: 'StateName'
    }
  ]

  weatherService: string = 'http://api.openweathermap.org/data/2.5/forecast';
  currentWeatherLayer: string = 'precipitation_new';

  weatherForecast() {
    let headers = new HttpHeaders().set('Content-Type', 'application/json');
    let params = new HttpParams();

    params = params.set("id", "2916936");
    params = params.set("appid", "fd8c0c5e3ba6d4e1e1393953265cda89");

    this.http.get(this.weatherService, { params: params, headers: headers })
      .subscribe(msg => {
        console.log(msg);
        for (let [key, value] of Object.entries(msg)) {
          console.log("FORECAST: " + key + ':' + value);
        }
      });
  }

  weatherLayer: L.Layer = tileLayer('https://tile.openweathermap.org/map/' + this.currentWeatherLayer + '/{z}/{x}/{y}.png?appid=fd8c0c5e3ba6d4e1e1393953265cda89');

  weatherLayerChanged(event: MouseEvent) {
    this.layers.removeLayer(this.weatherLayer);
    this.weatherLayer = tileLayer('https://tile.openweathermap.org/map/' + this.currentWeatherLayer + '/{z}/{x}/{y}.png?appid=fd8c0c5e3ba6d4e1e1393953265cda89');
    this.layers.addLayer(this.weatherLayer);
  }

  connectWeather() {
    this.weatherStream = this.weatherSocket.connect(environment.wsprocess + 'origin/' + this.weatherOrigin);

    this.socketSubscription = this.weatherStream.subscribe(
      (message: RawData) => {
        console.log('received message from server: ', message);
        let payloads: WeatherSensor[] = JSON.parse(message.payload);
        this.temperature = (payloads[0].value - 32) * 5 / 9;
        this.temperatureUnit = payloads[0].unit = 'C';
        this.humidity = payloads[1].value;
        this.humidityUnit = payloads[1].unit;
        this.pressure = payloads[3].value;
        this.pressureUnit = payloads[3].unit;
        this.windDir = payloads[4].value;
        this.winddirUnit = payloads[4].unit;
        this.windSpeed = payloads[5].value * 1.609344;
        this.windSpeedUnit = payloads[5].unit = 'Km/h';
        this.precipitation = payloads[6].value;
        this.precipitationUnit = payloads[6].unit;
      }
    );
  }

  ngOnDestroy() {
    if (this.socketSubscription != null) {
      this.socketSubscription.unsubscribe();
    }
  }
  checkList() {
    this.dialogChecklist = true;
  }
}