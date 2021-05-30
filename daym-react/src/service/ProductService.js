import axios from 'axios';
import {baseUrl} from "../config/Url";

export class ProductService {

    editProduct(id,editData) {
        return axios.patch(baseUrl+'v1/product/'+id,editData,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllProducts(id) {
        return axios.get(baseUrl+'v1/product?productType='+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createProduct(body) {
        return axios.post(baseUrl+'v1/product',body,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteProduct(id) {
        return axios.delete(baseUrl+'v1/product/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
