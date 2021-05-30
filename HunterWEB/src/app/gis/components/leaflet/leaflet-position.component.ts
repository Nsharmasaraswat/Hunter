import { HttpClient } from "@angular/common/http";
import { ChangeDetectorRef, Component, ElementRef, OnInit, Renderer2 } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import "@babel/polyfill";
import * as L from 'leaflet';
import 'leaflet-draw-drag';
import 'leaflet-easybutton';
import FreeDraw, { CREATE, NONE } from 'leaflet-freedraw';
import 'leaflet-path-drag';
import 'leaflet-path-transform';
import 'leaflet.fullscreen';
import { MessageService } from "primeng/components/common/messageservice";
import { LocationRestService } from '../../../process/services/location-rest.service';
import { GisTools } from "../../tools/GisTools";
import { CustomCircle, CustomPolygon } from './custom-shapes.model';

declare var require: any;

var parse = require('wellknown');



@Component({
  templateUrl: './leaflet-position.component.html'
})
export class LeafletPositionComponent implements OnInit {

  public map: L.Map;

  arrayJson: any = []

  // Grupo de layers das zonas.
  zoneGroup = new L.FeatureGroup();

  private gisTools: GisTools;

  // things = L.layerGroup([
  //   L.marker([-2.00004, -4.00003], {
  //     icon: L.icon({
  //       iconSize: [20, 20],
  //       iconUrl: '../assets/imgs/profile.jpg'
  //     })
  //   }).bindPopup(L.popup().setContent(
  //     'My name is Cláudio!'
  //   )),
  //   L.marker([0, 0], {
  //     icon: L.icon({
  //       iconSize: [20, 20],
  //       iconUrl: '../assets/imgs/target.svg'
  //     })
  //   }).bindPopup(L.popup().setContent(
  //     'CENTER'
  //   )),
  //   L.marker([-1.2002, 3.5002], {
  //     icon: L.icon({
  //       iconSize: [20, 20],
  //       iconUrl: '../assets/imgs/profile.jpg'
  //     })
  //   }).bindPopup(L.popup().setContent(
  //     'My name is Sérgio!'
  //   ))
  // ])

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
    .addLayer(this.freeDraw)

  // para dialog de referência da zona
  display: boolean = false
  currentLayer = new L.Layer()

  public options: any = {
    layers: this.layers,
    zoom: 5,
    minZoom: 3,
    maxZoom: 20,
    center: L.latLng(0, 0),
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
    // if (this.arrayJson) {
    //   this.converterPointParaLatLng(this.arrayJson);
    // }
    this.gisTools = new GisTools(map);
    this.locSvc.loadData().subscribe(data => {
      console.log(data);
      this.converterPointParaLatLng(data)
    })
  }

