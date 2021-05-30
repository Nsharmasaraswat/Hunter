import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import 'rxjs/add/operator/do';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class LogInterceptor implements HttpInterceptor {
    constructor() { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const started = Date.now();
        return next.handle(req).do(event => {
            if (event instanceof HttpResponse) {
                const elapsed = Date.now() - started;
                console.log(`Request for ${req.urlWithParams} took ${elapsed} ms.`);
            }
        });
    }
}
