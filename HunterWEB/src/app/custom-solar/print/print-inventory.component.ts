import { DatePipe } from '@angular/common';
import { HttpClient } from "@angular/common/http";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { PrintTagOrder } from '../../shared/classes/PrintTagOrder';
import { HunterFieldType } from "../../shared/model/enum/HunterFieldType";
import { HunterAddress } from '../../shared/model/HunterAddress';
import { HunterDocument } from "../../shared/model/HunterDocument";
import { HunterProduct } from "../../shared/model/HunterProduct";
import { HunterThing } from "../../shared/model/HunterThing";
import { ConferenceProduct } from '../../shared/classes/ConferenceProduct';
import { LabelData } from '../classes/LabelData';

@Component({
    selector: 'print-inventory',
    templateUrl: 'print-inventory.component.html',
    styleUrls: ['print.component.scss'],
    providers: [DatePipe]
})

export class PrintInventoryComponent implements OnInit, OnDestroy {
    private docId: string;
    private routeSubscription: Subscription;
    selConfPrd: ConferenceProduct;

    fieldTypes = HunterFieldType;
    deviceId: string;
    inventory: HunterDocument;

    address: HunterAddress;

    product: HunterProduct;
    prdConf: ConferenceProduct[];
    printOrders: ConferenceProduct[];

    loadDetails: boolean = false;
    printing: boolean = false;
    printProgress: number;

    columns: ReportColumn[] = [
        {
            field: 'sku',
            header: 'CODIGO',
            type: '',
            nullString: '',
            width: '20%'
        },
        {
            field: 'prd',
            header: 'PRODUTO',
            type: '',
            nullString: '',
            width: '50%'
        },
        {
            field: 'qty',
            header: 'QUANTIDADE TOTAL',
            type: 'NUMBER',
            nullString: '',
            width: '10%'
        },
        {
            field: 'measureUnit',
            header: 'UNIDADE',
            type: '',
            nullString: '',
            width: '10%'
        },
        {
            field: 'toPrint',
            header: '%',
            type: '',
            nullString: '',
            width: '10%'
        }
    ];

    items: any[] = [];

    columnsConference: ReportColumn[] = [
        {
            field: 'lot_id',
            header: 'LOTE',
            type: '',
            nullString: '',
            width: '25%'
        },
        {
            field: 'manufacturing_batch',
            header: 'FABRICAÇÃO',
            type: 'DATE',
            nullString: '',
            width: '25%'
        },
        {
            field: 'lot_expire',
            header: 'VALIDADE',
            type: 'DATE',
            nullString: '',
            width: '25%'
        },
        {
            field: 'quantity',
            header: 'QUANTIDADE POR VOLUME',
            type: '',
            nullString: '',
            width: '25%'
        }
    ];


    labelData: LabelData = new LabelData();

