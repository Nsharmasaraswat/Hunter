import axios from 'axios';
import {baseUrl} from "../config/Url";

export class UnitService {

    getAllUnits() {
        return axios.get(baseUrl+'unit/all',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createUnit(tagId,type) {
        return axios.post(baseUrl+'unit',{
            type,tagId
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteUnit(id) {
        return axios.delete(baseUrl+'unit/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
