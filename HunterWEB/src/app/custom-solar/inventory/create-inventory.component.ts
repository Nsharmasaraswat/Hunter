import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Observable, Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { HunterAddress } from "../../shared/model/HunterAddress";
import { HunterDocument } from "../../shared/model/HunterDocument";
import { HunterPermission, HunterPermissionCategory } from "../../shared/model/HunterPermission";
import { HunterProduct } from "../../shared/model/HunterProduct";
import { NavigationService } from "../../shared/services/navigation.service";


interface SelAddr {
    id: string;
    metaname: string;
}

interface SelPrd {
    name: string;
    product: HunterProduct;
}

@Component({
    selector: 'create-inventory',
    templateUrl: 'create-inventory.component.html',
    styleUrls: ['inventory.component.scss']
})
export class CreateInventoryComponent implements OnInit, OnDestroy {
    private routeSubscription: Subscription;
    private navigationSubscription: Subscription;

    permission: HunterPermission;
    source: SelAddr[] = [];
    target: SelAddr[] = [];
    sourceWH: SelAddr[] = [];
    targetWH: SelAddr[] = [];
    sourcePrdPA: SelPrd[] = [];
    targetPrdPA: SelPrd[] = [];
    sourcePrdMP: SelPrd[] = [];
    targetPrdMP: SelPrd[] = [];
    invtype: string = "";
    seltype: string = "";
    invCount: number = 2;

    constructor(private route: ActivatedRoute, private http: HttpClient, private navSvc: NavigationService,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.navigationSubscription = this.navSvc.getItems().subscribe((pCat: HunterPermissionCategory[]) => {
                for (let category of pCat) {
                    for (let menu of category.permissions) {
                        if (menu.route === this.router.url) {
                            this.permission = new HunterPermission(menu);
                            return;
                        }
                    }
                }
            });
        });
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

    loadWarehouses(): void {
        if (this.sourceWH === undefined || this.sourceWH === null || this.sourceWH.length === 0) {
            let warehouse = this.http.get(environment.processserver + 'address/bytype/WAREHOUSE');
            let picking = this.http.get(environment.processserver + 'address/bytype/PICKING');
            let segregation = this.http.get(environment.processserver + 'address/bytype/SEGREGATION');
            let repack = this.http.get(environment.processserver + 'address/bytype/REPACK');

            Observable.forkJoin([warehouse, picking, segregation, repack])
                .catch((err: HttpErrorResponse) => {
                    this.msgSvc.add({ severity: 'error', summary: "Falha ao Obter lista de Armazéns", detail: err.error });
                    return Observable.empty();
                })
                .subscribe((addresses: HunterAddress[][]) => {
                    for (let addrs of addresses) {
                        this.sourceWH = this.sourceWH.concat(addrs.map(addr => { return { 'id': addr.id, 'metaname': addr.name } }));
                    }
                    this.sourceWH = this.sourceWH.sort((a1, a2) => a1.metaname.localeCompare(a2.metaname));
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ARMAZÉNS', detail: error });
                },
                    () => console.log("Lodaded Warehouses"));
        }
    }

    loadAddresses(): void {
        if (this.source === undefined || this.source === null || this.source.length === 0) {
            let roads = this.http.get(environment.processserver + 'address/bytype/ROAD');
            let racks = this.http.get(environment.processserver + 'address/bytype/RACK');
            let drivein = this.http.get(environment.processserver + 'address/bytype/DRIVE-IN')
            
            Observable.forkJoin([roads, racks, drivein])
                .subscribe((addresses: HunterAddress[][]) => {
                    for (let addrs of addresses) {
                        this.source = this.source.concat(addrs.map(addr => { return { 'id': addr.id, 'metaname': addr.name } }));
                    }
                    this.source = this.source.sort((a1, a2) => a1.metaname.localeCompare(a2.metaname));
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ENDEREÇOS', detail: error });
                },
                    () => console.log("Lodaded Addresses"));
        }
    }

    loadPAProducts(): void {
        if (this.sourcePrdPA === undefined || this.sourcePrdPA === null || this.sourcePrdPA.length === 0) {
            this.http.get(environment.processserver + 'product/bytypeandsiblings/PA')
                .subscribe((products: HunterProduct[]) => {
                    this.sourcePrdPA = products.map(prd => { return { 'name': prd.sku + ' - ' + prd.name, 'product': prd } });
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR PRODUTOS', detail: error });
                },
                    () => console.log("Lodaded Products"));
        }
    }

    loadMPProducts(): void {
        if (this.sourcePrdMP === undefined || this.sourcePrdMP === null || this.sourcePrdMP.length === 0) {
            this.http.get(environment.processserver + 'product/bytype/MP')
                .subscribe((products: HunterProduct[]) => {
                    console.log(products);
                    this.sourcePrdMP = products.map(prd => { return { 'name': prd.sku + ' - ' + prd.name, 'product': prd } });
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR PRODUTOS', detail: error });
                },
                    () => console.log("Lodaded Products"));
        }
    }

    addInventory(): void {
        switch (this.invtype) {
            case "DRONE MANUAL":
                this.createStk();
                break;
            case "INVPA":
            case "INVMP":
                this.createInventory();
                break;
        }
    }

    createStk(): void {
        let addresses = [];

        for (let addr of this.target)
            addresses.push({ "id": addr.id });
            
        this.http.post(environment.customserver + 'wms/stksnapshot', addresses, { responseType: 'json' })
            .subscribe((document: HunterDocument) => {
                this.unsubscribeObservables();
                this.router.navigate(['home', 'custom-solar', 'check-inventory', document.id]);
            });
    }

    createInventory(): void {
        this.http.post(environment.customserver + 'wms/inventory/' + this.invtype + '/' + this.invCount, this.targetWH, { responseType: 'json' })
            .subscribe((document: HunterDocument) => {
                this.msgSvc.add({ severity: 'success', summary: 'INVENTÁRIO GERADO COM SUCESSO', detail: 'Aguarde a geração das tarefas de contagem e acesse-as pelo aplicativo hunter® WMS Mobile' });
                this.router.navigate(['home']);
            });
    }

}