import { Component } from "@angular/core";
import { MessageService } from "primeng/components/common/messageservice";
import { SocketService } from "../../shared/services/socket.service";
import { TokenService } from "../../security/services/token.service";
import { Router, ActivatedRoute } from "@angular/router";
import { environment } from '../../../environments/environment';
import { HttpClient } from "@angular/common/http";

@Component({
    templateUrl: './view-document.component.html'
})

export class ViewDocumentComponent {

    data: any;
    
    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.http.get(environment.processserver + 'document/' + data.id).subscribe((lst: any) => {
                console.log(lst);
                this.data = lst;
            });
        });
    }

    conf() {
        this.http.get(environment.customserver + 'api/testSAP/conferencia/' + this.data.id).subscribe((lst: any[]) => {
            //console.log(lst);
            this.msgSvc.add({ severity: 'info', summary: 'Conferência Certa', detail: 'A conferência está sendo processada.' });
        });
    }

    conf2() {
        this.http.get(environment.customserver + 'api/testSAP/conferencia2/' + this.data.id).subscribe((lst: any[]) => {
            //console.log(lst);
            this.msgSvc.add({ severity: 'info', summary: 'Conferência Errada', detail: 'A conferência está sendo processada.' });
        });
    }

}
