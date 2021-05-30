import axios from 'axios';
import {baseUrl} from "../config/Url";

export class UserGroupService {

    getAllUserGroup() {
        return axios.get(baseUrl+'v1/user-type?role[$in]=2&role[$in]=3',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createUserGroup(name,description,role) {
        return axios.post(baseUrl+'v1/user-type',{
            name ,
            description,
            role
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    editUserGroup(id,name,description,role) {
        return axios.patch(baseUrl+'v1/user-type/'+id,{
            name ,
            description,
            role
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteUserGroup(id) {
        return axios.delete(baseUrl+'v1/user-type/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
