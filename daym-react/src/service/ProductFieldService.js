import axios from 'axios';
import {baseUrl} from "../config/Url";

export class ProductFieldService {

    getAllFields(id) {
        return axios.get(baseUrl+'v1/product-field?productType='+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createProductField(name,type,values,productType,required) {
        let _values = values.map(each=>{
            return {
                label: each,
                value: each.split(' ').join('_').toLowerCase()
            }
        })
        return axios.post(baseUrl+'v1/product-field',{
            name,
            type,
            'values': _values,
            productType,
            required
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    editProductField(id,name,type,values,productType,required) {
        let _values = values.map(each=>{
            return {
                label: each,
                value: each.split(' ').join('_').toLowerCase()
            }
        })
        return axios.patch(baseUrl+'v1/product-field/'+id,{
            name,
            type,
            'values': _values,
            productType,
            required
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteProductField(id) {
        return axios.delete(baseUrl+'v1/product-field/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
