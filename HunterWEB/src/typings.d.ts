/* Leaftlet Path Transform*/
import * as L from 'leaflet';
import 'geotiff-layer-leaflet/dist/geotiff-layer-leaflet';

declare module 'leaflet' {

  function polygon(latlngs: LatLngExpression[] | LatLngExpression[][], options?: PathTransformPolylineOptions): Polygon;

  interface PathTransformPolylineOptions extends PolylineOptions {
    transform?: boolean;
    draggable?: boolean;
  }

  interface PathTransformOptions {
    handlerOptions?: L.PathOptions;
    boundsOptions?: L.PolylineOptions;
    rotateHandleOptions?: L.PolylineOptions;
    handleLength?: number;
    rotation?: boolean;
    scaling?: boolean;
    uniformScaling?: boolean;
  }

  interface Polygon {
    transform: PathTransform;
    dragging: PathDrag;
  }

  interface PathDrag {
    enable();
    disable();
  }

  interface PathTransform {
    enable(options?: PathTransformOptions);
    setOptions(options: PathTransformOptions);
    _apply();
    removeHooks();
  }

  interface PolylineOptions extends PathOptions {
    smoothFactor?: number;
    noClip?: boolean;
    clickable?: boolean;
    transform?: boolean;
    draggable?: boolean;
  }

}

/* SystemJS module definition */
declare var module: NodeModule;
interface NodeModule {
  id: string;
}



