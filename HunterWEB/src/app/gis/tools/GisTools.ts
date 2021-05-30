import * as L from 'leaflet';

declare var require: any;

export class GisTools {

    constructor(private map: L.Map) { }

    public haversine(lat1: number, lat2: number, lng1: number, lng2: number) {
        var φ1 = lat1 * Math.PI / 180;
        var φ2 = lat2 * Math.PI / 180;
        var Δφ = (lat2 - lat1) * Math.PI / 180;
        var Δλ = (lng2 - lng1) * Math.PI / 180;

        var a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return this.earthRadius(lat1) * c;
    }

    public getPointAtBearingAndDistance(latitude: number, longitude: number, bearing: number, dist: number): L.LatLng {
        let r_earth = this.earthRadius(latitude);
        let rLatitude = latitude * Math.PI / 180;
        let rLongitude = longitude * Math.PI / 180;
        let rBearing = bearing * Math.PI / 180;

        //https://www.movable-type.co.uk/scripts/latlong.html?from=49.1715000,-121.7493500&to=49.18258,-121.75441
        let newLat: number = Math.asin(Math.sin(rLatitude) * Math.cos(dist / r_earth) + Math.cos(rLatitude) * Math.sin(dist / r_earth) * Math.cos(rBearing)) * (180 / Math.PI);
        let newLng: number = (rLongitude + Math.atan2(Math.sin(rBearing) * Math.sin(dist / r_earth) * Math.cos(rLatitude), Math.cos(dist / r_earth) - Math.sin(rLatitude) * Math.sin(newLat * Math.PI / 180))) * (180 / Math.PI);

        return L.latLng(newLat, newLng);
    }

    public calcDistanceLatLng(p1: L.LatLng, p2: L.LatLng): number {
        let p1Lat = p1.lat;
        let p1Lng = p1.lng;
        let p2Lat = p2.lat;
        let p2Lng = p2.lng;

        return this.calcDistance(p1Lat, p1Lng, p2Lat, p2Lng);
    }

    public calcDistance(p1Lat: number, p1Lng: number, p2Lat: number, p2Lng: number): number {
        let latDistance = (p1Lat - p2Lat) * Math.PI / 180;
        let lngDistance = (p1Lng - p2Lng) * Math.PI / 180;

        let a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(p1Lat * Math.PI / 180) * Math.cos(p2Lat * Math.PI / 180) * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return this.earthRadius(Math.abs(p2Lat - p1Lat)) * c;
    }

    public earthRadius(lat: number): number {
        let EARTH_RADIUS_EQUATOR_M = 6378137;
        let EARTH_RADIUS_POLE_M = 6356752;
        //https://rechneronline.de/earth-radius/
        //latitude B, radius R, radius at equator r1, radius at pole r2
        //R = √ [ (r1² * cos(B))² + (r2² * sin(B))² ] / [ (r1 * cos(B))² + (r2 * sin(B))² ]
        let p1 = Math.pow(EARTH_RADIUS_EQUATOR_M, 2) * Math.cos(lat);
        let p2 = Math.pow(EARTH_RADIUS_POLE_M, 2) * Math.sin(lat);
        let p3 = EARTH_RADIUS_EQUATOR_M * Math.cos(lat);
        let p4 = EARTH_RADIUS_POLE_M * Math.sin(lat);
        let c1 = Math.pow(p1, 2) + Math.pow(p2, 2);
        let c2 = Math.pow(p3, 2) + Math.pow(p4, 2);

        return Math.sqrt(c1 / c2);
    }

    public parseWKT(wkt: string): any {
        var parse = require('wellknown');

        return parse(wkt);
    }

    public toWKT(geoJson: L.GeoJSON): string {
        var wk = require('wellknown');

        return wk.stringify(geoJson);
    }

    public getPolygonCoords(coordsSimple: Array<Array<Array<number>>>): Array<L.LatLngTuple> {
        let coordsPolygon: Array<L.LatLngTuple> = new Array<L.LatLngTuple>();

        for (let i = 0; i < coordsSimple.length; i++) {
            let coord: any[] = coordsSimple[i];
            let latLng: L.Point = L.point(coord[0], -coord[1]);
            let el = this.map.unproject(latLng, this.map.getMaxZoom() - 1);
            coordsPolygon.push([el.lat, el.lng]);
        }
        return coordsPolygon;
    }

    public moveZoneEvent(zone: L.Polygon): string {
        let poly: L.Polygon = zone;
        let latLngs = poly.getLatLngs();
        let first: string = null;
        let newWkt: string = 'POLYGON((';
        // let projection: L.GeoJSON = L.geoJSON();

        for (let value of latLngs) {
            if (value instanceof Array) {
                for (let v of value) {
                    if (v instanceof L.LatLng) {
                        let projected: L.Point = this.map.project([v.lat, v.lng], this.map.getMaxZoom() - 1);
                        let coord = Math.round(projected.x) + ' ' + Math.round(projected.y) * -1;

                        newWkt += coord + ', ';
                        if (first === null)
                            first = coord;
                    }
                };
            }
        }
        newWkt = newWkt.substring(0, newWkt.length - 1);
        newWkt += first + '))';
        return newWkt;
    }

    /// <summary>
    /// Takes an L.latLngBounds object and returns an 8 point L.polygon.
    /// L.rectangle takes an L.latLngBounds object in its constructor but this only creates a polygon with 4 points.
    /// This becomes an issue when you try and do spatial queries in SQL Server or another database because when the 4 point polygon is applied
    /// to the curvature of the earth it loses it's "rectangular-ness".
    /// The 8 point polygon returned from this method will keep it's shape a lot more.
    /// </summary>
    /// <param name="map">L.map object</param>
    /// <returns type="">L.Polygon with 8 points starting in the bottom left and finishing in the center left</returns>
    public createPolygonFromBounds(latLngBounds: L.LatLngBounds, options): L.Polygon {
        var center = latLngBounds.getCenter(), latlngs = [];

        latlngs.push(latLngBounds.getSouthWest());//bottom left
        latlngs.push({ lat: latLngBounds.getSouth(), lng: center.lng });//bottom center
        latlngs.push(latLngBounds.getSouthEast());//bottom right
        latlngs.push({ lat: center.lat, lng: latLngBounds.getEast() });// center right
        latlngs.push(latLngBounds.getNorthEast());//top right
        latlngs.push({ lat: latLngBounds.getNorth(), lng: center.lng });//top center
        latlngs.push(latLngBounds.getNorthWest());//top left
        latlngs.push({ lat: center.lat, lng: latLngBounds.getWest() });//center left

        return L.polygon(latlngs, options);
    }
}