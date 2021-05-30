import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { AfterViewInit, Component, OnDestroy, OnInit, ViewEncapsulation } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Observable, Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterDocument, HunterDocumentItem } from "../../shared/model/HunterDocument";
import { HunterPermission, HunterPermissionCategory } from "../../shared/model/HunterPermission";
import { NavigationService } from "../../shared/services/navigation.service";

interface SAPInventory {
    code: string;
    date: Date;
    ano: number;
    centro: string;
    deposito: string;
    document: HunterDocument;
}

interface SAPInventoryItem {
    document: HunterDocument;
    item: HunterDocumentItem;
    sku: string;
    name: string;
    quantity: number;
    measure: string;
    adjustment: number;
    total: number;
}

@Component({
    selector: 'sap-inventory',
    templateUrl: 'sap-inventory.component.html',
    styleUrls: ['inventory.component.scss'],
    encapsulation: ViewEncapsulation.None //https://stackoverflow.com/a/50159982 - Access child component within scss
})
export class SAPInventoryComponent implements OnInit, OnDestroy, AfterViewInit {
    private permission: HunterPermission;
    private routeSubscription: Subscription;
    private navigationSubscription: Subscription;

    areaText: string;
    inventory: HunterDocument;
    inventoryItems: SAPInventoryItem[];
    sapinventory: SAPInventory[];
    sapinventoryItems: SAPInventoryItem[];
    selInventories: SAPInventory[];
    columns: ReportColumn[] = [
        {
            field: 'code',
            header: 'CÓDIGO',
            type: 'TEXT',
            nullString: '-'
        },
        {
            field: 'date',
            header: 'DATA',
            type: 'TIMESTAMP',
            nullString: '-'
        },
        {
            field: 'centro',
            header: 'CENTRO',
            type: 'TEXT',
            nullString: '-'
        },
        {
            field: 'deposito',
            header: 'DEPÓSITO',
            type: 'TEXT',
            nullString: '-'
        },
        {
            field: 'ano',
            header: 'ANO',
            type: 'TEXT',
            nullString: '-'
        }
    ];

    columnsItem: any[] = [
        {
            field: 'sku',
            header: 'CÓDIGO',
            type: 'TEXT',
            nullString: '-',
            width: '10ch',
            styleClass: 'text-right'
        },
        {
            field: 'name',
            header: 'PRODUTO',
            type: 'TEXT',
            nullString: '-',
            width: '99%',
            styleClass: ''
        },
        {
            field: 'quantity',
            header: 'QUANTIDADE',
            type: 'DECIMAL',
            nullString: '-',
            width: '15ch',
            styleClass: 'text-right'
        },
        {
            field: 'measure',
            header: 'U.MEDIDA',
            type: 'TEXT',
            nullString: '-',
            width: '10ch',
            styleClass: 'text-right'
        },
        {
            field: 'adjustment',
            header: 'AJUSTE',
            type: 'DECIMAL',
            nullString: '-',
            width: '10ch',
            styleClass: 'text-right'
        },
        {
            field: 'total',
            header: 'TOTAL',
            type: 'DECIMAL',
            nullString: '-',
            width: '10ch',
            styleClass: 'text-right'
        }
    ];

