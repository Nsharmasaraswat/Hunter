import { DatePipe } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Rx';
import { environment } from '../../../environments/environment';
import { ReportColumn } from '../../report/interfaces/report.interface';
import { TokenService } from '../../security/services/token.service';
import { HunterProduct } from '../../shared/model/HunterProduct';
import { NavigationService } from '../../shared/services/navigation.service';
import { SocketService } from '../../shared/services/socket.service';
import RestStatus from '../../shared/utils/restStatus';

interface RncProduct {
    id: string;
    sku: string;
    name: string;
    lot_id: string;
    manuf: Date;
    exp: Date;
    rnc: string;
    qty: number;
    status: string;
    serial: string;
    address: string;
}

@Component({
    selector: 'create-rnc',
    templateUrl: 'create-rnc.component.html',
    styleUrls: ['create-rnc.component.scss']
})

export class CreateRNCComponent {
    routeSubscription: Subscription;

    ddPrd = [];
    selectedProduct: HunterProduct;

    prds: RncProduct[] = [];
    selectedPRDs: RncProduct[] = [];
    cols: ReportColumn[] = [
        {
            field: 'lot_id',
            header: 'LOTE',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'manuf',
            header: 'FABRICAÇÃO',
            type: 'DATE',
            nullString: '',
            width: '10%'
        },
        {
            field: 'exp',
            header: 'VALIDADE',
            type: 'DATE',
            nullString: '',
            width: '10%'
        },
        {
            field: 'qty',
            header: 'QUANTIDADE',
            type: 'NUMBER',
            nullString: '',
            width: '10%'
        },
        {
            field: 'rnc',
            header: 'RNC',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'serial',
            header: '#SÉRIE',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'address',
            header: 'LOCAL',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        }
    ];

    btnText: string = 'BLOQUEAR';
    rnc: string = '';
    totalQty: number = 0;

    constructor(private msgSvc: MessageService, private http: HttpClient, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.http.get(environment.processserver + 'product/bytype/' + routeParams.modPrd, { responseType: 'json' })
                .subscribe((products: HunterProduct[]) => {
                    this.ddPrd = products.map(p => {
                        return {
                            label: p.sku + ' - ' + p.name,
                            value: p
                        };
                    }).sort((a, b) => {
                        if (a.label < b.label) return -1;
                        if (a.label > b.label) return 1;
                        return 0;
                    });
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR DOCUMENTO', detail: error });
                });
        });
    }

    findThings(event): void {
        this.http.get(environment.processserver + 'ui/rnclist/' + this.selectedProduct.id, { responseType: 'json' })
            .subscribe((things: RncProduct[]) => {
                this.prds = things;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR DOCUMENTO', detail: error });
            });
    }

    selectionChanged(event): void {
        let tmp = this.selectedPRDs.filter(r => r.status === 'BLOQUEADO');

        if (tmp.length === 0) {
            this.btnText = 'BLOQUEAR';
        } else {
            let selRnc = event.data;
            this.selectedPRDs = Array.from(this.selectedPRDs.filter(r => r.rnc === selRnc.rnc));
            this.btnText = 'DESBLOQUEAR';
        }
        if (this.selectedPRDs.length > 0)
            this.totalQty = this.selectedPRDs.map(r => r.qty).reduce((p, c) => p + c);
        else
            this.totalQty = 0;
    }

    sendRNC() {
        let parms = new HttpParams().set('rnc', this.rnc);
        this.http.post(environment.customserver + 'document/qcblock/' + this.btnText, this.selectedPRDs.map(r => r.id), { params: parms })
            .catch(error => {
                console.error("error catched", error);
                return Observable.of({ result: false, message: "Error Value Emitted" });
            }).subscribe((resp: RestStatus) => {
                if (resp.result) {
                    this.selectedPRDs.forEach(r => {
                        r.status = this.btnText === 'BLOQUEAR' ? 'BLOQUEADO' : 'DESBLOQUEADO';
                        r.rnc = this.rnc;
                    });
                    this.msgSvc.add({ severity: 'success', summary: 'RNC Criado com sucesso', detail: 'Quantidade total:' + this.totalQty.toFixed(3) });
                    this.selectedPRDs = Array.from([]);
                    this.rnc = '';
                    this.totalQty = 0;
                } else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO SALVAR RNC', detail: resp.message });
            }, (error: RestStatus) => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO SALVAR RNC', detail: error.message });
            });
    }
}
