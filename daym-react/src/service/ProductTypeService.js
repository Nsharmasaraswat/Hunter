import axios from 'axios';
import {baseUrl} from "../config/Url";

export class ProductTypeService {

    getAllProductType() {
        return axios.get(baseUrl+'v1/product-type',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createProductType(name,description,unloadingTime) {
        return axios.post(baseUrl+'v1/product-type',{
            name ,
            description,
            unloadingTime
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    editProductType(id,name,description,unloadingTime) {
        return axios.patch(baseUrl+'v1/product-type/'+id,{
            name ,
            description,
            unloadingTime
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteProductType(id) {
        return axios.delete(baseUrl+'v1/product-type/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
