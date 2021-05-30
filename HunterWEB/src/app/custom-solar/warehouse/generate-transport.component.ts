import { DatePipe } from '@angular/common';
import { HttpClient } from "@angular/common/http";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterProduct } from '../../shared/model/HunterProduct';
import { HunterThing } from "../../shared/model/HunterThing";
import { SocketService } from '../../shared/services/socket.service';
import RestStatus from '../../shared/utils/restStatus';

class ThingStub {
    constructor(public id: string, public addr_name: string, public sku, public name: string, public status: string, public lot: string, public quantity: number, public man: Date, public exp: Date, public allocation: number, public enableActions: boolean, public thing: HunterThing) {
    }
}

class AddressOcupationStub {
    public ocupation: number;
    public relOcupation: number;

    constructor(public address_id: string, public name: string, public status: string, public products: string, public capacity: number, public free: number) {
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
    }
}

@Component({
    selector: 'generate-transport',
    templateUrl: 'generate-transport.component.html',
    styleUrls: ['manage-address.component.scss'],
    providers: [DatePipe]
})

export class GenerateTransportComponent implements OnInit, OnDestroy {
    private routeSubscription: Subscription;

    selAddressSrc: AddressOcupationStub;
    selAddressDest: AddressOcupationStub;
    addressList: AddressOcupationStub[] = [];
    thingList: ThingStub[] = [];
    detAddress: AddressOcupationStub;
    selProd: HunterProduct;
    mvQty = 0;
    displayDialog: boolean;
    colsAddress: ReportColumn[] = [
        {
            field: 'name',
            header: 'NOME',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        },
        {
            field: 'products',
            header: 'PRODUTOS',
            type: 'TEXT',
            nullString: '',
            width: '40em'
        },
        {
            field: 'ocupation',
            header: 'OCUP.',
            type: 'NUMBER',
            nullString: '',
            width: '1em'
        },
        {
            field: 'free',
            header: 'LIVRE',
            type: 'NUMBER',
            nullString: '',
            width: '1em'
        }
    ];
    colsThing: ReportColumn[] = [
        {
            field: 'addr_name',
            header: 'POSIÇÃO',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'sku',
            header: 'CÓD.',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'name',
            header: 'NOME',
            type: 'TEXT',
            nullString: '',
            width: '50%'
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'quantity',
            header: 'QTD.',
            type: 'NUMBER',
            nullString: '',
            width: '10%'
        },
        {
            field: 'lot',
            header: 'LOTE',
            type: 'TEXT',
            nullString: '',
            width: '20%'
        },
        {
            field: 'man',
            header: 'FAB.',
            type: 'DATE',
            nullString: '',
            width: '10%'
        },
        {
            field: 'exp',
            header: 'VENC.',
            type: 'DATE',
            nullString: '',
            width: '10%'
        }
    ];

    rowOptionsThing: number[] = [5, 10, 15, 25, 50];

    constructor(private route: ActivatedRoute, private http: HttpClient, private socket: SocketService,
        private msgSvc: MessageService, private router: Router, private datePipe: DatePipe) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.addressList = Array.of(...[]);
            this.loadAddresses();
        });
    }

    ngOnDestroy(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
    }

    detail(address: AddressOcupationStub) {
        this.detAddress = address;
        this.http.get(environment.customserver + 'wms/stkAddressList/' + address.address_id)
            .subscribe((things: HunterThing[]) => {
                let tzOffset = new Date().getTimezoneOffset() * 60 * 1000;
                this.thingList = things
                    .filter(th => th.properties.map(pr => pr.field).find(prmf => prmf.metaname === 'QUANTITY') !== undefined)
                    .map(th => {
                        let lotField = th.properties.map(pr => pr.field).find(prmf => prmf.metaname === 'LOT_ID');
                        let qtyField = th.properties.map(pr => pr.field).find(prmf => prmf.metaname === 'QUANTITY');
                        let manField = th.properties.map(pr => pr.field).find(amf => amf.metaname === 'MANUFACTURING_BATCH');
                        let expField = th.properties.map(pr => pr.field).find(amf => amf.metaname === 'LOT_EXPIRE');
                        let lot = th.siblings[0].properties.find(pr => pr.modelfield_id === lotField.id).value;
                        let qty = th.siblings.map(ths => +ths.properties.find(pr => pr.modelfield_id === qtyField.id).value).reduce((a, b) => a + b);
                        let prMan = th.siblings[0].properties.find(pr => pr.modelfield_id === manField.id);
                        let man = new Date(new Date(prMan.value).getTime() + tzOffset);
                        let prExp = th.siblings[0].properties.find(pr => pr.modelfield_id === expField.id);
                        let exp = new Date(new Date(prExp.value).getTime() + tzOffset);
                        let payload: any = (th.payload === null || th.payload === undefined) ? { allocation: 3 } : JSON.parse(th.payload);
                        let allocation = payload['allocation'];

                        if (qty !== undefined && man !== undefined && exp !== undefined)
                            return new ThingStub(th.siblings[0].id, th.address.name, th.siblings.map(th => th.product.sku).join(','), th.siblings.map(th => th.product.name).join(','), th.status, lot, qty, man, exp, allocation, allocation === 3, th.siblings[0]);
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

    loadAddresses() {
        this.addressList = Array.of(...[]);
        this.http.get(environment.customserver + 'wms/listStock/')
            .subscribe((addresses: AddressOcupationStub[]) => {
                this.addressList = addresses.map(aos => new AddressOcupationStub(aos.address_id, aos.name, aos.status, aos.products, aos.capacity, aos.free));
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ENDEREÇOS', detail: error });
            },
                () => console.log("Lodaded Addresses"));
    }

    transf() {
        if (this.mvQty > 0) {
            this.http.post(environment.customserver + 'wms/transportPallet/' + this.selAddressSrc.address_id + '/' + this.selAddressDest.address_id + '/' + this.mvQty, { responseType: 'json' })
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
        } else
            this.msgSvc.add({ severity: 'error', summary: 'QUANTIDADE DE PALLETS', detail: 'AJUSTE A QUANTIDADE DE PALLETS A SER MOVIMENTADA' });
    }
}