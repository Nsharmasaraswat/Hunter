import axios from 'axios';
import {baseUrl} from "../config/Url";

export class WarehouseService {

    editWarehouse(id,editData) {
        return axios.patch(baseUrl+'v1/address/'+id,editData,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllWarehouses(location) {
        return axios.get(baseUrl+'v1/address',{
            params: {
                location: location,
                addressType: 1,
            },
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createWarehouse(name,location_id, wkt) {
        return axios.post(baseUrl+'v1/address',{
            name,
            location: location_id,
            "wkt": wkt,
            addressType: 1
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteWarehouse(id) {
        return axios.delete(baseUrl+'v1/address/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

  getWareHouseDetails(id) {
    return axios.get(baseUrl+'v1/address/'+id,{
      headers: {
        'Authorization': localStorage.getItem('daym-tok')
      }
    }).then(res => res);
  }
}
