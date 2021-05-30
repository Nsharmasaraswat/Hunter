import axios from 'axios';
import {baseUrl} from "../config/Url";

export class TruckFieldService {

    getAllFields() {
        return axios.get(baseUrl+'v1/truck-field',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createTruckField(name,type,values,required) {
        let _values = values.map(each=>{
            return {
                label: each,
                value: each.split(' ').join('_').toLowerCase()
            }
        })
        return axios.post(baseUrl+'v1/truck-field',{
            name,
            type,
            'values': _values,
            required
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    editTruckField(id,name,type,values,required) {
        let _values = values.map(each=>{
            return {
                label: each,
                value: each.split(' ').join('_').toLowerCase()
            }
        })
        return axios.patch(baseUrl+'v1/truck-field/'+id,{
            name,
            type,
            'values': _values,
            required
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteTruckField(id) {
        return axios.delete(baseUrl+'v1/truck-field/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
