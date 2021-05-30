import { DatePipe } from '@angular/common';
import { HttpClient } from "@angular/common/http";
import { AfterViewInit, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, ViewEncapsulation } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { SelectItem } from 'primeng/api';
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterAddress } from '../../shared/model/HunterAddress';
import { HunterField } from '../../shared/model/HunterField';
import { HunterModelField } from '../../shared/model/HunterModelField';
import { HunterPermissionCategory } from '../../shared/model/HunterPermission';
import { HunterProduct } from '../../shared/model/HunterProduct';
import { HunterThing } from "../../shared/model/HunterThing";
import { NavigationService } from '../../shared/services/navigation.service';
import { SocketService } from '../../shared/services/socket.service';
import RestStatus from '../../shared/utils/restStatus';

const ELEM_HEIGHT: number = 55;

class ThingStub {
    constructor(public id: string, public addr_name: string, public sku: string, public name: string, public status: string, public lot: string, public quantity: number,
        public man: Date, public exp: Date, public allocation: number, public enableActions: boolean, public thing: HunterThing) {

    }
}

class PalletStub {
    constructor(public id: string, public lot: string, public man: Date, public exp: Date, public qty: number, public vol: number, public lyb: number, public lyr: number,
        public plb: number, public bx: number) {

    }
}

class AddressOcupationStub {
    public ocupation: number;
    public relOcupation: number;

    constructor(public address_id: string, public name: string, public status: string, public products: string, public productStatus: string, public capacity: number,
        public free: number) {
        this.calcOcupation();
    }

    addPallet(qty: number): void {
        this.free -= qty;
        this.calcOcupation();
    }

    removePallets(qty: number) {
        this.free += qty;
        this.calcOcupation();
    }

    calcOcupation(): void {
        this.ocupation = this.capacity - this.free;
        this.relOcupation = this.capacity === 0 ? 100 : (this.ocupation) / this.capacity * 100;
        if (this.ocupation == 0) {
            this.products = '';
            this.productStatus = '';
        }
    }
}

@Component({
    selector: 'manage-address',
    templateUrl: 'manage-address.component.html',
    styleUrls: ['manage-address.component.scss'],
    providers: [DatePipe],
    encapsulation: ViewEncapsulation.None //https://stackoverflow.com/a/50159982 - Access child component within scss
})

export class ManageAddressComponent implements OnInit, OnDestroy, AfterViewInit {
    private routeSubscription: Subscription;
    private restSubscription: Subscription;
    private navigationSubscription: Subscription;

