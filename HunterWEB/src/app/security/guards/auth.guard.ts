import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { HunterUser } from '../../shared/model/HunterUser';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

@Injectable()
export class AuthGuard implements CanActivate {

    constructor(private router: Router, private authSvc: AuthService, private tokenSvc: TokenService) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        if (sessionStorage.getItem('currentUser') || this.authSvc.checkMobile()) {
            // if (state.url == '/home' || state.url == '/security/login' || state.url == '/home/process/action' || state.url == '/home/core/deviceList' || state.url == '/home/core/listDocuments' ||
            //     state.url == '/home/process/featureList' || state.url == '/home/core/groupList' || state.url == '/home/core/location'
            //     || state.url == '/home/process/originList' || state.url == '/home/core/personList'
            //     || state.url == '/home/core/permissionList' || state.url == '/home/core/portList' || state.url == '/home/process/processFilter'
            //     || state.url == '/home/process/process' || state.url == '/home/process/productModel' || state.url == '/home/process/propertyModel'
            //     || state.url == '/home/process/purposeList' || state.url == '/home/core/sourceList' || state.url == '/home/core/leaflet' || state.url == '/home/gis/leafletGis'
            //     || state.url == '/home/process/taskDef' || state.url == '/home/process/workflow') {
            //     // logged in so return true
            //     //rotas permitidas
            return true;
            // } else {
            //     alert('Acesso negado!');
            //     return false; //se a rota selecionada não estiver entre as permitidas, o usuario nao consegue acessar a rota
            // }
        } else if (route.queryParams['t']) {
            this.tokenSvc.setToken(route.queryParams['t']);
            this.authSvc.getUser().catch((err: HttpErrorResponse) => {
                this.unauthorized(state);
                return Observable.empty();
            }).subscribe((data: HunterUser) => {
                this.tokenSvc.setUid(data.id);
                this.tokenSvc.setKiosk('true');
            }, error => {
                console.log(error);
                this.unauthorized(state);
            });
            return true;
        }

        // not logged in so redirect to login page with the return url
        this.unauthorized(state);
        return false;
    }

    unauthorized(state: RouterStateSnapshot) {
        this.tokenSvc.logout();
        this.router.navigate(['/security/login'], { queryParams: { returnUrl: state.url } });
    }
}
