import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenService } from '../../security/services/token.service';
import { ConferenceProduct } from '../../shared/classes/ConferenceProduct';
import { HunterDocument } from '../../shared/model/HunterDocument';
import { NavigationService } from '../../shared/services/navigation.service';
import { SocketService } from '../../shared/services/socket.service';
import RestStatus from '../../shared/utils/restStatus';

interface ProductCount {
    product_id: string;
    quantity: number;
}

@Component({
    selector: 'failed-conference',
    templateUrl: 'failed-conference.component.html',
    styleUrls: ['failed-conference.component.scss'],
    providers: [DatePipe]
})
export class FailedConferenceComponent {
    routeSubscription: Subscription;
    transport: HunterDocument;
    ordconf: HunterDocument;
    ordconfCount: ProductCount[];
    retordconfs: Array<HunterDocument>;
    selRetordconf: HunterDocument;
    selRetordconfCount: ProductCount[];
    prdConf: ConferenceProduct[];
    confType: string;
    tryRows: number;

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute,
        private navSvc: NavigationService, private datePipe: DatePipe) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.http.get(environment.processserver + 'document/parent/' + routeParams.docId, { responseType: 'json' })
                .subscribe((doc: HunterDocument) => {
                    this.transport = doc;
                    this.ordconf = doc.siblings.filter(s => s.id === routeParams.docId).pop();
                    let confTypeField = this.ordconf.fields.find(df => df.field.metaname === 'CONF_TYPE');
                    this.confType = confTypeField === undefined ? 'ENV' : confTypeField.value;
                    this.retordconfs = this.ordconf.siblings.filter(ds => ds.model.metaname === 'RETORDCONF');
                    this.tryRows = this.retordconfs.length;
                    this.ordconfCount = Array.of(...[]);
                    for (let di of this.ordconf.items) {
                        let prdcnt: ProductCount = this.ordconfCount.find(pc => pc.product_id === di.product.id);

                        if (prdcnt === undefined)
                            this.ordconfCount.push({ product_id: di.product.id, quantity: di.qty });
                        else
                            prdcnt.quantity += di.qty;
                    }
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR DOCUMENTO', detail: error });
                });
        });
    }

    selectRetordconf(ev) {
        let sortFunc = (a, b) => {
            if (a.product.sku !== b.product.sku) return a.product.sku.localeCompare(b.product.sku);
            if (a.lot_id !== b.lot_id) return a.lot_id.localeCompare(b.lot_id);
            return 0;
        };
        this.tryRows = 1;
        this.selRetordconf = ev.data;
        if (this.confType === 'EPAPD')
            this.prdConf = ConferenceProduct.getPrdCPAArray(this.selRetordconf)
        else if (this.confType === 'SPA' || this.confType === 'RPAPD')
            this.prdConf = ConferenceProduct.getSPAPrdArray(this.selRetordconf).sort(sortFunc);
        else
            this.prdConf = ConferenceProduct.getPrdArray(this.selRetordconf).sort(sortFunc);
        this.selRetordconfCount = Array.of(...[]);
        for (let prdCnf of this.prdConf) {
            let prdcnt: ProductCount = this.selRetordconfCount.find(pc => pc.product_id === prdCnf.product.id);

            if (prdcnt === undefined) {
                prdcnt = { product_id: prdCnf.product.id, quantity: prdCnf.quantity * prdCnf.volumes };
                this.selRetordconfCount.push(prdcnt);
            } else
                prdcnt.quantity += (prdCnf.quantity * prdCnf.volumes);
        }
        for (let confCount of this.ordconfCount) {
            let userCount = this.selRetordconfCount.find(cnt => cnt.product_id === confCount.product_id);
            if (userCount !== undefined) {
                if (confCount.quantity !== 0 && Math.round((confCount.quantity + Number.EPSILON) * 10000) / 10000 !== Math.round((userCount.quantity + Number.EPSILON) * 10000) / 10000) {
                    console.log(confCount.quantity, userCount.quantity)
                    for (let prdCnf of this.prdConf) {
                        if (prdCnf.product.id === confCount.product_id) prdCnf.wrong = true;
                    }
                }
            }
        }
    }

    sendConf() {
        let succOrdConf = Object.assign({}, this.selRetordconf);
        let params = new HttpParams().set('id', this.ordconf.id);

        succOrdConf.id = null;
        succOrdConf.status = "SUCESSO";
        succOrdConf.code = succOrdConf.code.substring(0, succOrdConf.code.indexOf("-"));
        succOrdConf.items = Array.from([]);
        this.prdConf.forEach((prc: ConferenceProduct) => {
            let di = prc.getDocumentItem();
            succOrdConf.items.push(di);
        });

        this.ordconf.status = "SUCESSO";
        this.ordconf.siblings.push(succOrdConf);
        this.http.put(environment.customserver + 'document/failordconf', this.ordconf, { params: params })
            .subscribe((res: RestStatus) => {
                if (res.result)
                    this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: "Conferencia Salva" });
                this.router.navigate(['home', 'process', 'viewTasks', 'WMSFAILCONF']);
            }, (errmsg: HttpErrorResponse) => {
                console.log(errmsg);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR DOCUMENTO', detail: errmsg.error });
            });
    }
}
