import axios from 'axios';
import {baseUrl} from "../config/Url";

export class DockService {

    editDock(id,editData) {
        return axios.patch(baseUrl+'v1/address/'+id,editData,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllDocks(parentId) {
        return axios.get(baseUrl+'v1/address',{
            params: {
                parentId: parentId
            },
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createDock(name, startingTime, endingTime, productTypes, location_id,parent_id,coordinates) {
        return axios.post(baseUrl+'v1/address',{
            name,
            startingTime,
            endingTime,
            productTypes,
            location: location_id,
            parentId: parent_id,
            "wkt": coordinates,
            addressType : 3,
            parentType: "warehouse",
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteDock(id) {
        return axios.delete(baseUrl+'v1/address/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
