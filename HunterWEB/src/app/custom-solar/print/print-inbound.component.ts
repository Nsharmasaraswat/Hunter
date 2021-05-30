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
import { ConferenceProduct } from '../../shared/classes/ConferenceProduct';
import { LabelData } from '../classes/LabelData';

@Component({
    selector: 'print-inbound',
    templateUrl: 'print-inbound.component.html',
    styleUrls: ['print.component.scss'],
    providers: [DatePipe]
})
export class PrintInboundComponent implements OnInit, OnDestroy {
    private docId: string;
    private routeSubscription: Subscription;
    selConfPrd: ConferenceProduct;

    fieldTypes = HunterFieldType;
    deviceId: string;
    transport: HunterDocument;
    document: HunterDocument;

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
            width: '60%'
        },
        {
            field: 'qty',
            header: 'QUANTIDADE TOTAL',
            type: 'NUMBER',
            nullString: '',
            width: '10%'
        }, {
            field: 'measureUnit',
            header: 'UNIDADE',
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
        }, {
            field: 'lot_expire',
            header: 'VALIDADE',
            type: 'DATE',
            nullString: '',
            width: '25%'
        }, {
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
            this.http.get(environment.customserver + "document/grandparent/" + this.docId)
                .subscribe(
                    (data: HunterDocument) => {
                        console.log(data);
                        this.transport = data;
                        this.document = this.transport.siblings.find(s => s.metaname === 'ORDCONF').siblings.find(n => n.metaname === 'RETORDCONF' && n.status === 'QUALIDADE OK');
                        let apodoca = this.transport.siblings.find(s => s.model.metaname === 'APODOCA');
                        let addrId = apodoca === undefined || apodoca === null ? this.transport.fields.find(df=>df.field.metaname === 'DOCK').value : apodoca.fields.find(f => f !== null).value;
                        this.prdConf = ConferenceProduct.getPrdQCArray(this.document);
                        this.document.items.forEach(di => {
                            let prds = this.prdConf.filter(cp => cp.product.id === di.product.id);
                            if(prds !== undefined){//NF Sem Produto
                                if(this.items.find(it=>it.product.id === di.product.id) === undefined){
                                    console.log(di);
                                    this.items.push({
                                        product: di.product,
                                        sku: di.product.sku,
                                        prd: di.product.name,
                                        qty: +prds.map(po => po.quantity * po.volumes).reduce((previousValue: number, currentValue: number, currentIndex: number, qtdarr: number[]) => previousValue + currentValue, 0).toFixed(3),
                                        measureUnit: di.measureUnit
                                    });
                                }
                            } else 
                            this.msgSvc.add({severity:"warning",summary:'Item Não Consta na NF', detail:di.product.sku + di.product.name});
                        });
                        this.http.get(environment.processserver + "address/" + addrId)
                            .subscribe((addr: HunterAddress) => {
                                this.address = addr;
                            },
                                (error: Error) => {
                                    this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar endereço', detail: error.message });
                                },
                                () => { });
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
        this.labelData.reatividade = '';
        this.labelData.riscoAVida = '';
    }

    productUnselected(event) {
        this.confUnselected(event);
        this.product = null;
        this.printOrders = Array.from([]);
    }

    confSelected(event): void {
        let nf = this.transport.siblings.filter(s => s.model.metaname === 'NFENTRADA').filter(nf => nf.items[0].product.id = this.product.id).pop();

        this.selConfPrd = event.data;
        this.labelData.destino = this.address.name;
        this.labelData.fornecedor = nf.person.name;
        this.labelData.nfEntrada = nf.code;
        this.labelData.dtRecebimento = this.datePipe.transform(this.document.createdAt, "dd/MM/yyyy");
        this.labelData.sku = this.selConfPrd.product.sku;
        this.labelData.produto = this.selConfPrd.product.name;
        this.labelData.loteFornecedor = this.selConfPrd.lot_id;
        this.labelData.unidademedida = this.selConfPrd.measureUnit;
        this.labelData.qtdRecebimento = +this.printOrders.map(po => po.quantity * po.volumes).reduce((previousValue: number, currentValue: number, currentIndex: number, qtdarr: number[]) => previousValue + currentValue).toFixed(3);
        this.labelData.qtdVolume = this.selConfPrd.volumes;
        this.labelData.qtdPorVolume = this.selConfPrd.quantity;
        this.labelData.dtFabricacao = this.datePipe.transform(this.selConfPrd.manufacturing_batch, "dd/MM/yyyy");
        this.labelData.dtValidade = this.selConfPrd.lot_expire != 'Indeterminado' ? this.datePipe.transform(this.selConfPrd.lot_expire, "dd/MM/yyyy") : this.selConfPrd.lot_expire;
        this.labelData.qtdImpresso = this.selConfPrd.getPrintCount(this.document.things);
        this.labelData.recomendacoesEspeciais = '';
        this.labelData.inflamabilidade = '';
        this.labelData.loteInterno = this.selConfPrd.internal_lot;
        this.labelData.reatividade = '';
        this.labelData.riscoAVida = '';
        this.labelData.rodape = this.selConfPrd.rodape;
        this.loadDetails = true;
        this.updateProgress();
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
        pto.docname = this.transport.name;
        pto.properties = this.labelData;
        pto.sku = this.labelData.sku;
        this.printing = true;
        if (toPrint > 0) {
            let t = this.selConfPrd.filterThings(this.document.things)[0];

            pto.thing = t.id;
            console.log(pto);
            this.http.post(environment.processserver + 'print/single/' + this.address.id, pto)
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
            this.printProgress = 0;
        }
    }

    private updateProgress() {
        let totalLabels = this.prdConf.map(cp => cp.volumes).reduce((p: number, c: number) => c + p);
        let selLabels = this.document.things.filter(dt => dt.thing.units.length !== 0 && dt.thing.product.id === this.product.id).length;

        this.printProgress = eval(((selLabels / totalLabels) * 100).toFixed(2));
        if (this.printProgress >= 100) {
            this.router.navigate(['home'])
        }
    }
}