    canAddPallet: boolean;
    canBlockProduct: boolean;
    canClearAddress: boolean;
    canCopyPallet: boolean;
    canRemovePallet: boolean;
    canUnblockProduct: boolean;
    canTransportPallet: boolean;
    selProd: HunterProduct;
    selPallet: PalletStub = new PalletStub(null, 'HWEB', null, null, 0, 1, 0, 0, 0, 0);
    selAddress: AddressOcupationStub[] = [];
    addressList: AddressOcupationStub[] = [];
    destSiblings: SelectItem[];
    sibDest: HunterAddress;
    thingList: ThingStub[] = [];
    selThing: HunterThing;
    detAddress: AddressOcupationStub;
    selAddressDest: AddressOcupationStub;
    displayDialog: boolean;
    detailDialogWidth: number;
    detailDialogHeight: number;
    detailDialogTop: number;
    displayDialogAdd: boolean;
    displayDialogRem: boolean;
    displayDialogEdit: boolean;
    displayDialogTransport: boolean;
    displayDialogChangeAddress: boolean;
    bypassThingAction: boolean = false;
    destinations: SelectItem[];
    colsAddress: ReportColumn[] = [
        {
            field: 'name',
            header: 'ENDEREÇO',
            type: 'TEXT',
            nullString: '',
            priority: 1,
            width: '8%'
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: '',
            priority: 6,
            width: '7%'
        },
        {
            field: 'products',
            header: 'PRODUTOS',
            type: 'TEXT',
            nullString: '',
            priority: 1,
            width: '35%'
        },
        {
            field: 'productStatus',
            header: 'STATUS PRODUTO',
            type: 'TEXT',
            nullString: '',
            priority: 3,
            width: '9%'
        },
        {
            field: 'capacity',
            header: 'CAP.',
            type: 'NUMBER',
            nullString: '',
            priority: 6,
            width: '3%'
        },
        {
            field: 'ocupation',
            header: 'OCUP.',
            type: 'NUMBER',
            nullString: '',
            priority: 2,
            width: '4%'
        },
        {
            field: 'free',
            header: 'LIVRE',
            type: 'NUMBER',
            nullString: '',
            priority: 6,
            width: '4%'
        },
        {
            field: 'relOcupation',
            header: '%',
            type: 'NUMBER',
            nullString: '',
            priority: 6,
            width: '3%'
        }
    ];
    colsThing: ReportColumn[] = [
        {
            field: 'addr_name',
            header: 'POSIÇÃO',
            type: 'TEXT',
            nullString: '',
            priority: 2,
            width: '12ch'
        },
        {
            field: 'sku',
            header: 'CÓD.',
            type: 'TEXT',
            nullString: '',
            priority: 1,
            width: '9ch'
        },
        {
            field: 'name',
            header: 'NOME',
            type: 'TEXT',
            nullString: '',
            priority: 1,
            width: '90%'
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: '',
            priority: 5,
            width: '15ch'
        },
        {
            field: 'quantity',
            header: 'QTD.',
            type: 'NUMBER',
            nullString: '',
            priority: 3,
            width: '5ch'
        },
        {
            field: 'lot',
            header: 'LOTE',
            type: 'TEXT',
            nullString: '',
            priority: 6,
            width: '20ch'
        },
        {
            field: 'man',
            header: 'FAB.',
            type: 'DATE',
            nullString: '',
            priority: 1,
            width: '11ch'
        },
        {
            field: 'exp',
            header: 'VENC.',
            type: 'DATE',
            nullString: '',
            priority: 1,
            width: '11ch'
        }
    ];

    rows: number = 15;
    rowOptions: number[] = [5, 10, 15, 25, 50];
    rowOptionsThing: number[] = [5, 8, 10, 12, 15];

