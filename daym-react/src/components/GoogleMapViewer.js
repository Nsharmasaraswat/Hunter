import React from 'react';
import {Map, Marker, GoogleApiWrapper} from 'google-maps-react';

export function MapContainer({google, coordinate}){

    return (
        <div id='googleMaps' style={{position: 'unset !important'}}>
            <Map
                zoom={10}
                google={google}
                initialCenter={{
                    lat: coordinate[1],
                    lng: coordinate[0]
                }}
                containerStyle={{
                    width: '27vw',
                    height: '40vh',
                    position: 'relative',
                    margin: '15px 0'
                }}
                center={{
                    lat: coordinate[1],
                    lng: coordinate[0]
                }}

            >
                <Marker
                    position={{
                        lat: coordinate[1],
                        lng: coordinate[0]
                    }} />
            </Map>
        </div>
    );
}

export default GoogleApiWrapper(props =>({
    apiKey: ('AIzaSyBBYqXYQ_WN8pz5RSyZwN97basD2xdxfHA'),
  language : props.language
}))(MapContainer);
