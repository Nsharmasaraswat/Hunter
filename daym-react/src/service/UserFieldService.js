import axios from 'axios';
import {baseUrl} from "../config/Url";

export class UserFieldService {

    getAllFields(id) {
        return axios.get(baseUrl+'v1/user-field?userType='+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createUserField(name,type,values,userType,required) {
        let _values = values.map(each=>{
            return {
                label: each,
                value: each.split(' ').join('_').toLowerCase()
            }
        })
        return axios.post(baseUrl+'v1/user-field',{
            name,
            type,
            'values': _values,
            userType,
            required
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    editUserField(id,name,type,values,userType,required) {
        let _values = values.map(each=>{
            return {
                label: each,
                value: each.split(' ').join('_').toLowerCase()
            }
        })
        return axios.patch(baseUrl+'v1/user-field/'+id,{
            name,
            type,
            'values': _values,
            userType,
            required
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteUserField(id) {
        return axios.delete(baseUrl+'v1/user-field/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
