import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { environment } from '../../../environments/environment';
import { TokenService } from "../../security/services/token.service";
import { SocketService } from "../../shared/services/socket.service";

@Component({
    templateUrl: './checkingprocess.component.html'
})
export class CheckingProcessComponent implements OnInit {

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
    public tmpErr: any[] = [];

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) { }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            //console.log(data);
            this.process = data.origin;
            this.document = data.document;
            this.lstDi = [];
            //console.log(this.document);
            this.http.get(environment.processserver + 'document/' + this.document)
                .subscribe((msg: any) => {
                    console.log(msg);
                    this.fulldoc = msg;
                    this.lstDi = msg.itens;
                    this.tmpErr = msg.things;
                    this.http.get(environment.processserver + 'process/byBase/' + data.origin).subscribe((proc: any) => {
                        // console.log(proc);
                        for (let itm of this.tmpErr) {
                            // itm.sku = itm.product.sku;
                            // itm.unit = itm.unitModel[0].tagId;
                            //console.log(itm.tstatus + " - " + proc.estadoPara);
                            if (itm.tstatus === proc.estadoPara) {
                                this.lstOk.push(itm);
                                for (let prd of this.lstDi) {
                                    if (prd.product.sku === itm.sku) {
                                        for (let thg of prd.things) {
                                            if (itm.unit === thg.unit) {
                                                if (itm.things == null) {
                                                    itm.things = [];
                                                }
                                                prd.things.splice(prd.things.indexOf(thg), 1);
                                                break;
                                            }
                                        }
                                        prd.things.push(itm);
                                        break;
                                    }
                                }
                            } else {
                                this.lstErr.push(itm);
                            }
                        }
                        this.stream = this.socket.connect(environment.wsprocess + 'process/' + this.process);
                        this.socketSubscription = this.stream.subscribe(
                            (msg: any) => {
                                msg.sku = msg.product.sku;
                                msg.unit = msg.unitModel[0].tagId;
                                for (let itm of this.lstDi) {
                                    if (itm.product.sku === msg.product.sku) {
                                        for (let thg of itm.things) {
                                            if (msg.unit === thg.unit) {
                                                itm.things.splice(itm.things.indexOf(thg), 1);
                                                break;
                                            }
                                        }
                                        if ((!msg.cancelProcess) && (!(msg.errors.length > 0))) {
                                            itm.things.push(msg);
                                        }
                                        break;
                                    }
                                }
                                if (msg.cancelProcess) {
                                    this.lstFail.push(msg);
                                } else if (msg.errors && msg.errors.length > 0) {
                                    //this.lstErr.push(msg);
                                } else {
                                    for (let thg of this.lstErr) {
                                        if (thg.unit === msg.unit) {
                                            this.lstErr.splice(this.lstErr.indexOf(thg),1);
                                            break;
                                        }
                                    }
                                    for (let thg of this.lstOk) {
                                        if (thg.unit === msg.unit) {
                                            this.lstOk.splice(this.lstOk.indexOf(thg),1);
                                            break;
                                        }
                                    }
                                    this.lstOk.push(msg);
                                }
                            }
                        );
                    });
                }, error => {
                    console.log(error);
                });
            this.lstDi.slice();
            this.lstErr.slice();
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
