import { Injectable } from "@angular/core";


@Injectable()
export class TokenService {
    logout() {
        sessionStorage.removeItem('currentUser');
        sessionStorage.removeItem('uid');
        sessionStorage.removeItem('kiosk');
    }

    setToken(token: string) {
        sessionStorage.setItem('currentUser', token);
    }

    setUid(uid: string) {
        sessionStorage.setItem('uid', uid);
    }

    setKiosk(kiosk: string){
        sessionStorage.setItem('kiosk', kiosk);
    }

    getToken() {
        return sessionStorage.getItem('currentUser');
    }

    getUid() {
        return sessionStorage.getItem('uid');
    }
    

    isKiosk(){
        return sessionStorage.getItem('kiosk') === 'true';
    }
}