  onDrawReady(drawControl: L.Control.Draw) {
    let self = this

    // Botões que são criados abaixo dos botoes do leaflet-draw
    let transformButtons = [
      L.easyButton('<span class="fa fa-pencil fa-lg" style="margin-top: 0.45rem"></span>', () => {
        self.freeDraw.mode(CREATE)
        this.renderer.setStyle(this.el.nativeElement, 'cursor', 'crosshair')
      }, 'Desenhar'),
      L.easyButton('<span class="fa fa-undo fa-lg" style="margin-top: 0.45rem"></span>', () => {
        self.enableRotate()
      }, 'Rotacionar'),
      L.easyButton('<span class="fa fa-expand fa-lg" style="margin-top: 0.45rem"></span>', () => {
        self.enableScale()
      }, 'Escalar')
    ]

    let buttonsRefSeq = [
      L.easyButton('<span class="fa fa-font fa-lg" style="margin-top: 0.45rem"></span>', () => {
        self.addRefsToZones()
      }, 'Editar/Adicionar Referências'),
      L.easyButton('<span class="fa fa-clone fa-lg" style="margin-top: 0.45rem"></span>', () => {
        self.addSequence()
      }, 'Criar Sequencia')
    ]

    L.easyBar(transformButtons).addTo(this.map)
    L.easyBar(buttonsRefSeq).addTo(this.map)

    L.easyButton('<span class="fa fa-check fa-lg" style="margin-top: 0.45rem"></span>', () => {
      self.savePolygonPlace()
    }, 'Salvar Alterações').addTo(this.map)

    // Criar nossos custom shapes ao criar uma zona pelo leaflet-draw
    this.map.on('draw:created', (event) => {
      if (event['layerType'] === 'circle') {
        let circle = new CustomCircle(event['layer']['_latlng'], event['layer']['options'])
        circle.bindTooltip(circle.ref, { interactive: true })
        self.zones.addLayer(circle)
        self.zones.removeLayer(event['layer'])
      } else {
        let polygon = new CustomPolygon(event['layer']['_latlngs'], event['layer']['options'])
        polygon.bindTooltip(polygon.ref, { interactive: true })
        polygon.dragging.disable()
        self.zones.addLayer(polygon)
        self.zones.removeLayer(event['layer'])
      }
    })

    // Ao clicar em Editar do leaflet-draw, todos os layers ficam verdes.
    this.map.on('draw:editstart', (event) => {
      let layers = event['target']['_renderer']['_layers']
      for (let key in layers) {
        let layer = layers[key]
        layer['options']['weight'] = 1
        layer['options']['color'] = 'green'
        layer.off('click', onclick)
        this.zones.removeLayer(layer)
        this.zones.addLayer(layer)
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

  constructor(private msgSvc: MessageService,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private changeDetector: ChangeDetectorRef,
    private locSvc: LocationRestService,
    private el: ElementRef,
    private renderer: Renderer2) {
    (L as any).drawLocal.draw.toolbar.buttons.polygon = 'Criar poligono';
    (L as any).drawLocal.draw.toolbar.buttons.circle = 'Criar circulo';
    (L as any).drawLocal.draw.toolbar.buttons.rectangle = 'Criar retangulo';
    (L as any).drawLocal.edit.toolbar.buttons.edit = 'Editar Formatos e Mover';
    (L as any).drawLocal.edit.toolbar.buttons.remove = 'Remover';
  }

  ngOnInit(): void {
    this.locSvc.getLocation('95f31d9f-4cd9-11e9-a948-0266c0e70a8c').subscribe(location => this.layers.addLayer(L.imageOverlay(location.mapfile, [[0, 0], [0, 0]])))
  }

  enableRotate() {
    this.zones.eachLayer((layer) => {
      if (layer instanceof CustomPolygon) {
        layer.off('click', onclick);
        layer['options']['weight'] = 1;
        layer['options']['color'] = 'yellow';
        layer['options']['draggable'] = false;
        this.zones.removeLayer(layer);
        this.zones.addLayer(layer);
        let rotate = true;
        layer.on('click', onclick => {
          layer.transform.setOptions({ rotation: rotate, scaling: false });
          layer.transform.enable();
          rotate = !rotate;
          if (rotate) {
            layer.transform._apply();
            layer.transform.removeHooks();
          }
          this.map.dragging.enable();
        });
      } else {
        layer.off('click', onclick);
        layer['options']['weight'] = 1;
        layer['options']['color'] = 'green';
        layer['options']['draggable'] = false;
        this.zones.removeLayer(layer);
        this.zones.addLayer(layer);
      }
    })
  }

  enableScale() {
    this.zones.eachLayer((layer) => {
      if (layer instanceof CustomPolygon) {
        layer.off('click', onclick)
        layer['options']['weight'] = 1
        layer['options']['color'] = 'orange'
        layer['options']['draggable'] = false
        let polygon = new CustomPolygon(layer['_latlngs'][0], layer['options'])
        polygon.ref = layer['ref']
        polygon.bindTooltip(polygon.ref, { interactive: true })
        let scale = true
        polygon.on('click', onclick => {
          polygon.transform.setOptions({ rotation: false, scaling: scale, uniformScaling: false })
          polygon.transform.enable()
          scale = !scale
          if (scale) {
            polygon.transform._apply()
            polygon.transform.removeHooks()
          }
          this.map.dragging.enable()
        })
        this.zones.addLayer(polygon)
        this.zones.removeLayer(layer)
      }
    });
  }

  // Adiciona uma referencia mudara  cor para azul.
  addRefsToZones() {
    this.zones.eachLayer((layer) => {
      layer.off('click', onclick)
      layer['options']['weight'] = 1
      layer['options']['color'] = 'blue'
      layer.on('click', onclick => {
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

  // adiciona uma sequencia, quando clicado todos os polygon fica vermelho.
  addSequence() {
    this.zones.eachLayer((layer) => {
      layer.off('click', onclick)
      layer['options']['weight'] = 3
      layer['options']['color'] = 'red'
      layer.on('click', onclick => {
        if (!('ref' in layer) || layer['ref'] === undefined) {
          this.addRefsToZones()
          this.openDialog(layer)
          return this.addSequence()
        }
        this.createSequence(layer)
      })
      this.zones.removeLayer(layer)
      this.zones.addLayer(layer)
    })
  }

  createSequence(layer: L.Layer) {
    if (layer instanceof CustomPolygon) {
      let rec = new CustomPolygon(layer['_latlngs'], layer['options'])
      let latlngs = []
      rec['_latlngs'][0].forEach(element => {
        latlngs.push([element['lat'], element['lng'] + 1])
      });
      rec.setLatLngs(latlngs)
      rec.ref = this.defNextRef(layer['ref'])
      rec.bindTooltip(rec.ref, { interactive: true })
      layer.off('click', onclick)
      rec.on('click', onclick => {
        this.createSequence(rec)
      })
      this.zones.addLayer(rec)
    }
    if (layer instanceof CustomCircle) {
      let circle = new CustomCircle(layer['_latlng'], layer['options'])
      circle.setRadius(layer['_mRadius'])
      circle.setLatLng(L.latLng(layer['_latlng']['lat'], layer['_latlng']['lng'] + 1))
      circle.ref = this.defNextRef(layer['ref'])
      circle.bindTooltip(circle.ref, { interactive: true })
      layer.off('click', onclick)
      circle.on('click', onclick => {
        this.createSequence(circle)
      })
      this.zones.addLayer(circle)
    }
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
    })
    this.locSvc.saveZones(this.zones)
  }

  notEmpty(obj): boolean {
    for (var key in obj) {
      if (obj.hasOwnProperty(key))
        return true;
    }
    return false;
  }

  converterPointParaLatLng(arrayJson) {

    let convertJSON = JSON.parse(JSON.stringify(arrayJson));

    let novasLatitudeLongitude = []
    let wktExtraido;
    let novoArray = []
    let poly = {
      createdAt: {},
      id: '',
      name: '',
      shape: '',
      ref: '',
      status: '',
      updatedAt: {},
      latLng: novoArray,
    }

    let areas: Array<any>;

    convertJSON.forEach(element => {
      wktExtraido = parse(element.wkt)
      poly.shape = wktExtraido.type;
      poly.createdAt = new Date(element.createdAt);
      poly.id = element.id;
      poly.status = element.status;
      poly.name = element.name;
      poly.updatedAt = new Date(element.updatedAt);
      poly.ref = element.metaname;

      wktExtraido.coordinates.forEach(element => {

        element.forEach(element => {
          novasLatitudeLongitude.push(this.map.layerPointToLatLng(L.point(element[0], element[1])))
        });
      });
      novasLatitudeLongitude.forEach(ltg => {
        novoArray.push([ltg.lat, ltg.lng])
      });
      novasLatitudeLongitude = [];
      poly.latLng = novoArray;
      novoArray = []

      areas = [poly]
      console.log(areas);

      areas.forEach((area) => {

        let zone;
        let options = { stroke: true, weight: 1, color: 'green', transform: true }
        zone = new CustomPolygon(area.latLng, options)
        zone.ref = area.ref
        zone.createdAt = area.createdAt
        zone.id = area.id;
        zone.status = area.status;
        zone.name = area.name;
        zone.updatedAt = area.updatedAt;
        zone.bindTooltip(`
          <div>
            <strong>createdAt: </strong>${zone.createdAt.toLocaleDateString('pt-Br')}  ${zone.createdAt.toLocaleTimeString()}
          <br>
            <strong>id: </strong>${zone.id}
          <br>
            <strong>metaname: </strong>${zone.ref}
          <br>
            <strong>status: </strong>${zone.status}
          <br>
            <strong>name: </strong>${zone.name}
          <br>
            <strong>updatedAt: </strong> ${zone.updatedAt.toLocaleDateString('pt-Br')}  ${zone.updatedAt.toLocaleTimeString()}

          </div>
        `, { interactive: true })

        this.zones.addLayer(zone)
      })

    });
    console.log(this.zones)
  }

}
