import axios from 'axios';
import {baseUrl} from "../config/Url";

export class HeadQuarterService {

    editHeadQuarter(id,editData) {
        return axios.patch(baseUrl+'v1/location/'+id,editData,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllHeadquaters() {
        return axios.get(baseUrl+'v1/location',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
  getHeadQuarterDetails(id) {
    return axios.get(`${baseUrl}v1/location/${id}`,{
      headers: {
        'Authorization': localStorage.getItem('daym-tok')
      }
    }).then(res => res);
  }

    createHeadQuarter(name,type, location) {
        return axios.post(baseUrl+'v1/location',{
            name,
            type: type,
            "crs": "EPSG4326",
            "wkt": location,
            "rotation": "0",
            "mapFile": "icon.png"
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteHeadQuarter(id) {
        return axios.delete(baseUrl+'v1/location/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
