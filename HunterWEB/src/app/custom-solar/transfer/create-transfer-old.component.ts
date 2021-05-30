import { HttpClient } from "@angular/common/http";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterDocument, HunterDocumentItem } from "../../shared/model/HunterDocument";
import { HunterProduct } from "../../shared/model/HunterProduct";
import { SocketService } from '../../shared/services/socket.service';

class ProductionOrderCompilation {
    constructor(public document: HunterDocument, public code: string, public consumption: string[], public production: string[], public line: string) { }
}

class ProductionOrder {
    public edtQty: string;

    constructor(public sku: string, public product: string, public qty: number, public measureUnit: string, public prd: HunterProduct) {
        this.edtQty = qty + '';
    }
}

@Component({
    selector: 'create-transfer-old',
    templateUrl: 'create-transfer-old.component.html',
    styleUrls: ['create-transfer-old.component.scss']
})

export class CreateTransferOldComponent implements OnInit, OnDestroy {
    private routeSubscription: Subscription;

    planProds: ProductionOrderCompilation[];
    selPlanProds: HunterDocument[];
    documentItems: HunterDocumentItem[] = [];
    ordProds: ProductionOrder[];
    colsPlanProd: ReportColumn[] = [
        {
            field: 'code',
            header: 'CÓDIGO',
            type: '',
            nullString: '',
            width: '10%'
        },
        {
            field: 'consumption',
            header: 'CONSUMO',
            type: '',
            nullString: '',
            width: '10%'
        },
        {
            field: 'production',
            header: 'PRODUÇÃO',
            type: '',
            nullString: '',
            width: '10%'
        },
        {
            field: 'line',
            header: 'LINHA',
            type: '',
            nullString: '',
            width: '10%'
        }
    ];

    colsOrdProd: ReportColumn[] = [
        {
            field: 'sku',
            header: 'SKU',
            type: '',
            nullString: '',
            width: '20%'
        },
        {
            field: 'product',
            header: 'PRODUTO',
            type: '',
            nullString: '',
            width: '70%'
        },
        {
            field: 'measureUnit',
            header: 'UNIDADE DE MEDIDA',
            type: '',
            nullString: '',
            width: '10%'
        }
    ];

    constructor(private route: ActivatedRoute, private http: HttpClient, private socket: SocketService,
        private msgSvc: MessageService, private router: Router) { }
    tstBlur(e) {
        console.log(e);
    }
    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.http.get(environment.processserver + 'document/bytype/PLANPROD')
                .subscribe((docs: HunterDocument[]) => {
                    this.planProds = Array.from([]);
                    for (let doc of docs) {
                        let consumption: string[] = doc.items.filter(di => di.properties['PRODUCAO'] === 'CONSUMO').map(di => di.product.name);
                        if (consumption.length == 0 || doc.status != 'INTEGRADO')
                            continue;
                        let production: string[] = doc.items.filter(di => di.properties['PRODUCAO'] === 'PRODUCAO').map(di => di.product.name);
                        let prodLine = doc.fields.find(df => df.field.metaname === 'LINHA_PROD').value;
                        this.planProds.push(new ProductionOrderCompilation(doc, doc.code, consumption, production, prodLine));
                    }
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR DOCUMENTO', detail: error });
                });
        });
    }

    ngOnDestroy(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
    }

    consolidate() {
        this.documentItems = Array.from([]);
        this.ordProds = Array.from([]);
        for (let planProd of this.selPlanProds) {
            if (planProd instanceof ProductionOrderCompilation)
                continue;
            for (let ppDocItem of planProd.items.filter(di => di.properties['PRODUCAO'] === 'CONSUMO')) {
                let contains = this.documentItems.find(di => di.product.id === ppDocItem.product.id);

                if (contains === undefined) {
                    let trnDocItem = Object.assign({}, ppDocItem);

                    trnDocItem.id = null;//createNew
                    trnDocItem.properties = null;
                    this.documentItems.push(trnDocItem);
                } else {
                    contains.qty += ppDocItem.qty;
                }
            }
        }

        for (let docItem of this.documentItems) {
            let sku = docItem.product.sku;
            let prd = docItem.product.name;
            let qty = docItem.qty;
            let mu = docItem.measureUnit;

            this.ordProds.push(new ProductionOrder(sku, prd, qty, mu, docItem.product));
        }
    }

    createOrdTransfs() {
        if (this.documentItems.length > 0) {
            let di = this.documentItems[0];
            let po = this.ordProds.find(po => po.prd.id === di.product.id);

            if (po != undefined && po.qty > 0) {
                di.qty = po.qty;
                di.measureUnit = po.measureUnit;
                let ordTransf = new HunterDocument({
                    id: null,
                    code: '',
                    name: null,
                    metaname: null,
                    status: 'ATIVO',
                    createdAt: new Date(),
                    updatedAt: new Date(),
                    model: null,
                    fields: [],
                    items: [di],
                    things: [],
                    siblings: [...this.selPlanProds],
                    person: null,
                    parent: null,
                    props: new Map<string, string>()
                });
                this.sendOrdTransf(ordTransf);
            }
        } else {
            for (let doc of this.selPlanProds) {
                this.planProds = this.planProds.filter(pp => pp.document.id !== doc.id);
            }
            this.selPlanProds = Array.from([]);
            this.documentItems = Array.from([]);
            this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: "Documentos Criados" });
        }
    }

    sendOrdTransf(doc: HunterDocument) {
        this.http.post(environment.customserver + 'document/createtransfer', doc)
            .subscribe((resp: HunterDocument) => {
                console.log(resp);
                this.documentItems = Array.from(this.documentItems.slice(1));
                this.createOrdTransfs();
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO SALVAR DOCUMENTO', detail: error });
            });
    }

    cancelTransfer() {
        this.selPlanProds = Array.from([]);
        this.ordProds = Array.from([]);
    }
}
