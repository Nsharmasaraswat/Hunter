import axios from 'axios';
import {baseUrl} from "../config/Url";

export class GateService {

    editGate(id,editData) {
        return axios.patch(baseUrl+'v1/address/'+id,editData,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllGates(location) {
        return axios.get(baseUrl+'v1/address',{
            params: {
                location: location,
                addressType: 2
            },
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createGate(name,cameraId,location_id,coordinates,gateType) {
        return axios.post(baseUrl+'v1/address',{
            name,
            cameraId,
            gateType,
            location: location_id,
            "wkt": coordinates,
            addressType: 2,
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteGate(id) {
        return axios.delete(baseUrl+'v1/address/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
