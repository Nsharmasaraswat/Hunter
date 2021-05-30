import axios from 'axios';
import {baseUrl} from "../config/Url";

export class UsersService {

    getSupplierType() {
        return axios.get(baseUrl+'v1/user-type',{
            params: {
                role: 4
            },
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getDashboardData() {
        return axios.get(baseUrl+'v1/dashboard',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res.data);
    }

    changePassword(password,id) {
        return axios.patch(baseUrl+'v1/user/'+id,{
            password
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAdminType() {
        return axios.get(baseUrl+'v1/user-type',{
            params: {
                role: 8
            },
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getSecurityType() {
        return axios.get(baseUrl+'v1/user-type',{
            params: {
                role: 5
            },
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getDriverType() {
        return axios.get(baseUrl+'v1/user-type',{
            params: {
                role: 6
            },
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    editUser(id,editData) {
        return axios.patch(baseUrl+'v1/user/'+id,editData,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllUser(id) {
        return axios.get(baseUrl+'v1/user?userType='+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllAdmins(id,dock) {
        return axios.get(baseUrl+'v1/user?userType='+id+'&dock='+dock,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllSecurityGuards(id,gate) {
        return axios.get(baseUrl+'v1/user?userType='+id+'&gate='+gate,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllDrivers(id) {
        return axios.get(baseUrl+'v1/user?parent='+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createUser(body) {
        return axios.post(baseUrl+'v1/user',body,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteUser(id) {
        return axios.delete(baseUrl+'v1/user/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
