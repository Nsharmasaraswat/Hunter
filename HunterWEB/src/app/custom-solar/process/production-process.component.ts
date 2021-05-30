import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { TokenService } from "../../security/services/token.service";
import { HunterDocument, HunterDocumentItem } from "../../shared/model/HunterDocument";
import { HunterThing } from "../../shared/model/HunterThing";
import { SocketService } from "../../shared/services/socket.service";

@Component({
    templateUrl: './production-process.component.html'
})
export class ProductionProcessComponent implements OnInit {
    navigationSubscription: Subscription;
    socketSubscription: Subscription;
    routeSubscription: Subscription;
    stream: Observable<any>;
    fulldoc: HunterDocument;
    lstDi: any[] = [];
    productItem: HunterDocumentItem;
    canSuccess: boolean = true;
    palletstk: any = {};
    eucatexstk: any = {};
    inputbuffer: any = {};
    palletcnt: number;
    boxcnt: number;
    lineName: string;
    palletCount: number;
    isFinished: boolean;
    prdBoxCount: number;

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(data => {
            this.lstDi = [];
            this.navigationSubscription = this.http.get(environment.processserver + 'document/' + data.document)
                .subscribe((msg: HunterDocument) => {
                    console.log(msg);
                    this.fulldoc = new HunterDocument(msg);
                    this.lineName = this.fulldoc.fields.find(df => df.field.metaname === 'LINHA_PROD').value;
                    this.palletCount = this.fulldoc.siblings.filter(ds => ds.model.metaname === 'ORDCRIACAO').length;
                    this.productItem = msg.items.find(di => di.properties['PRODUCAO'] === 'PRODUCAO');
                    this.lstDi.push(...msg.items.filter(di => di.properties['PRODUCAO'] == 'CONSUMO'));
                    this.fulldoc.things.forEach(dt => {
                        let prd = this.lstDi.find(di => di.product.id === dt.thing.product.id);

                        if (prd)
                            prd.things.push(dt.thing)
                    });
                    let qty = this.fulldoc.siblings
                        .map(ds => ds.things)
                        .reduce((arr, curr) => arr.concat(curr), [])
                        .map(dt => dt.thing.siblings)
                        .reduce((arr, curr) => arr.concat(curr), [])
                        .map(t => t.properties)
                        .reduce((arr, curr) => arr.concat(curr), [])
                        .filter(pr => pr.field.metaname === 'QUANTITY')
                        .map(pr => +pr.value)
                        .reduce((acc, curr) => acc + curr, 0);
                    this.productItem.qty += qty;
                    this.lstDi.sort((a, b) => {
                        if (a.product.sku < b.product.sku) return -1;
                        if (a.product.sku > b.product.sku) return 1;
                        return 0;
                    });
                    this.stream = this.socket.connect(environment.wsprocess + 'process/' + data.process + '/' + data.document);
                    this.socketSubscription = this.stream.subscribe(
                        (msg: any) => {
                            console.log('Socket Message', msg);
                            if (msg.payload !== undefined && msg.payload !== '') {
                                let payload = JSON.parse(msg.payload);
                                let message = payload['message'];
                                if (message) {
                                    let status = payload['sensor-value'];
                                    let sId = payload['sensor-id'];
                                    let type = payload['type'];

                                    if (type === 'STATUS') {
                                        switch (sId) {
                                            case 4://Pallet PET
                                            case 5://Pallet Latas
                                                this.palletstk['Direita'] = message;
                                                break;
                                            case 14://Pallet Latas
                                                this.palletstk['Esquerda'] = message;
                                                break;
                                            case 3://Garrafeira Vazia
                                            case 8://Lata Vazia
                                                this.inputbuffer['Direita'] = message;
                                                break;
                                            case 9://Eucatex Lata
                                            case 10://Eucatex PET
                                                this.eucatexstk['Direita'] = message;
                                                break;
                                            case 6://Eucatex PET
                                                this.eucatexstk['Esquerda'] = message;
                                                break;
                                            default:
                                                console.log(msg);
                                        }
                                    } else if (type === 'SENSOR') {
                                        let cnt = payload['count'];
                                        switch (sId) {
                                            case 2://Caixas LATA
                                            case 13://Caixas PET
                                                this.boxcnt = cnt;
                                                //this.productItem.qty++;
                                                break;
                                            case 11://Pallet LATA
                                            case 7://Pallet PET
                                                this.palletcnt = cnt;
                                                break;
                                            default:
                                                console.log(msg);
                                        }
                                        this.prdBoxCount = Math.max(this.boxcnt - this.palletCount * this.productItem.qty, 0);
                                    }
                                    console.log('Sensor ' + sId + ' with value ' + status + ' gives message ' + message);
                                }
                            } else if (msg.hasOwnProperty('command') && msg.hasOwnProperty('data')) {
                                switch (msg.command) {
                                    case 'NOTIFICATION':
                                        this.msgSvc.add({ severity: 'info', summary: msg.command, detail: msg.data });
                                        if (this.isFinished && msg.data.indexOf(" Criado com Sucesso!") > -1)
                                            this.router.navigate(['home', 'process', 'viewTasks', 'WMSFINISHPROD']);
                                        else if (this.isFinished && msg.data.indexOf("Não foi possível registrar a produção") > -1)
                                            this.isFinished = false;
                                        break;
                                    case 'PACK_COUNT':
                                        this.prdBoxCount = +msg.data;
                                        break;
                                    case 'PALLET_COUNT':
                                        console.log('PALLET COUNT', msg);
                                        break;
                                }
                            } else {
                                let thing = new HunterThing(msg);
                                let product = this.productItem.product.id === thing.product_id ? this.productItem.product : this.lstDi.find(di => di.product.id === msg.product_id).product;
                                let itemName = product.sku + ' - ' + product.name;

                                if (msg.product_id === this.productItem.product.id) {
                                    let qty = thing.properties.filter(pr => pr.field.metaname === 'QUANTITY').map(pr => +pr.value).reduce((acc, curr) => acc + curr);
                                    this.productItem.qty += qty;
                                    this.palletCount++;
                                    this.msgSvc.add({ severity: 'success', summary: 'Novo Pallet', detail: itemName });
                                } else {
                                    let prd = this.lstDi.find(di => di.product.id === msg.product_id);
                                    let th = this.fulldoc.things.map(dt => dt.thing).find(tn => tn.id == thing.id);

                                    if (!th) {
                                        prd.things.push(thing);
                                        this.msgSvc.add({ severity: 'success', summary: 'Novo Item', detail: itemName });
                                    } else {
                                        this.msgSvc.add({ severity: 'info', summary: 'Item já adicionado', detail: itemName });
                                    }
                                }
                            }
                        }
                    );
                    this.socket.send({ "command": "GET_PACK_COUNT", "data": "" });
                    this.socket.send({ "command": "GET_PALLET_COUNT", "data": "" });
                }, error => {
                    console.log(error);
                }, () => {
                    console.log('Connected?');
                });
        });
    }

    resetCounters(): void {
        console.log('Resetting Counters');
        this.socket.send({ "command": "RESET_COUNTERS", "data": "" });
    }

    reloadDocument(): void {
        console.log('Restarting OP');
        this.socket.send({ "command": "RELOAD_DOCUMENT", "data": "" });
    }

    lastPallet(): void {
        console.log('Last Pallet');
        this.socket.send({ "command": "LAST_PALLET", "data": "" });
    }

    recordProduction(): void {
        console.log('Record Production');
        this.socket.send({ "command": "RECORD_PRODUCTION", "data": +this.prdBoxCount });
        this.isFinished = true;
    }

    reload() {
        this.lstDi = [];

        this.ngOnDestroy();
        this.ngOnInit();
    }

    ngOnDestroy() {
        this.unsubscribeObservers();
    }

    unsubscribeObservers(): void {
        if (this.socketSubscription != null)
            this.socketSubscription.unsubscribe();
        if (this.navigationSubscription != null)
            this.navigationSubscription.unsubscribe();
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
        if (this.socket != null)
            this.socket.disconnect();
    }
}