    constructor(private router: Router, private route: ActivatedRoute, private http: HttpClient, private msgSvc: MessageService, private datePipe: DatePipe) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(lstPrm => {
            this.docId = lstPrm.docId;
            this.deviceId = lstPrm.devId;
            this.http.get(environment.processserver + "document/" + this.docId)
                .subscribe(
                    (data: HunterDocument) => {
                        console.log(data);
                        this.inventory = data;
                        this.prdConf = ConferenceProduct.getPrdInvArray(this.inventory)
                        let tList = this.inventory.things.map<HunterThing>(dt => dt.thing);
                        this.inventory.items.forEach(di => {
                            let prds = this.prdConf.filter(cp => cp.product.id === di.product.id);
                            let things = tList.filter(t => t.product.id === di.product.id);
                            if (this.items.find(it => it.product.id === di.product.id) === undefined) {
                                this.items.push({
                                    product: di.product,
                                    sku: di.product.sku,
                                    prd: di.product.name,
                                    qty: +prds.map(po => po.quantity * po.volumes).reduce((previousValue: number, currentValue: number) => previousValue + currentValue).toFixed(3),
                                    measureUnit: di.measureUnit,
                                    toPrint: +((things.filter(t => t.units.length !== 0).length / things.length) * 100).toFixed(2)
                                });
                            }
                        });
                    },
                    (error: Error) => {
                        this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar documento', detail: error.message });
                    },
                    () => { });
        });
    }

    ngOnDestroy(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
    }

    getProductDetails(event): void {
        this.product = event.data.product;
        this.printOrders = this.prdConf.filter(cp => cp.product.id === this.product.id);
        this.loadDetails = false;
        this.labelData.recomendacoesEspeciais = '';
        this.labelData.inflamabilidade = '';
        this.labelData.loteInterno = '';
        this.labelData.reatividade = '';
        this.labelData.riscoAVida = '';
        this.labelData.rodape = '';
    }

    productUnselected(event) {
        this.confUnselected(event);
        this.product = null;
        this.printOrders = Array.from([]);
    }

    confSelected(event): void {
        this.selConfPrd = event.data;
        let tList = this.selConfPrd.filterThingsNoQuantity(this.inventory.things);
        if (tList.length > 0) {
            let t = tList[0];
            this.labelData.destino = t.address.name;
            this.labelData.fornecedor = '';
            this.labelData.nfEntrada = '';
            this.labelData.dtRecebimento = this.datePipe.transform(this.inventory.createdAt, "dd/MM/yyyy");
            this.labelData.sku = this.selConfPrd.product.sku;
            this.labelData.produto = this.selConfPrd.product.name;
            this.labelData.loteFornecedor = this.selConfPrd.lot_id;
            this.labelData.unidademedida = this.selConfPrd.measureUnit;
            this.labelData.qtdRecebimento = +this.printOrders.map(po => po.quantity * po.volumes).reduce((previousValue: number, currentValue: number) => previousValue + currentValue).toFixed(3);
            this.labelData.qtdVolume = this.selConfPrd.volumes;
            this.labelData.qtdPorVolume = this.selConfPrd.quantity;
            this.labelData.dtFabricacao = this.datePipe.transform(this.selConfPrd.manufacturing_batch, "dd/MM/yyyy");
            this.labelData.dtValidade = this.selConfPrd.lot_expire != 'Indeterminado' ? this.datePipe.transform(this.selConfPrd.lot_expire, "dd/MM/yyyy") : this.selConfPrd.lot_expire;
            this.labelData.qtdImpresso = this.selConfPrd.getPrintCount(this.inventory.things);
            this.labelData.recomendacoesEspeciais = '';
            this.labelData.inflamabilidade = '';
            this.labelData.loteInterno = t.properties.find(pr => pr.field.metaname === 'INTERNAL_LOT').value;
            this.labelData.reatividade = '';
            this.labelData.riscoAVida = '';
            this.labelData.rodape = 'INVENTÁRIO';
            this.loadDetails = true;
            this.updateProgress();
        } else {
            this.msgSvc.add({ severity: 'success', summary: 'Nenhuma etiqueta para imprimir', detail: "Todas as etiquetas foram impressas" });
            this.loadDetails = false;
        }
    }

    confUnselected(event) {
        this.loadDetails = false;
        this.selConfPrd = null;
    }

    printTag() {
        //TODO: PrintWebSocket
        // this.stream = this.socket.connect(environment.wsprocess + 'origin/' + this.devId);

        // this.socketSubscription = this.stream.subscribe(
        //     (message: PrintTagOrder) => {
        //         console.log('received message from server: ', message);
        //     }
        // );
        let toPrint = this.labelData.qtdVolume - this.labelData.qtdImpresso;
        let pto: PrintTagOrder = new PrintTagOrder(this.deviceId, this.docId, this.product.id);

        if (this.labelData.recomendacoesEspeciais == null || this.labelData.recomendacoesEspeciais === undefined)
            this.labelData.recomendacoesEspeciais = '';
        if (this.labelData.reatividade == null || this.labelData.reatividade === undefined)
            this.labelData.reatividade = '';
        if (this.labelData.riscoAVida == null || this.labelData.riscoAVida === undefined)
            this.labelData.riscoAVida = '';
        if (this.labelData.rodape == null || this.labelData.rodape === undefined)
            this.labelData.rodape = '';
        if (this.labelData.loteInterno == null || this.labelData.loteInterno === undefined)
            this.labelData.loteInterno = '';
        if (this.labelData.inflamabilidade == null || this.labelData.inflamabilidade === undefined)
            this.labelData.inflamabilidade = '';
        pto.docname = this.inventory.name;
        pto.properties = this.labelData;
        pto.sku = this.labelData.sku;
        this.printing = true;
        if (toPrint > 0) {
            let t = this.selConfPrd.filterThingsNoQuantity(this.inventory.things)[0];

            pto.thing = t.id;
            console.log(pto);
            this.http.post(environment.processserver + 'print/single/' + t.address.id, pto)
                .subscribe((data: any) => {
                    if (data.result) {
                        this.labelData.qtdImpresso++;
                        t.units.push(JSON.parse(data.message));
                        this.updateProgress();
                        this.printTag();
                    } else {
                        this.msgSvc.add({ severity: 'error', summary: 'Não foi possível imprimir etiqueta', detail: data.message });
                        this.printing = false;
                    }
                },
                    (error: Error) => {
                        this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar documento', detail: error.message });
                        this.printing = false;
                    },
                    () => { this.printing = false; }
                );
        } else {
            this.msgSvc.add({ severity: 'success', summary: 'Nenhuma etiqueta para imprimir', detail: "Todas as etiquetas foram impressas" });
            this.printProgress = 100;
            this.loadDetails = false;
        }
    }

    private updateProgress() {
        this.printProgress = eval(((this.labelData.qtdImpresso / this.labelData.qtdVolume) * 100).toFixed(2));
    }
}