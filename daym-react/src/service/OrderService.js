import axios from 'axios';
import {baseUrl} from "../config/Url";

export class OrderService {

    editOrder(id,appointment,quantity) {
        return axios.patch(baseUrl+'v1/order/'+id+'?$populate[0]=product&$populate[1]=dock',{
            quantity,
            appointment
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }


    createOrder(appointment,product,quantity,dock) {
        return axios.post(baseUrl+'v1/order?$populate[0]=product&$populate[1]=dock',{
            appointment,product,quantity,dock
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteOrder(id) {
        return axios.delete(baseUrl+'v1/order/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
