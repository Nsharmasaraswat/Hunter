import axios from 'axios';
import {baseUrl} from "../config/Url";

export class AppointmentService {


    getAllAppointments(query = '') {
        return axios.get(baseUrl+'v1/appointment?$populate[0]=user&$populate[1]=supplier&$populate[2]=entranceGate&$populate[3]=driver&$populate[4]=truck&$populate[5]=exitGate&$populate[6]=userType'+query,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllAppointmentsForCalendar() {
        return axios.get(baseUrl+'v1/order?orderInfo=true',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    uploadFile(file) {
        let formData = new FormData();
        file.map((each,i)=>formData.append('demo'+i,each))
        return axios.post(baseUrl+'v1/upload',formData,{
            headers: {
                'Content-Type': 'multipart/form-data;',
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAppointment(id) {
        return axios.get(baseUrl+'v1/appointment/'+id+'?$populate[0]=user&$populate[1]=supplier&$populate[2]=entranceGate&$populate[3]=driver&$populate[4]=truck&$populate[5]=exitGate&$populate[6]=userType',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    updateStatusToThree(id,receipt) {
        return axios.patch(baseUrl+'v1/appointment/'+id+'?$populate[0]=user&$populate[1]=supplier&$populate[2]=entranceGate&$populate[3]=driver&$populate[4]=truck&$populate[5]=exitGate&$populate[6]=userType',{
            receipt, status: 3
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    updateStatusToFour(id,entranceGate, exitGate) {
        return axios.patch(baseUrl+'v1/appointment/'+id+'?$populate[0]=user&$populate[1]=supplier&$populate[2]=entranceGate&$populate[3]=driver&$populate[4]=truck&$populate[5]=exitGate&$populate[6]=userType',{
            entranceGate, exitGate, status: 4
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    changeStatus(id,status,appointment) {
        return axios.patch(baseUrl+'v1/order/'+id,{
            status,appointment
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    addDriver(id,driver,truck,extra) {
        console.log('Extra', extra);
        return axios.patch(baseUrl+'v1/appointment/'+id+'?$populate[0]=user&$populate[1]=supplier&$populate[2]=entranceGate&$populate[3]=driver&$populate[4]=truck&$populate[5]=exitGate&$populate[6]=userType',{
            driver,
            truck,
            ...extra
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    cancelAppointment(id) {
        return axios.patch(baseUrl+'v1/appointment/'+id+'?$populate[0]=user&$populate[1]=supplier&$populate[2]=entranceGate&$populate[3]=driver&$populate[4]=truck&$populate[5]=exitGate&$populate[6]=userType',{
            status: -1
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    generateTicket(appointment,language) {
        return axios.post(baseUrl+'v1/generate-ticket',{
            appointment,
            language
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    notifyDriver(text,users) {
        return axios.post(baseUrl+'v1/notification',{
            text,users
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getDriverCoordinate(appointment) {
        return axios.get(baseUrl+'v1/driver-session?status=1&appointment='+appointment,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res.data[0]);
    }

    getAllProducts() {
        return axios.get(baseUrl+'v1/product?$populate[0]=productType',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllProductTypes() {
        return axios.get(baseUrl+'v1/product-type',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllDocks() {
        return axios.get(baseUrl+'v1/address?addressType=3&$populate[0]=parentId',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllGates() {
        return axios.get(baseUrl+'v1/address?addressType=2',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllSuppliers(isSupplier , getAllFields) {
        if(isSupplier){
            return axios.get(baseUrl+'v1/user-type?role[$in]=1&role[$in]=2&role[$in]=3',{
                headers: {
                    'Authorization': localStorage.getItem('daym-tok')
                }
            }).then(res => res);
        }else {
            return axios.get(baseUrl+'v1/user?role=4'+(!getAllFields?'&getAllFields=false':''),{
                headers: {
                    'Authorization': localStorage.getItem('daym-tok')
                }
            }).then(res => res);
        }

    }

    getAllDriver(supplier) {
        return axios.get(baseUrl+'v1/user?parent='+supplier,{
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

    createAppointment(supplier,orders, deliveryOn,type, operationType, extraQuery) {
        const delData = type === 1 ? {deliveryOn} : {};
        let _data = {
        ...(JSON.parse(localStorage.getItem('daym-user')).role === 4 ? {'userType': supplier} : {supplier}),orders,operationType, ...extraQuery
        }
        if(type === 1){
            return axios.post(baseUrl+'v1/appointment?$populate[0]=user&$populate[1]=supplier&$populate[2]=userType',{
                ...delData, ..._data
            },{
                headers: {
                    'Authorization': localStorage.getItem('daym-tok')
                }
            }).then(res => res);
        }else {
            return axios.post(baseUrl+'v1/create-weekly-appointment',{
                ...delData, ..._data
            },{
                headers: {
                    'Authorization': localStorage.getItem('daym-tok')
                }
            }).then(res => res);
        }
    }

}
