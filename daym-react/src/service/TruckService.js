import axios from 'axios';
import {baseUrl} from "../config/Url";

export class TruckService {

    editTruck(id,editData) {
        return axios.patch(baseUrl+'v1/truck/'+id,editData,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllTrucks(id) {
        return axios.get(baseUrl+'v1/truck?user='+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllTrucksOvertime() {
        return axios.get(baseUrl+'v1/driver-session?trucksExceedTime=true&$populate=truck&$populate=user&$populate=appointment&status=1',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createTruck(body) {
        return axios.post(baseUrl+'v1/truck',body,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteTruck(id) {
        return axios.delete(baseUrl+'v1/truck/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
