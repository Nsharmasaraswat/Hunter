import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { TokenService } from "../../security/services/token.service";
import { SocketService } from "../../shared/services/socket.service";

@Component({
    templateUrl: './pickingprocess.component.html'
})
export class PickingProcessComponent implements OnInit {

    socketSubscription: Subscription;
    stream: any;
    process: string;
    canSuccess: boolean = true;
    public lstDi: any[] = [];
    public lstFail: any[] = [];
    public lstErr: any[] = [];
    public lstOk: any[] = [];

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) { }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.process = data.origin;
            this.lstDi = [];
            this.stream = this.socket.connect(environment.wsprocess + 'process/' + data.origin);
            this.socketSubscription = this.stream.subscribe(
                (msg: any) => {
                    console.log(msg);
                    let tmp: any[] = [];
                    let tmpFail: any[] = [];
                    let tmpErr: any[] = [];
                    let tmpOk: any[] = [];
                    for(let itm of this.lstDi) {
                        if(itm.id != msg.id){
                            for(let t of itm.things) {
                                if(msg.cancelProcess) {
                                    tmpFail.push(t);
                                    this.canSuccess=false;
                                } else if (t.errors && t.errors.length>0) {
                                    tmpErr.push(t);
                                } else {
                                    tmpOk.push(t);
                                }
                            }
                            tmp.push(itm);
                        }
                    }
                    for(let t of msg.things) {
                        if(msg.cancelProcess) {
                            tmpFail.push(t);
                            this.canSuccess=false;
                        } else if (t.errors && t.errors.length>0) {
                            tmpErr.push(t);
                        } else {
                            tmpOk.push(t);
                        }
                    }
                    tmp.push(msg);
                    this.lstDi = tmp;
                    this.lstFail = tmpFail;
                    this.lstErr = tmpErr;
                    this.lstOk = tmpOk;
                    console.log(this.lstDi);
                }
            );
        });
    }

    success() {
        this.http.get(environment.processserver + 'process/success/' + this.process, { responseType: 'text' })
            .subscribe((msg: any) => {
                console.log(msg);
                this.socketSubscription.unsubscribe();
                this.msgSvc.add({ severity: 'success', summary: 'Process Finished', detail: 'Process Successfully Finished' });
                this.router.navigate([msg]);
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
            });
    }

    failure() {
        this.http.get(environment.processserver + 'process/failure/' + this.process, { responseType: 'text' })
        .subscribe((msg: any) => {
            console.log(msg);
            this.socketSubscription.unsubscribe();
            this.msgSvc.add({ severity: 'success', summary: 'Process Cancelled', detail: 'Process Successfully Cancelled' });
            this.router.navigate([msg]);
        }, error => {
            console.log(error);
            this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
        });
    }

    ngOnDestroy() {
        this.socketSubscription.unsubscribe()
      }

}