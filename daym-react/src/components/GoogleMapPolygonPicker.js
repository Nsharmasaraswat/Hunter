import React, {useState} from 'react';
import {Map, Marker, GoogleApiWrapper, Polygon} from 'google-maps-react';
import {stringify} from 'wkt';
import {Button} from "primereact/button";
import {withTranslation} from "react-i18next";

export function MapContainer({google,setCoordinates, coordinate,initialCenter,t}){
//console.log(initialCenter);
    const [ paths , setPaths ]= useState(coordinate === [] ? coordinate : coordinate.map((each)=>({lat: each[0], lng: each[1]})));
    const [state, setState] = useState({
        mapCenter: {
            lat: coordinate.length > 0 ? coordinate[0][0] : initialCenter ? initialCenter[0] || '-5.266008' : '-5.266008',
            lng: coordinate.length > 0 ? coordinate[0][1] : initialCenter ? initialCenter[1] || '-37.048647' : '-37.048647'
        }
    });
    return (
        <div id='googleMaps' style={{position: 'unset !important'}}>
            <Map
                zoom={coordinate.length > 0 ? 16 : 13}
                google={google}
                onClick={(mapProps, map, event) => {
                  setPaths([...paths,{ lat : event.latLng.lat(), lng : event.latLng.lng()}]);
                }}
                containerStyle={{
                    width: '27vw',
                    height: '40vh',
                    position: 'relative',
                    margin: '15px 0'
                }}
                initialCenter={{
                    lat: state.mapCenter.lat,
                    lng: state.mapCenter.lng
                }}
            >
              {
                paths.length === 1 ?  <Marker
                    name={'Click another point to start geo fencing'}
                    title={'Click another point to start geo fencing'}
                    position = {paths[0]}
                  />
                  :
                  <Polygon  paths = {paths}/>

              }
            </Map>
          <Button onClick={() => setPaths([])} label={t('cancel')}  style={{marginRight: '10px'}} />
            <Button icon="pi pi-check" onClick={() => {
                if(paths.length === 0) {
                  //console.log('Please input a point');
                    return ;
                }
                const location = {
                    type : paths.length === 1 ? 'Point' : paths.length === 2 ? 'LineString' : 'Polygon',
                    coordinates : paths.length === 1 ? [paths[0].lat , paths[0].lng] : paths.length === 2 ? paths.map((each) => [each.lat , each.lng]) : [paths.map((each) => [each.lat , each.lng])]
                };
              //console.log(location);
              //console.log(stringify(location));
                setCoordinates(stringify(location));
            }} label={t('save')} />
        </div>
    );
}

export default withTranslation()(GoogleApiWrapper((props) => ({
    apiKey: ('AIzaSyBBYqXYQ_WN8pz5RSyZwN97basD2xdxfHA'),
    language : props.language
}))(MapContainer));