    constructor(private route: ActivatedRoute, private http: HttpClient, private socket: SocketService, private cdRef: ChangeDetectorRef,
        private msgSvc: MessageService, private router: Router, private datePipe: DatePipe, private navSvc: NavigationService) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.navigationSubscription = this.navSvc.getItems().subscribe((perms: HunterPermissionCategory[]) => {
                perms.forEach(category => {
                    category.permissions.forEach(menu => {
                        if (menu.route === this.router.url) {
                            this.canClearAddress = menu.properties !== undefined && menu.properties['clear_address'] !== undefined && menu.properties['clear_address'].toLowerCase() === 'true';
                            this.canAddPallet = menu.properties !== undefined && menu.properties['add_pallet'] !== undefined && menu.properties['add_pallet'].toLowerCase() === 'true';
                            this.canCopyPallet = menu.properties !== undefined && menu.properties['copy_pallet'] !== undefined && menu.properties['copy_pallet'].toLowerCase() === 'true';
                            this.canRemovePallet = menu.properties !== undefined && menu.properties['remove_pallet'] !== undefined && menu.properties['remove_pallet'].toLowerCase() === 'true';
                            this.canTransportPallet = menu.properties !== undefined && menu.properties['transport_pallet'] !== undefined && menu.properties['transport_pallet'].toLowerCase() === 'true';
                            this.canBlockProduct = menu.properties !== undefined && menu.properties['block_product'] !== undefined && menu.properties['block_product'].toLowerCase() === 'true';
                            this.canUnblockProduct = menu.properties !== undefined && menu.properties['unblock_product'] !== undefined && menu.properties['unblock_product'].toLowerCase() === 'true';
                        }
                    });
                });
            });
            this.loadAddresses();
        });
    }

    ngAfterViewInit() {
        this.rows = Math.floor(window.innerHeight / ELEM_HEIGHT);
        this.rowOptions = [Math.floor(this.rows / 3), Math.floor(this.rows / 2), this.rows, this.rows * 2, this.rows * 3];
        this.detailDialogWidth = window.innerWidth * 0.99;
        this.detailDialogHeight = window.innerHeight * 0.99;
        if (window.innerHeight < 720)
            this.detailDialogTop = 0;
    }

    @HostListener('document:keydown', ['$event'])
    public onKeyDown(eventData: KeyboardEvent) {
        this.bypassThingAction = eventData.altKey && eventData.ctrlKey && eventData.shiftKey;
    }

    @HostListener('document:keyup', ['$event'])
    public onKeyUp(eventData: KeyboardEvent) {
    }

    @HostListener('window:beforeunload', ['$event'])
    beforeUnloadHander(event) {
        console.log("beforeUnload");
        this.ngOnDestroy();
    }

    ngOnDestroy(): void {
        console.log('onDestroy');
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
        if (this.restSubscription != null)
            this.restSubscription.unsubscribe();
        if (this.navigationSubscription != null)
            this.navigationSubscription.unsubscribe();
    }

    changePalletsStatus(status: string): void {
        while (this.selAddress.length > 0) {
            let selAddr = this.selAddress.shift();

            this.http.put(environment.customserver + 'wms/changePalletStatusByAddress/' + selAddr.address_id + '/' + status, null).catch(error => {
                console.error("error catched", error);
                return Observable.of({ result: false, message: "Error Value Emitted" });
            }).subscribe((resp: RestStatus) => {
                if (resp.result) {
                    this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: resp.message });
                    selAddr.productStatus = status;
                    this.selAddress = this.selAddress.slice();
                } else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ALTERAR PALETES', detail: resp.message });
            }, (error: RestStatus) => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ALTERAR PALETES', detail: error.message });
            });
        }
    }

    async changeComplete() {
        while (this.selAddress.length > 0) {
            let selAddr = this.selAddress.shift();

            console.log('Call ', selAddr.name);
            await this.http.delete(environment.customserver + 'wms/clearAddress/' + selAddr.address_id)
                .catch(error => {
                    console.error("error catched", error);
                    return Observable.of({ result: false, message: "Error Value Emitted" });
                })
                .toPromise()
                .then((resp: RestStatus) => {
                    if (resp.result) {
                        //selAddr.removePallets(selAddr.ocupation);
                        selAddr.products = '';
                        selAddr.ocupation = 0;
                        selAddr.productStatus = '';
                        selAddr.free = selAddr.capacity;
                        selAddr.relOcupation = 0;
                        this.msgSvc.add({ severity: 'success', summary: 'ENDEREÇO ' + selAddr.name + ' ESVAZIADO', detail: resp.message });
                        this.selAddress = this.selAddress.slice();
                    } else
                        this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ESVAZIAR ENDEREÇOS', detail: resp.message });
                }, (error: RestStatus) => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ESVAZIAR ENDEREÇOS', detail: error.message });
                });
        }
    }

    add(address: AddressOcupationStub) {
        this.detAddress = address;
        if (this.selPallet !== null || this.selPallet !== undefined)
            this.selPallet.id = null;
        this.displayDialogAdd = true;
    }

    rem(address: AddressOcupationStub) {
        this.detAddress = address;
        this.displayDialogRem = true;
    }

    trn(address: AddressOcupationStub) {
        this.detAddress = address;
        this.displayDialogTransport = true;
    }

    chngAddr(tStub: ThingStub) {
        this.selThing = tStub.thing;
        this.displayDialogChangeAddress = true;
    }

    detailAddress(address: AddressOcupationStub) {
        this.detAddress = address;
        this.http.get(environment.customserver + 'wms/stkAddressList/' + address.address_id)
            .subscribe((things: HunterThing[]) => {
                let tzOffset = new Date().getTimezoneOffset() * 60 * 1000;
                this.thingList = things
                    .filter(th => th.properties.map(pr => pr.field).find(prmf => prmf.metaname === 'QUANTITY') !== undefined)
                    .map(th => {
                        let hasChildren: boolean = th.siblings !== undefined && th.siblings.length > 0;
                        let sibs: HunterThing[] = hasChildren ? th.siblings : Array.of(...[th]);
                        let lotField: HunterModelField = th.properties.map(pr => pr.field).find(prmf => prmf.metaname === 'LOT_ID');
                        let qtyField: HunterModelField = th.properties.map(pr => pr.field).find(prmf => prmf.metaname === 'QUANTITY');
                        let manField: HunterModelField = th.properties.map(pr => pr.field).find(amf => amf.metaname === 'MANUFACTURING_BATCH');
                        let expField: HunterModelField = th.properties.map(pr => pr.field).find(amf => amf.metaname === 'LOT_EXPIRE');
                        let lot: string = sibs[0].properties.find(pr => pr.modelfield_id === lotField.id).value;
                        let qty: number = sibs.map(ths => +ths.properties.find(pr => pr.modelfield_id === qtyField.id).value).reduce((a, b) => a + b);
                        let prMan: HunterField = sibs[0].properties.find(pr => pr.modelfield_id === manField.id);
                        let man: Date = hasChildren && prMan.value !== 'TEMPORARIO' && prMan.value !== 'Indeterminado' ? new Date(this.getDate(prMan.value).getTime() + tzOffset) : new Date(th.createdAt);
                        let prExp: HunterField = sibs[0].properties.find(pr => pr.modelfield_id === expField.id);
                        let exp: Date = hasChildren && prMan.value !== 'TEMPORARIO' && prMan.value !== 'Indeterminado' ? new Date(this.getDate(prExp.value).getTime() + tzOffset) : new Date(th.createdAt);
                        let payload: any = (th.payload === null || th.payload === undefined) ? { allocation: 3 } : JSON.parse(th.payload);
                        let allocation: any = payload['allocation'];

                        if (qty !== undefined && man !== undefined && exp !== undefined)
                            return new ThingStub(sibs[0].id, th.address.name, sibs.map(th => th.product.sku).join(','), sibs.map(th => th.product.name).join(','), th.status, lot, qty, man, exp, allocation, allocation === 3, sibs[0]);
                        else
                            console.log('Fix ' + th.id + ' Fields');
                    });
                this.displayDialog = true;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR PALLETS', detail: error });
            },
                () => console.log("Lodaded Things"));
    }

    getDate(value1: string): Date {
        if (value1.match(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/)) {
            return new Date(value1.replace(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/, "$2/$1/$3 $4:$5:$6"))
        } else if (value1.match(/(\d{2})\/(\d{2})\/(\d{4})/)) {
            return new Date(value1.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$2/$1/$3"))
        }
        return new Date(value1);
    }

    detailPallet(tStub: ThingStub) {
        let lot_prefix = tStub.thing.properties.find(pr => pr.field.metaname === 'LOT_ID').value.substring(0, 4);
        let lyr = this.selPallet === null ? 0 : this.selPallet.lyr;
        let lyb = this.selPallet === null ? 0 : this.selPallet.lyb;
        let plb = this.selPallet === null ? 0 : this.selPallet.plb;

        if (this.selProd === undefined || this.selProd.id !== tStub.thing.product.id) {
            this.selProd = tStub.thing.product;
            let pfLyr = this.selProd.fields.find(pf => pf.model.metaname === 'PALLET_LAYER');
            let pfLyb = this.selProd.fields.find(pf => pf.model.metaname === 'BOX_LAYER');
            let pfPlb = this.selProd.fields.find(pf => pf.model.metaname === 'PALLET_BOX');

            lyb = pfLyb === undefined ? 0 : +pfLyb.value;
            lyr = pfLyr === undefined ? 0 : +pfLyr.value;
            plb = pfPlb === undefined ? 0 : +pfPlb.value;
        }
        this.selPallet = new PalletStub(tStub.thing.id, lot_prefix, tStub.man, tStub.exp, tStub.quantity, 1, lyb, lyr, plb, 0);
        this.displayDialogAdd = true;
    }

    addPallets() {
        let sMan = this.datePipe.transform(this.selPallet.man, 'yyyy-MM-ddT00:00:00');
        let sExp = this.datePipe.transform(this.selPallet.exp, 'yyyy-MM-ddT00:00:00');

        if (this.selProd === undefined || this.selProd === null)
            this.msgSvc.add({ severity: 'error', summary: 'PREENCHER CÓDIGO', detail: 'Digite o código do produto a ser incluído no endereço' });
        else if (sMan === null) {
            this.msgSvc.add({ severity: 'error', summary: 'PREENCHER FABRICAÇÃO', detail: 'Escolha a data de fabricação do pallet a ser incluído no endereço' });
        } else if (sExp === null) {
            this.msgSvc.add({ severity: 'error', summary: 'PREENCHER VENCIMENTO', detail: 'Escolha a data de vencimento do pallet a ser incluído no endereço' });
        } else if (this.selPallet.vol === undefined) {
            this.msgSvc.add({ severity: 'error', summary: 'PREENCHER VOLUMES', detail: 'Digite a quantidade de pallets a ser incluída do endereço' });
        } else if (this.selPallet.qty === undefined) {
            this.msgSvc.add({ severity: 'error', summary: 'PREENCHER QUANTIDADE', detail: 'Digite a quantidade de caixas a ser incluída no pallet' });
        } else {
            let stub = {
                "id": this.selPallet.id,
                "lot-prefix": this.selPallet.lot,
                "product-id": this.selProd.id,
                "address-id": this.detAddress.address_id,
                "manufacture": sMan,
                "expire": sExp,
                "status": "ARMAZENADO",
                "quantity": this.selPallet.qty,
                "volumes": this.selPallet.vol
            }
            console.log(stub);
            this.sendThing(stub);
        }
    }

    copyPallet(toCopy: ThingStub) {
        let sMan = this.datePipe.transform(toCopy.man, 'yyyy-MM-ddT00:00:00');
        let sExp = this.datePipe.transform(toCopy.exp, 'yyyy-MM-ddT00:00:00');

        let stub = {
            "id": null,
            "lot-prefix": toCopy.thing.properties.find(pr => pr.field.metaname === 'LOT_ID').value.substring(0, 4),
            "product-id": toCopy.thing.product.id,
            "address-id": this.detAddress.address_id,
            "manufacture": sMan,
            "expire": sExp,
            "status": toCopy.status,
            "quantity": toCopy.quantity,
            "volumes": 1,
            "thing": toCopy
        }
        this.selProd = toCopy.thing.product;
        console.log(stub);
        this.sendThing(stub);
    }

    removePallets(qty: number) {
        if (qty === undefined || qty === null)
            this.msgSvc.add({ severity: 'error', summary: 'PREENCHER QUANTIDADE', detail: 'Digite a quantidade de pallets a ser excluída do endereço' });
        else {
            console.log(this.detAddress);
            console.log(qty);
            this.http.delete(environment.customserver + 'wms/removeFromAddress/' + this.detAddress.address_id + '/' + qty).catch(error => {
                console.error("error catched", error);
                return Observable.of({ result: false, message: "Error Value Emitted" });
            }).subscribe((resp: RestStatus) => {
                if (resp.result) {
                    this.detAddress.removePallets(qty);
                    if (this.detAddress.ocupation === 0)
                        this.detAddress.products = '';
                    this.msgSvc.add({ severity: 'success', summary: qty + ' ENDEREÇOS ESVAZIADOS', detail: resp.message });
                } else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ESVAZIAR ENDEREÇOS', detail: resp.message });
            }, (error: RestStatus) => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ESVAZIAR ENDEREÇOS', detail: error.message });
            });
        }
    }

    removePallet(id: string): void {
        this.http.delete(environment.customserver + 'wms/removePallet/' + id).catch(error => {
            console.error("error catched", error);
            return Observable.of({ result: false, message: "Error Value Emitted" });
        }).subscribe((resp: RestStatus) => {
            if (resp.result) {
                this.detAddress.removePallets(1);
                if (this.detAddress.ocupation === 0)
                    this.detAddress.products = '';
                this.thingList.splice(this.thingList.findIndex(th => th.id === id), 1);
                this.msgSvc.add({ severity: 'success', summary: 'PALLET EXCLUÍDO', detail: resp.message });
            } else
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXLCUIR PALLET', detail: resp.message });
        }, (error: RestStatus) => {
            console.log(error);
            this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXCLUIR PALLET', detail: error.message });
        });
    }

    loadProduct(sku: string) {
        this.http.get(environment.processserver + 'product/bysku/' + sku)
            .catch(error => {
                console.error("error catched", error);
                return Observable.of({ result: false, message: "Refaça Login" });
            })
            .subscribe((prod: HunterProduct) => {
                this.selProd = prod;
                if (prod !== null) {
                    if (this.selPallet.exp !== null && this.datePipe.transform(this.selPallet.exp, 'yyyy-MM-ddT00:00:00') !== null)
                        this.expireSelected(this.selPallet.exp);
                    if (this.selPallet !== undefined) {
                        let pfLyr = this.selProd.fields.find(pf => pf.model.metaname === 'PALLET_LAYER');
                        let pfLyb = this.selProd.fields.find(pf => pf.model.metaname === 'BOX_LAYER');
                        let pfPlb = this.selProd.fields.find(pf => pf.model.metaname === 'PALLET_BOX');

                        if (pfLyb !== undefined)
                            this.selPallet.lyb = +pfLyb.value;
                        if (pfLyr !== undefined)
                            this.selPallet.lyr = +pfLyr.value;
                        if (pfPlb !== undefined)
                            this.selPallet.plb = +pfPlb.value;
                    }
                    this.calcBoxPallet();
                } else {
                    this.selPallet.qty = 0;
                    this.msgSvc.add({ severity: 'error', summary: 'PRODUTO INEXISTENTE', detail: 'Não foi possível encontrar produto com o código ' + sku });
                }

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR PRODUTO', detail: error });
            },
                () => console.log("Lodaded Product"));
    }

    loadAddresses() {
        this.destinations = Array.of(...[]);
        this.addressList = Array.of(...[]);
        this.http.get(environment.customserver + 'wms/listStock/')
            .subscribe((addresses: AddressOcupationStub[]) => {
                this.addressList = addresses.map(aos => new AddressOcupationStub(aos.address_id, aos.name, aos.status, aos.products, aos.productStatus, aos.capacity, aos.free));
                this.destinations = this.addressList.map(aos => {
                    return {
                        label: aos.name + ' - ' + aos.status,
                        value: aos
                    }
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ENDEREÇOS', detail: error });
            },
                () => console.log("Lodaded Addresses"));
    }

    loadAddressSiblings() {
        this.http.get(environment.processserver + 'address/' + this.selAddressDest.address_id)
            .subscribe((address: HunterAddress) => {
                this.destSiblings = address.siblings.map((a: HunterAddress) => {
                    return {
                        label: a.metaname,
                        value: a
                    }
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ENDEREÇOS', detail: error });
            },
                () => console.log("Lodaded Addresses"));
    }

    transportPallets(qty: number) {
        if (this.selAddressDest !== undefined && this.detAddress.address_id === this.selAddressDest.address_id)
            this.msgSvc.add({ severity: 'warn', summary: 'ORIGEM = DESTINO', detail: 'Movimentação apenas sistemica, origem = destino. Completar a movimentação manualmente' });
        if (qty === undefined || qty === null || qty === 0)
            this.msgSvc.add({ severity: 'error', summary: 'PREENCHER QUANTIDADE', detail: 'Digite a quantidade de pallets a ser movimentada do endereço' });
        else if (this.selAddressDest === undefined)
            this.msgSvc.add({ severity: 'error', summary: 'SELECIONE O DESTINO', detail: 'Selecione o endereço de destino da movimentação' });
        else {
            this.http.post(environment.customserver + 'wms/transportPallet/' + this.detAddress.address_id + '/' + this.selAddressDest.address_id + '/' + qty, { responseType: 'json' })
                .catch(error => {
                    console.error("error catched", error);
                    return Observable.of({ result: false, message: "Error Value Emitted" });
                }).subscribe((resp: RestStatus) => {
                    if (resp.result)
                        this.msgSvc.add({ severity: 'success', summary: 'Sucesso', detail: 'Movimentação Gerada' });
                    else
                        this.msgSvc.add({ severity: 'error', summary: 'ERRO AO GERAR MOVIMENTAÇÃO DO PALLET', detail: resp.message });
                }, (error: RestStatus) => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CRIAR PALLET', detail: error.message });
                });
        }
    }

    changePalletAddress() {
        let tId = this.selThing.parent_id === null || this.selThing.parent_id === undefined ? this.selThing.id : this.selThing.parent_id;
        this.http.put(environment.customserver + 'wms/changePalletAddress/' + tId + '/' + this.sibDest.id, { responseType: 'json' })
            .catch(error => {
                console.error("error catched", error);
                return Observable.of({ result: false, message: "Error Value Emitted" });
            }).subscribe((resp: RestStatus) => {
                if (resp.result) {
                    this.detAddress.removePallets(1);
                    if (this.detAddress.ocupation === 0)
                        this.detAddress.products = '';
                    this.thingList.splice(this.thingList.findIndex(th => th.id === this.selThing.id), 1);
                    this.selAddressDest.addPallet(1);
                    if (!this.selAddressDest.products.includes(this.selThing.product.sku))
                        this.selAddressDest.products += (this.selThing.product.sku + ' - ' + this.selThing.product.name);
                    if (!this.selAddressDest.productStatus.includes(this.selThing.status))
                        this.selAddressDest.productStatus += this.selThing.status;
                    for (let idx = 1; idx < this.destSiblings.length; idx++) {
                        let tmp = this.destSiblings[idx];

                        if (tmp.value.id === this.sibDest.id) {
                            this.sibDest = this.destSiblings[idx - 1].value;
                            break;
                        }
                    }
                    this.selThing = null;
                    this.displayDialogChangeAddress = false;
                    this.msgSvc.add({ severity: 'success', summary: 'Sucesso', detail: 'Alteração Concluída' });
                }
                else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO GERAR MOVIMENTAÇÃO DO PALLET', detail: resp.message });
            }, (error: RestStatus) => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CRIAR PALLET', detail: error.message });
            });
    }

    sendThing(stub: any) {
        let obs: Observable<Object> = null;
        let add = stub.id === null;

        if (add)
            obs = this.http.post(environment.customserver + 'wms/createPallet', stub, { responseType: 'json' });
        else
            obs = this.http.put(environment.customserver + 'wms/editPallet/' + stub.id, stub, { responseType: 'json' });

        obs.catch(error => {
            console.error("error catched", error);
            return Observable.of({ result: false, message: "Error Value Emitted" });
        }).subscribe((resp: RestStatus) => {
            if (resp.result) {
                if (add) {
                    let prd: string = this.selProd.sku + ' - ' + this.selProd.name;

                    if (this.detAddress.products.indexOf(prd) < 0) {
                        if (this.detAddress.products.length !== 0)
                            this.detAddress.products += ', ';
                        this.detAddress.products += prd;
                    }
                    this.detailAddress(this.detAddress);
                    this.detAddress.addPallet(stub.volumes);
                } else {
                    this.detailAddress(this.detAddress);
                }
                this.displayDialogAdd = false;
                this.msgSvc.add({ severity: 'success', summary: 'Sucesso', detail: resp.message });
            } else
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CRIAR PALLET', detail: resp.message });
        }, (error: RestStatus) => {
            console.log(error);
            this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CRIAR PALLET', detail: error.message });
        });
    }

    expireSelected(ev: Date) {
        let exp: Date = new Date(ev.getTime());
        let man: Date = new Date(ev.getTime());
        let pmfShelfLife = this.selProd.model.fields.find(pmf => pmf.metaname === 'SHELFLIFE');

        man.setDate(man.getDate() - +this.selProd.fields.find(pf => pf.model.id === pmfShelfLife.id).value)
        this.selPallet.man = man;
        this.selPallet.exp = exp;
    }

    manufactureSelected(ev: Date) {
        let exp: Date = new Date(ev.getTime());
        let man: Date = new Date(ev.getTime());
        let pmfShelfLife = this.selProd.model.fields.find(pmf => pmf.metaname === 'SHELFLIFE');

        exp.setDate(man.getDate() + +this.selProd.fields.find(pf => pf.model.id === pmfShelfLife.id).value)
        this.selPallet.man = man;
        this.selPallet.exp = exp;
    }

    calcBoxPallet(): void {
        this.selPallet.qty = this.selPallet.plb;
    }
    calcBoxLayer(): void {
        this.selPallet.qty = this.selPallet.lyb * this.selPallet.lyr + this.selPallet.bx;
    }

    viewHistory(th: HunterThing): void {
        this.router.navigate(['home', 'custom-solar', 'viewPallet', th.parent_id !== null && th.parent_id !== undefined ? th.parent_id : th.id]);
    }
}