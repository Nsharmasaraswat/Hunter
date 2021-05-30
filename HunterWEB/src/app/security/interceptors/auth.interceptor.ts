import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import 'rxjs/add/operator/do';
import { Observable } from 'rxjs/Rx';
import { TokenService } from '../services/token.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private tokenSvc: TokenService, private router: Router, private msgSvc: MessageService) {

    }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (req.url.indexOf("/auth") == -1) {
            const authHeader = "Bearer " + this.tokenSvc.getToken();
            const authReq = req.clone({ setHeaders: { Authorization: authHeader } });
            console.log('Token', this.tokenSvc.getToken());
            return next.handle(authReq).do(event => {

            }, error => {
                if (error.status == 401 || error.status == 0) {
                    this.tokenSvc.logout();
                    this.msgSvc.add({ severity: "error", summary: "Erro de Comunicação", detail: "Sua conexão com o servidor foi reiniciada. Retornando a tela de login..." })
                    setTimeout(() => this.router.navigate(["/security/login"]), 500);
                } else {
                    console.log(error);
                }
            });
        } else {
            return next.handle(req);
        }
    }
}
