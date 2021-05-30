import axios from 'axios';
import {baseUrl} from "../config/Url";

export class LoginService {

    requestToken(userName,password,strategy='local') {
        return axios.post(baseUrl+'authentication?$populate=userType', {
            userName,
            password,
            strategy,
            fcmId: localStorage.getItem('ftk')
        }).then(res => res);
    }

    validateToken(accessToken,strategy='jwt') {
        return axios.post(baseUrl+'authentication?$populate=userType', {
            accessToken,
            strategy,
            fcmId: localStorage.getItem('ftk')
        }).then(res => res);
    }

}
