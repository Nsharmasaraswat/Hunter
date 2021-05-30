import axios from 'axios';
import {baseUrl} from "../config/Url";

export class AddressModelService {

    createAddressModel(metaName, locationId) {
        return axios.post(baseUrl+'addressmodel',{
            "name": metaName.toLowerCase()+"_"+locationId,
            "metaname": metaName.toUpperCase()+"_"+locationId,
            "status": "ATIVO",
            "siblings": [],
            "fields": [],
            "classe": null
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllAddressModel() {
        return axios.get(baseUrl+'addressmodel/all',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

}
