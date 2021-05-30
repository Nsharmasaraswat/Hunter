// import { HttpClient } from "@angular/common/http";
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
// import { CustomCircle, CustomPolygon } from './custom-shapes.model';

import * as turf from '@turf/turf'
// declare var turf: any;

import { Circle, Polygon, Polyline, imageOverlay, LayerGroup } from 'leaflet';

class CustomCircle extends Circle {
  ref: string
  shape: string = 'Circle'
}

class CustomPolygon extends Polygon {
  ref: string
  shape: string = 'Polygon'
}

class CustomPolyline extends Polyline {
  ref: string
  shape: string = 'Polyline'
}

export { CustomCircle, CustomPolygon };



@Component({
  templateUrl: './leaflet-gis.component.html'
})

export class LeafletGisComponent implements OnInit {

public polylinesParalelas = [];
public pontosInterceptados = [];
public markerG;
public distanciaEntrePalelalas = 30;

polygonTeste = [
  [-0.5191062808214728, -15.998943198574915],
  [9.147175274555611, -8.39765847348829],
  [7.1039663509573385, 7.463981906605755],
  [-3.985245287233739, 5.00345043490142],
  [-9.824678770759578, -4.26748064598457],
  [-1.3533879568378422, -6.420445683725878]
];

poly = {
  shape: 'Polyline',
  ref: 'D30',
  // latLng: [[4.915832801313164, 10.239257812500002], [0.9228116626857066, 6.987304687500001],
  // [-5.178482088522876, 10.678710937500002], [0.7909904981540058, 13.403320312500002]],
  latLng: [this.polygonTeste]
};

warehouse = imageOverlay(
  // '../assets/imgs/videoio_overview.svg',
  'http://www.anteprojectos.com.pt/wp-content/uploads/2015/03/Planta-de-Projeto-Model.jpg',
  [[-20.00008, 20.00008], [20.00008, -20.00008]]
);

  public map: L.Map;

freeDraw = new FreeDraw({
  mode: NONE,
  leaveModeAfterCreate: true,
  strokeWidth: 1
});

// Para auxiliar o evento de criação do FreeDraw, que para cada zona criada emite 3 eventos iguais.
freeDrawCount: number = 0;

// Grupo de layers das zonas.
zones = L.featureGroup();

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
    } else if (event['layerType'] === 'polygon') {
      let polygon = new CustomPolygon(event['layer']['_latlngs'], event['layer']['options'])
      polygon.bindTooltip(polygon.ref, { interactive: true })
      this.iniciar(polygon)
      let testeee = polygon.toGeoJSON();
      polygon.dragging.disable()
      self.zones.addLayer(polygon)
      self.zones.removeLayer(event['layer'])
      console.log(testeee)
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

enableRotate() {
  this.zones.eachLayer((layer) => {
    if (layer instanceof CustomPolygon) {
      layer.off('click', onclick)
      layer['options']['weight'] = 1
      layer['options']['color'] = 'yellow'
      layer['options']['draggable'] = false
      this.zones.removeLayer(layer)
      this.zones.addLayer(layer)
      let rotate = true
      layer.on('click', onclick => {
        layer.transform.setOptions({ rotation: rotate, scaling: false })
        layer.transform.enable()
        rotate = !rotate
        if (rotate) {
          layer.transform._apply()
          layer.transform.removeHooks()
        }
        this.map.dragging.enable()
      })
    } else {
      layer.off('click', onclick)
      layer['options']['weight'] = 1
      layer['options']['color'] = 'green'
      layer['options']['draggable'] = false
      this.zones.removeLayer(layer)
      this.zones.addLayer(layer)
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
  })
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
    this.testaBounds();
    console.log('Save');
    
  })
}

notEmpty(obj): boolean {
  for (var key in obj) {
    if (obj.hasOwnProperty(key))
      return true;
  }
  return false;
}


constructor(
  private changeDetector: ChangeDetectorRef,
  private el: ElementRef,
  private renderer: Renderer2,
) { }

ngOnInit() {
  this.layers.addLayer(this.warehouse)

  let areas: Array<any> = [this.poly]
  let zones = new LayerGroup()
  // areas.forEach((area) => {
  //   let zone
  //   let options = { stroke: true, weight: 2, color: 'green', transform: true }
  //   if (area.shape === 'Circle') {
  //     options['radius'] = area.radius
  //     zone = new CustomCircle(area.latLng, options)
  //     zone.ref = area.ref
  //     zone.bindTooltip(zone.ref, { interactive: true })
  //   } else {
  //     zone = new CustomPolyline(area.latLng, options)
  //     zone.ref = area.ref
  //     zone.bindTooltip(zone.ref, { interactive: true })
  //   }
  //   zones.addLayer(zone)
  //   this.layers.addLayer(zone)
  // }

  // );


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


  // console.log(bound1)
  // console.log(bound2)

  let pontoHorizontal1 = ponto1
  let pontoHorizontal2 = ponto2


  // quem é maior Y ?
  let maiorY = bound1.y > bound2.y ? bound1.y : bound2.y
  let menorY = bound1.y < bound2.y ? bound1.y : bound2.y

  // quem é o maior X ?
  let maiorX = bound1.x > bound2.x ? bound1.x : bound2.x
  let menorX = bound1.x < bound2.x ? bound1.x : bound2.x


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

  for (let j = 0; j < (maiorX - menorX); j++) {

    // distancia entre retas parelelas
    ponto1.x = ponto1.x - this.distanciaEntrePalelalas
    ponto2.x = ponto2.x - this.distanciaEntrePalelalas

    // let markerG = L.polyline([this.map.layerPointToLatLng(ponto1), this.map.layerPointToLatLng(ponto2)], {
    //     color: 'green',
    //     weight: 1
    // }).addTo(this.map);
  }

  // gerar polylines dentro da area
  for (let i = 0; i < (maiorX - menorX) + (maiorX - menorX) + 4; i++) {

    // distancia entre retas parelelas
    ponto1.x = ponto1.x + this.distanciaEntrePalelalas
    ponto2.x = ponto2.x + this.distanciaEntrePalelalas

    this.markerG = L.polyline([this.map.layerPointToLatLng(ponto1), this.map.layerPointToLatLng(ponto2)], {
      color: 'green',
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
    onEachFeature: (feature) =>  {

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

  var polinhaaa = L.polyline(jaFormatado, {
    color: 'blue',
    weight: 1
  }).addTo(this.map)

  this.polylinesParalelas = []
  this.pontosInterceptados = []
}

}

