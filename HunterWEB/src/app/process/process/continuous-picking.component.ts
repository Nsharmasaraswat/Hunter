import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { TokenService } from "../../security/services/token.service";
import { SocketService } from "../../shared/services/socket.service";

@Component({
    templateUrl: './continuous-picking.component.html'
})
export class ContinuousPickingComponent implements OnInit {

    socketSubscription: Subscription;
    stream: any;
    process: string;
    document: string;
    fulldoc: any;
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
            this.process = data.process;
            this.document = data.document;
            this.lstDi = [];
            this.http.get(environment.processserver + 'document/' + this.document)
                .subscribe((msg: any) => {
                    console.log(msg);
                    this.fulldoc = msg;
                    for(let itm of msg.items) {
                        console.log(itm);
                        this.lstDi.push(itm);
                    }
                    for(let t of msg.things) {
                        console.log(t);
                        for(let itm of this.lstDi) {
                            if(t.sku === itm.product.sku) {
                                itm.things.push(t.thing);
                                break;
                            }
                        }
                        this.lstOk.push(t);
                    }
                    this.lstDi.slice();
                    this.stream = this.socket.connect(environment.wsprocess + 'process/' + data.process + '/' + data.document);
                    this.socketSubscription = this.stream.subscribe(
                        (msg: any) => {
                            console.log(msg);
                            for(let itm of this.lstDi) {
                                if(itm.product.sku === msg.product.sku) {
                                    console.log(msg);
                                    msg.sku = msg.product.sku;
                                    msg.unit = msg.unitModel[0].tagId;
                                    itm.things.push(msg);
                                    break;
                                }
                            }
                            if(msg.cancelProcess) {
                                this.lstFail.push(msg);
                            } else if (msg.errors && msg.errors.length>0) {
                                this.lstErr.push(msg);
                            } else {
                                this.lstOk.push(msg);
                            }
                        }
                    );
                }, error => {
                    console.log(error);
                });
        });
    }

    // success() {
    //     this.http.get(environment.processserver + 'process/success/' + this.process, { responseType: 'text' })
    //         .subscribe((msg: any) => {
    //             console.log(msg);
    //             this.socketSubscription.unsubscribe();
    //             this.msgSvc.add({ severity: 'success', summary: 'Process Finished', detail: 'Process Successfully Finished' });
    //             this.router.navigate([msg]);
    //         }, error => {
    //             console.log(error);
    //             this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
    //         });
    // }

    // failure() {
    //     this.http.get(environment.processserver + 'process/failure/' + this.process, { responseType: 'text' })
    //         .subscribe((msg: any) => {
    //             console.log(msg);
    //             this.socketSubscription.unsubscribe();
    //             this.msgSvc.add({ severity: 'success', summary: 'Process Cancelled', detail: 'Process Successfully Cancelled' });
    //             this.router.navigate([msg]);
    //         }, error => {
    //             console.log(error);
    //             this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
    //         });
    // }

    reload() {
        this.lstDi = [];
        this.lstFail = [];
        this.lstErr = [];
        this.lstOk = [];

        this.ngOnDestroy();
        this.ngOnInit();
    }

    ngOnDestroy() {
        this.socketSubscription.unsubscribe();
    }

}