import axios from 'axios';
import {baseUrl} from "../config/Url";

export class UserPermissionService {

    getAlPermissions() {
        return axios.get(baseUrl+'v1/all-permissions',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllUserPermissions(id) {
        return axios.get(baseUrl+'v1/user-permission?userType='+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    addUserPermissions(id,paths) {
        return axios.patch(baseUrl+'v1/user-permission/'+id,{
            paths
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
