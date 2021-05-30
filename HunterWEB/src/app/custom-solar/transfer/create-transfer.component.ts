import { HttpClient } from "@angular/common/http";
import { AfterViewInit, Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterDocument, HunterDocumentItem } from "../../shared/model/HunterDocument";
import { HunterField } from "../../shared/model/HunterField";
import { HunterProduct } from "../../shared/model/HunterProduct";
import { SocketService } from '../../shared/services/socket.service';

const ELEM_HEIGHT: number = 50;

class ProductStub {
    selQty: number;
    constructor(public sku: string, public name: string, public product: HunterProduct) {
        this.selQty = 0;
    }
}

@Component({
    selector: 'create-transfer',
    templateUrl: 'create-transfer.component.html',
    styleUrls: ['create-transfer.component.scss']
})

export class CreateTransferComponent implements OnInit, OnDestroy, AfterViewInit {
    private routeSubscription: Subscription;

    colsPrd: ReportColumn[] = [
        {
            field: 'sku',
            header: 'SKU',
            type: '',
            nullString: '',
            width: '30%'
        },
        {
            field: 'name',
            header: 'DESCRIÇÃO',
            type: '',
            nullString: '',
            width: '70%'
        }
    ];

    rows: number;
    products: ProductStub[];
    docProperties: any;
    selectedProducts: ProductStub[];
    documentItems: HunterDocumentItem[];

    constructor(private route: ActivatedRoute, private http: HttpClient, private socket: SocketService,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.products = Array.from([]);
            this.documentItems = Array.from([]);
            this.selectedProducts = Array.from([]);
            this.docProperties = {
                FROM: 'MP01',
                TO: 'MP04'
            };

            this.http.get(environment.processserver + 'product/bytype/MP')
                .subscribe((products: HunterProduct[]) => {
                    for (let prd of products) {
                        this.products.push(new ProductStub(prd.sku, prd.name, prd));
                    }
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR PRODUTOS', detail: error });
                },
                    () => console.log("Lodaded Products"));
        });
    }

    ngAfterViewInit() {
        this.rows = Math.floor(window.innerHeight / ELEM_HEIGHT);
    }

    ngOnDestroy(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
    }

    clearTransf() {
        this.products.push(...this.selectedProducts.splice(0));
    }

    createDI(e) {
        let prd: ProductStub = e.data;
        let di = this.documentItems.find(itm => itm.product.id === prd.product.id);

        if (di === undefined)
            di = new HunterDocumentItem({});
        else {
            this.documentItems.splice(this.documentItems.indexOf(di), 1);
            this.selectedProducts.splice(this.selectedProducts.indexOf(prd), 1);
        }
        if (prd.selQty > 0) {
            di.product = prd.product;
            di.qty = prd.selQty;
            this.documentItems.push(di);
            this.selectedProducts.push(prd);
        }
        console.log(di);
        console.log(this.documentItems);
        console.log(this.selectedProducts);
    }

    removeDI(e) {
        console.log(e.data);
        let prd = e.data;
        this.products.push(...this.selectedProducts.splice(this.selectedProducts.indexOf(prd), 1));
    }

    createOrdTransfs() {
        if (this.selectedProducts.length > 0) {
            if (this.documentItems.length > 0) {
                let di = this.documentItems[0];
                let docFieldFROM = new HunterField({
                    field: {
                        id: '79327337-8216-11e9-815b-005056a19775'
                    },
                    value: this.docProperties['FROM']
                });
                let docFieldTO = new HunterField({
                    field: {
                        id: '7933b848-8216-11e9-815b-005056a19775'
                    },
                    value: this.docProperties['TO']
                });
                let ordTransf = new HunterDocument({
                    id: null,
                    code: '',
                    name: null,
                    metaname: null,
                    status: 'ATIVO',
                    createdAt: new Date(),
                    updatedAt: new Date(),
                    model: null,
                    fields: [docFieldFROM, docFieldTO],
                    items: [di],
                    things: [],
                    siblings: [],
                    person: null,
                    parent: null,
                    props: this.docProperties
                });
                this.sendOrdTransf(ordTransf);
            } else {
                let detail = this.selectedProducts.length === 1 ? 'Transferência criada' : 'Transferências criadas';
                this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: detail });
            }
        } else {
            this.msgSvc.add({ severity: 'error', summary: 'NENHUM PRODUTO SELECIONADO', detail: 'Selecione ao menos um produto para gerar a transferência' });
        }
    }

    sendOrdTransf(doc: HunterDocument) {
        this.documentItems = Array.from(this.documentItems.slice(1));
        this.http.post(environment.customserver + 'document/createtransfer', doc)
            .subscribe((resp: HunterDocument) => {
                this.createOrdTransfs();
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO SALVAR DOCUMENTO', detail: error });
            });
    }
}
