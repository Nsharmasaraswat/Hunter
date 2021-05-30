import { Circle, Polygon } from 'leaflet';

class CustomCircle extends Circle {
  ref: string
  shape: string = 'Circle'
}

class CustomPolygon extends Polygon {
  ref: string
  shape: string = 'Polygon'
}

export { CustomCircle, CustomPolygon };