    constructor(private route: ActivatedRoute, private http: HttpClient, private navSvc: NavigationService,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.navigationSubscription = this.navSvc.getItems()
                .catch((err: HttpErrorResponse) => {
                    this.msgSvc.add({ severity: 'error', summary: "Falha ao Carregar Permissões", detail: err.error });
                    return Observable.empty();
                })
                .subscribe((pCat: HunterPermissionCategory[]) => {
                    for (let category of pCat) {
                        for (let menu of category.permissions) {
                            if (menu.route === this.router.url) {
                                this.permission = new HunterPermission(menu);
                                return;
                            }
                        }
                    }
                });
            this.http.get(environment.processserver + 'document/' + routeParams.docId)
                .catch((err: HttpErrorResponse) => {
                    this.msgSvc.add({ severity: 'error', summary: "Falha ao Carregar Inventário", detail: err.error });
                    return Observable.empty();
                })
                .subscribe((doc: HunterDocument) => {
                    this.inventory = doc;
                    this.inventoryItems = this.getItems(doc);
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ARMAZÉNS', detail: error });
                },
                    () => console.log("Lodaded Inventory"));
        });
    }

    ngAfterViewInit(): void {
        this.http.get(environment.processserver + 'document/bytypestatus/SAPINVENTORY/ATIVO')
            .catch((err: HttpErrorResponse) => {
                this.msgSvc.add({ severity: 'error', summary: "Falha ao Carregar Inventários SAP", detail: err.error });
                return Observable.empty();
            })
            .subscribe((docs: HunterDocument[]) => {
                this.sapinventory = docs.map(d => {
                    let doc = new HunterDocument(d);
                    return {
                        code: doc.code,
                        date: doc.createdAt,
                        ano: +doc.props.ano,
                        centro: doc.props.centro,
                        deposito: doc.props.deposito,
                        document: doc
                    }
                });
                this.sapinventoryItems = Array.of(...[]);

                console.log(docs);
                for (let d of docs) {
                    this.sapinventoryItems = this.sapinventoryItems.concat(this.getItems(d));
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ARMAZÉNS', detail: error });
            },
                () => console.log("Lodaded SAP Inventories"));
    }

    ngOnDestroy(): void {
        this.unsubscribeObservables();
    }

    unsubscribeObservables(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
        if (this.navigationSubscription != null)
            this.navigationSubscription.unsubscribe();
    }

    getItems(d: HunterDocument): SAPInventoryItem[] {
        let sapitm: SAPInventoryItem[] = Array.of(...[]);
        let adj: HunterDocument = d.siblings.find(d => d.model.metaname === 'INVADJUST');

        for (let itm of d.items) {
            let adjItm = undefined;

            if (adj !== undefined)
                adjItm = adj.items.find(di => di.product.id === itm.product.id);

            sapitm.push({
                'document': d,
                'item': itm,
                'sku': itm.product.sku,
                'name': itm.product.name,
                'quantity': itm.qty,
                'measure': itm.measureUnit,
                'adjustment': (adjItm === undefined ? 0 : adjItm.qty),
                'total': itm.qty + (adjItm === undefined ? 0 : adjItm.qty)
            })
        }
        return sapitm;
    }

    sendInventory(): void {
        this.http.put(environment.customserver + 'wms/sapinventory/' + this.inventory.id, this.selInventories.map(si => { return { id: si.document.id }; }), { responseType: 'json' })
            .catch((err: HttpErrorResponse) => {
                this.msgSvc.add({ severity: 'error', summary: "Falha ao Carregar Inventários SAP", detail: err.error });
                return Observable.empty();
            })
            .subscribe((document: HunterDocument) => {
                this.msgSvc.add({ severity: 'success', summary: 'INVENTÁRIO GERADO COM SUCESSO', detail: 'Aguarde a geração das tarefas de contagem e acesse-as pelo aplicativo hunter® WMS Mobile' });
                this.router.navigate(['home']);
            });
    }

    onPaste($event): void {
        if ($event !== undefined && $event.clipboardData !== undefined) {
            let lines: string[] = $event.clipboardData.getData('Text').split('\n');

            this.calcAdjustments(lines);
        }
    }

    calcAdjustments(lines: string[]) {
        for (let line of lines) {
            if (line.length > 0) {
                let fields: string[] = line.split('\t');

                for (let itm of this.inventoryItems) {
                    let cod = fields[0].length > 7 ? fields[0].slice(-7) : fields[0];

                    if (itm.sku === cod) {
                        itm.adjustment = +(fields[1].replace(',', '.'));
                    }
                    itm.total = itm.quantity + itm.adjustment;
                }
            }
        }
    }

    getClass(col: ReportColumn, itm: SAPInventoryItem): string {
        if (col.field === 'quantity' && itm.total !== itm.quantity) return 'inv_missing';
        if (col.field === 'total' && itm.total < itm.quantity) return 'inv_greater';
        if (col.field === 'total' && itm.total > itm.quantity) return 'adj_add';
        if (col.field === 'adjustment' && itm.adjustment > 0) return 'adj_add';
        if (col.field === 'adjustment' && itm.adjustment < 0) return 'inv_greater';
        return '';
    }

    saveAdjustments(): void {
        let adjItems: HunterDocumentItem[] = this.inventoryItems
            .filter(ii => ii.adjustment !== 0)
            .map(ii => {
                ii.item.qty = ii.adjustment;
                return ii.item;
            });

        if (adjItems !== undefined && adjItems.length > 0) {
            this.http.post(environment.customserver + 'wms/sapinventoryadjustment/' + this.inventory.id, adjItems, { responseType: 'json' })
                .catch((err: HttpErrorResponse) => {
                    this.msgSvc.add({ severity: 'error', summary: "Falha ao Salvar Ajustes de Inventário", detail: err.error });
                    return Observable.empty();
                })
                .subscribe((doc: HunterDocument) => {
                    this.inventory = doc;
                    this.inventoryItems = this.getItems(doc);
                    this.msgSvc.add({ severity: 'success', summary: 'DOCUMENTO SALVO', detail: 'Ajuste de inventário salvo com sucesso!' });
                    // this.ngOnDestroy();
                    // this.ngOnInit();
                });
        }
    }

    clearAdjustments(): void {
        this.areaText = '';
        this.inventoryItems.forEach(itm => {
            itm.adjustment = 0;
            itm.total = itm.quantity;
        });
    }

    recalculateAdjustments(): void {
        this.inventoryItems.forEach(itm => {
            itm.total = +itm.quantity + +itm.adjustment;
        });
    }

    preventMinusBug(val: string): number {
        if (val === '-') return 0;
        if (val === '+') return 0;
        return +val;
    }
}