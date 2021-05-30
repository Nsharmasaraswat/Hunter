import React, {useState} from 'react';
import {Map, Marker, GoogleApiWrapper} from 'google-maps-react';
import PlacesAutocomplete, {
    geocodeByAddress,
    getLatLng,
} from 'react-places-autocomplete';
import {withTranslation} from "react-i18next";

export function MapContainer({google,setCoordinates, coordinate}){

    const [state, setState] = useState({
        address: '',
        showingInfoWindow: false,
        activeMarker: {},
        selectedPlace: {},
        mapCenter: {
            lat: coordinate[0],
            lng: coordinate[1]
        }
    });
  console.log(google);

    const [selectedCenter,setSelectedCenter] = useState([]);

    const handleChange = address => {
        setState({ ...state, address });
    };

    const handleSelect = address => {
        geocodeByAddress(address)
            .then(results => getLatLng(results[0]))
            .then(latLng => {
                // console.log(this.props.t('success'), latLng);
                setCoordinates({
                    type: "Point",
                    coordinates: [latLng.lat, latLng.lng]
                });
                // update center state
                setState({ ...state,address,mapCenter: latLng });
                setSelectedCenter(latLng );
            })
            .catch(error => console.error(this.props.t('error'), error));
    };

    return (
        <div id='googleMaps' style={{position: 'unset !important'}}>
            <PlacesAutocomplete
                value={state.address}
                onChange={handleChange}
                onSelect={handleSelect}
            >
                {({ getInputProps, suggestions, getSuggestionItemProps, loading }) => (
                    <div style={{position: 'relative', width: '100%'}}>
                        <input
                            {...getInputProps({
                                placeholder: 'Search Places',
                                className: 'location-search-input',
                            })}
                            style={{
                                padding: '6px',
                                margin: '6px 0px',
                                width: '100%'
                            }}
                        />
                        <div className="autocomplete-dropdown-container" style={{
                            position: 'absolute',
                            zIndex: 99,
                            maxWidth: '100%'
                        }}>
                            {loading && <div>Loading Results...</div>}
                            {suggestions.map((suggestion,i) => {
                                const className = suggestion.active
                                    ? 'suggestion-item--active'
                                    : 'suggestion-item';
                                // inline style for demonstration purpose
                                const style = suggestion.active
                                    ? { backgroundColor: '#fafafa', cursor: 'pointer' }
                                    : { backgroundColor: '#ffffff', cursor: 'pointer' };
                                return (
                                    <div
                                        key={i}
                                        {...getSuggestionItemProps(suggestion, {
                                            className,
                                            style,
                                        })}
                                    >
                                        <span style={{
                                            padding: '4px'
                                        }}>{suggestion.description}</span>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}
            </PlacesAutocomplete>
            <Map
                zoom={10}
                google={{
                  ...google,
                  language :'es'
                }}
                bootstrapURLKeys={{
                    language: 'ru',
                    region: 'ru',
                }}
                initialCenter={{
                    lat: state.mapCenter.lat,
                    lng: state.mapCenter.lng
                }}
                containerStyle={{
                    width: '27vw',
                    height: '40vh',
                    position: 'relative',
                    margin: '15px 0'
                }}
                center={selectedCenter}
                onClick={(mapProps,map,event)=>{
                    setCoordinates({
                            type: "Point",
                            coordinates: [mapProps.initialCenter.lat, mapProps.initialCenter.lng]
                        });
                    setState({
                        ...state,
                        mapCenter: {
                            lat: event.latLng.lat(),
                            lng: event.latLng.lng()
                        }
                    });
                }}
            >
                <Marker
                    position={{
                        lat: state.mapCenter.lat,
                        lng: state.mapCenter.lng
                    }} />
            </Map>
        </div>
    );
}

export default withTranslation()(GoogleApiWrapper((props) => ({
    apiKey: ('AIzaSyBBYqXYQ_WN8pz5RSyZwN97basD2xdxfHA'),
    language : props.language
}))(MapContainer));
