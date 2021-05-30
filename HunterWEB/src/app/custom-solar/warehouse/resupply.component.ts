import { DatePipe } from '@angular/common';
import { HttpClient } from "@angular/common/http";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { SelectItem } from 'primeng/api';
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterPermission, HunterPermissionCategory } from '../../shared/model/HunterPermission';
import { HunterProduct, HunterProductModel } from '../../shared/model/HunterProduct';
import { NavigationService } from '../../shared/services/navigation.service';
import { SocketService } from '../../shared/services/socket.service';
import RestStatus from '../../shared/utils/restStatus';

class ResupplyStub {
    constructor(public product_id: string, public product_sku: string, public product_name: string, public ammount: number, public min_expiry: number, public prefix: string, public status: string) {
    }
}
@Component({
    selector: 'resupply',
    templateUrl: 'resupply.component.html',
    styleUrls: ['resupply.component.scss'],
    providers: [DatePipe]
})

export class ResupplyComponent implements OnInit, OnDestroy {
    private routeSubscription: Subscription;
    private navigationSubscription: Subscription;
    private restSubscription: Subscription;

    permission: HunterPermission;
    showAS: boolean;
    expValue: boolean = false;
    destId: string;
    movPrefix: string;
    movStatus: string;
    resupplyList: ResupplyStub[] = [];
    products: SelectItem[];
    selProduct: HunterProduct;
    colsResupply: ReportColumn[] = [
        {
            field: 'product_sku',
            header: 'CÓDIGO',
            type: 'TEXT',
            nullString: '',
            width: '15%'
        },
        {
            field: 'product_name',
            header: 'PRODUTO',
            type: 'TEXT',
            nullString: '',
            width: '70%'
        },
        {
            field: 'ammount',
            header: 'PALLETS',
            type: 'NUMBER',
            nullString: '',
            width: '15%'
        },
        {
            field: 'min_expiry',
            header: 'VALIDADE MÍNIMA',
            type: 'NUMBER',
            nullString: '',
            width: '15%'
        }
    ];

    constructor(private route: ActivatedRoute, private http: HttpClient, private socket: SocketService, private navSvc: NavigationService,
        private msgSvc: MessageService, private router: Router, private datePipe: DatePipe) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.resupplyList = Array.of(...[]);
            this.products = Array.of(...[]);
            this.navigationSubscription = this.navSvc.getItems().subscribe((pCat: HunterPermissionCategory[]) => {
                for (let category of pCat) {
                    for (let menu of category.permissions) {
                        if (menu.route === this.router.url) {
                            this.permission = new HunterPermission(menu);
                            if (this.permission.params !== null)
                                this.showAS = this.permission.params['as_option'] !== undefined && this.permission.params['as_option'] === 'true';
                            else
                                this.showAS = false;
                            console.log(this.permission);
                            return;
                        }
                    }
                }
            });
            this.destId = routeParams.destId;
            this.movPrefix = routeParams.movPrefix;
            this.movStatus = routeParams.movStatus;
            this.loadProductModel('PA');
        });
    }

    ngOnDestroy(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
        if (this.restSubscription != null)
            this.restSubscription.unsubscribe();
        if (this.navigationSubscription != null)
            this.navigationSubscription.unsubscribe();
    }

    loadProductModel(type: string) {
        this.restSubscription = this.http.get(environment.processserver + 'productmodel/bymetaname/' + type)
            .subscribe((paModel: HunterProductModel) => {
                this.restSubscription = this.http.get(environment.processserver + 'productmodel/listsiblings/' + paModel.id)
                    .subscribe((models: HunterProductModel[]) => {
                        models.forEach(pm => this.loadProducts(pm.metaname));
                    }, error => {
                        console.log(error);
                        this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR TIPOS DE PRODUTOS', detail: error });
                    },
                        () => console.log("Lodaded Product Models"));
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR CATEGORIA DE PRODUTOS', detail: error });
            },
                () => console.log("Lodaded ParendModel"));
    }

    loadProducts(type: string) {
        this.restSubscription = this.http.get(environment.processserver + 'product/bytype/' + type)
            .subscribe((products: HunterProduct[]) => {
                this.products = this.products.concat(products.map(prd => {
                    let pfUB = prd.fields.find(pf => pf.model.metaname === 'UNIT_BOX');
                    let unitbox = pfUB === undefined ? '' : ' C/' + pfUB.value;

                    return {
                        label: prd.sku + ' - ' + prd.name + unitbox,
                        value: prd
                    };
                }));
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR PRODUTOS', detail: error });
            },
                () => console.log("Lodaded Products"));
    }

    addResupply(ammount: number) {
        let minExpiry: number = !this.expValue ? 15 : 40;

        this.resupplyList.push(new ResupplyStub(this.selProduct.id, this.selProduct.sku, this.selProduct.name, ammount, minExpiry, this.movPrefix, this.movStatus));
    }

    remResupply(index: number) {
        console.log(index);
        this.resupplyList.splice(index, 1);
    }

    resupply() {
        this.http.post(environment.customserver + 'wms/resupply/' + this.destId, this.resupplyList, { responseType: 'json' })
            .catch(error => {
                console.error("error catched", error);
                return Observable.of({ result: false, message: "Error Value Emitted" });
            }).subscribe((resp: RestStatus) => {
                if (resp.result) {
                    this.resupplyList = Array.of(...[]);
                    this.msgSvc.add({ severity: 'success', summary: 'Sucesso', detail: 'Movimentação Gerada' });
                } else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO GERAR MOVIMENTAÇÃO DO PALLET', detail: resp.message });
            }, (error: RestStatus) => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO GERAR MOVIMENTAÇÃO', detail: error.message });
            });
    }
}