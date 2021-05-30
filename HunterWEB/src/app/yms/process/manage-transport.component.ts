import { DecimalPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { ConfirmationService } from "primeng/api";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { SummaryItem, TransportSummary } from "../../shared/classes/TransportSummary";
import { HunterUnitType } from '../../shared/model/enum/HunterUnitType';
import { HunterAddress } from '../../shared/model/HunterAddress';
import { HunterDocument } from "../../shared/model/HunterDocument";
import { HunterField } from '../../shared/model/HunterField';
import { HunterThing } from "../../shared/model/HunterThing";
import { HunterUnit } from '../../shared/model/HunterUnit';
import { HunterUser } from "../../shared/model/HunterUser";
import CPFCNPJ from "../../shared/utils/cpfcnpjutils";
import RestStatus from "../../shared/utils/restStatus";

class LeadTime {
    constructor(public event: string, public date: Date, public duration: string, public user: HunterUser, public total: boolean) {

    }
}

class StockDueDateStub {
    constructor(public sku: string, public name: string, public status: string, public addr: string, public man: Date, public exp: Date, public fab: number, public due: number, public count: number) {
    }
}

class HeaderModel {
    cnpj: string;
    custcode: string;
    transport: string;
    driver: string;
    supplier: string;
    truck: string;
    dock: string;
    obs: string;
    carrier: string;
    sealexp: string;
    sealgate: string;
    safetyout: string;
    extinguisherout: string;
    carrout: any;
    conesout: any;
    kmout: any;
    safetyin: string;
    extinguisherin: string;
    carrin: any;
    conesin: any;
    kmin: any;
    tracker: string;
    leftQty: string;
    rightQty: string;
}
@Component({
    templateUrl: './manage-transport.component.html',
    styles: [`
        .product-shortage {
            background-color: #FF4D40 !important;
            color: #FFFFFF !important;
        }
        .product-shortage-partial {
            background-color: #F2A2A2 !important;
            color: #FFFFFF !important;
        }
        .product-partial {
            background-color: #FFF1BA !important;
            color: #000000 !important;
        }
    `
    ]
})
export class ManageTransportComponent implements OnInit, OnDestroy {
    @ViewChild('txtCustCode') txtCustCode: ElementRef;
    @ViewChild('tId') txtDocCode: ElementRef;

    routeSubscription: Subscription;
    restSubscription: Subscription;
    transport: HunterDocument;
    prdShortage: HunterDocument;
    checkinportaria: HunterDocument;
    checkoutportaria: HunterDocument;
    apolacre: HunterDocument;
    nfe: HunterDocument[];
    nfs: HunterDocument[];
    movs: HunterDocument[];
    checks: HunterDocument[];
    picks: HunterDocument[];
    palletsRec: HunterThing[];
    palletsExp: HunterThing[];
    leadTimes: LeadTime[];
    summary: TransportSummary;
    rowGroupMetadata: any;
    changed: boolean = false;
    additive: boolean = false;
    btnNFEnabled: boolean = false;
    btnNFSnabled: boolean = false;
    detailProduct: boolean = false;
    detailVehicle: boolean = false;
    changeVehicle: boolean = false;
    editSeal: boolean = false;
    editObs: boolean = false;
    restrictedView: boolean = false;

    prdStock: StockDueDateStub[];
    selRow: SummaryItem;
    columnsStock: ReportColumn[] = [
        {
            field: 'sku',
            header: 'CÓDIGO',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        },
        {
            field: 'name',
            header: 'PRODUTO',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        },
        {
            field: 'addr',
            header: 'ENDEREÇO(s)',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        },
        {
            field: 'man',
            header: 'FABRICAÇÃO',
            type: 'DATE',
            nullString: '',
            width: '15em'
        },
        {
            field: 'exp',
            header: 'VENCIMENTO',
            type: 'DATE',
            nullString: '',
            width: '15em'
        },
        {
            field: 'fab',
            header: 'FABRICADO',
            type: 'NUMBER',
            nullString: '',
            width: '15em'
        },
        {
            field: 'due',
            header: 'VALIDADE',
            type: 'NUMBER',
            nullString: '',
            width: '15em'
        },
        {
            field: 'count',
            header: 'QUANTIDADE',
            type: 'NUMBER',
            nullString: '',
            width: '15em'
        }
    ];
    model: HeaderModel = new HeaderModel();

    constructor(private msgSvc: MessageService, private http: HttpClient, private route: ActivatedRoute, private confirmationService: ConfirmationService, private decPipe: DecimalPipe) {

    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(data => {
            if (data.docId !== undefined && data.docId !== 'ignore') {
                this.loadById(data.docId);
            }
            if (data.restricted !== undefined) {
                this.restrictedView = data.restricted === 'true';
            }
        });
    }

    ngOnDestroy() {
        if (this.restSubscription !== null && this.restSubscription !== undefined)
            this.restSubscription.unsubscribe();
        if (this.routeSubscription !== null && this.routeSubscription !== undefined)
            this.routeSubscription.unsubscribe();
    }

    loadByCode(code: string) {
        if (code.length <= 6 || code.indexOf('R') === 0) {
            while (code.length < 6) {
                code = '0' + code;
            }
            this.loadTransport(environment.processserver + 'document/bytypecode/TRANSPORT/' + code);
        } else {
            this.loadTransport(environment.customserver + 'yms/findDocumentBySAPTransport/' + code);
        }
    }

    loadById(id: string) {
        this.loadTransport(environment.processserver + 'document/' + id);
    }

    loadTransport(url: string) {
        this.restSubscription = this.http.get(url)
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            })
            .subscribe((transp: HunterDocument) => {
                if (transp !== null && transp.id !== null) {
                    this.build(transp);
                } else
                    this.msgSvc.add({ detail: "Verifique o código digitado", severity: "error", summary: "Transporte Inexistente" });
            });
    }

    build(transp: any) {
        let apochegada: HunterDocument = undefined;
        let apochamada: HunterDocument = undefined;
        let apochecklist: HunterDocument = undefined;
        let apoentrada: HunterDocument = undefined;
        let apodoca: HunterDocument = undefined;
        let apodescarga: HunterDocument = undefined;
        let apocarga: HunterDocument = undefined;
        let apoliberacao: HunterDocument = undefined;
        let aposaida: HunterDocument = undefined;
        let obsField: HunterField = transp.fields.find(df => df.field.metaname === 'OBS');
        let ordcri: HunterDocument[] = Array.from([]);

        this.apolacre = undefined;
        this.prdShortage = undefined;
        this.checkinportaria = undefined;
        this.nfe = Array.of(...[]);
        this.nfs = Array.of(...[]);
        this.movs = Array.of(...[]);
        this.checks = Array.of(...[]);
        this.picks = Array.of(...[]);
        this.palletsRec = Array.of(...[]);
        this.palletsExp = Array.of(...[]);
        this.leadTimes = Array.of(...[]);
        this.transport = new HunterDocument(transp);
        this.model = new HeaderModel();

        for (let s of this.transport.siblings) {
            switch (s.model.metaname) {
                case "APOCHEGADA":
                    apochegada = s;
                    break
                case "APOCHAMADA":
                    apochamada = s;
                    break;
                case "APOCHECKLIST":
                    apochecklist = s;
                    this.model.sealexp = s.fields.filter(df => df.field.metaname.indexOf('ATTLACRE') > -1 && df.value !== '').map(df => df.value).sort().join(',');
                    break;
                case 'CHECKINPORTARIA':
                    let attCarrinhoIn = s.fields.find(df => df.field.metaname === 'ATTCARRINHO');
                    let attConesIn = s.fields.find(df => df.field.metaname === 'ATTCONE');
                    let attKmIn = s.fields.find(df => df.field.metaname === 'ATTKILOMETRAGEM');
                    let attExtIn = s.fields.find(df => df.field.metaname === 'ATTEXTINTOR');
                    let attSftIn = s.fields.find(df => df.field.metaname === 'ATTITENSEGUR');

                    this.checkinportaria = s;
                    this.model.carrin = attCarrinhoIn !== undefined ? +attCarrinhoIn.value : '';
                    this.model.conesin = attConesIn !== undefined ? +attConesIn.value : '';
                    this.model.kmin = attKmIn !== undefined ? +attKmIn.value : '';
                    this.model.extinguisherin = attExtIn !== undefined ? (attExtIn.value.toLowerCase() === 'true' ? 'SIM' : 'NÃO') : '';
                    this.model.safetyin = attSftIn !== undefined ? (attSftIn.value.toLowerCase() === 'true' ? 'SIM' : 'NÃO') : '';
                    break;
                case "CHECKOUTPORTARIA":
                    let attCarrinhoOut = s.fields.find(df => df.field.metaname === 'ATTCARRINHO');
                    let attConesOut = s.fields.find(df => df.field.metaname === 'ATTCONE');
                    let attKmOut = s.fields.find(df => df.field.metaname === 'ATTKILOMETRAGEM');
                    let attExtOut = s.fields.find(df => df.field.metaname === 'ATTEXTINTOR');
                    let attSftOut = s.fields.find(df => df.field.metaname === 'ATTITENSEGUR');

                    this.checkoutportaria = s;
                    this.model.sealgate = s.fields.filter(df => df.field.metaname.indexOf('ATTLACRE') > -1 && df.value !== '').map(df => df.value).sort().join(',');
                    this.model.carrout = attCarrinhoOut !== undefined ? +attCarrinhoOut.value : '';
                    this.model.conesout = attConesOut !== undefined ? +attConesOut.value : '';
                    this.model.kmout = attKmOut !== undefined ? +attKmOut.value : '';
                    this.model.extinguisherout = attExtOut !== undefined ? (attExtOut.value.toLowerCase() === 'true' ? 'SIM' : 'NÃO') : '';
                    this.model.safetyout = attSftOut !== undefined ? (attSftOut.value.toLowerCase() === 'true' ? 'SIM' : 'NÃO') : '';
                    break;
                case "APOENTRADA":
                    apoentrada = s;
                    break;
                case "APODOCA":
                    apodoca = s;
                    break;
                case "APODESCARGA":
                    apodescarga = s;
                    break;
                case "APOCARGA":
                    apocarga = s;
                    break;
                case "APOLACRE":
                    this.apolacre = s;
                    this.model.sealexp = s.fields.filter(df => df.field.metaname.indexOf('ATTLACRE') > -1 && df.value !== '').map(df => df.value).sort().join(',');
                case "APOLIBERACAO":
                    apoliberacao = s;
                    break;
                case "APOSAIDA":
                    aposaida = s;
                case "ORDCRIACAO":
                    ordcri.push(s);
                    this.movs.push(...s.siblings.filter(ds => ds.model.metaname === 'ORDMOV'));
                    break;
                case "NFSAIDA":
                    this.nfs.push(s);
                    break;
                case "NFENTRADA":
                    this.nfe.push(s);
                    break;
                case "PRDSHORTAGE":
                    this.prdShortage = s;
                    break;
                case "ORDMOV":
                    this.movs.push(s);
                    break;
                case "ORDCONF":
                    this.checks.push(s);
                    break;
                case "PICKING":
                    this.movs.push(...s.siblings.filter(ds => ds.model.metaname === 'ORDMOV'));
                    if (s.siblings.find(ds => ds.model.metaname === 'OSG') != undefined)
                        this.picks.push(s);
                    for (let sib of s.siblings) {
                        if (sib.model.metaname === 'ORDCONF')
                            this.checks.push(sib);
                        if (sib.model.metaname === 'OSG')
                            this.palletsExp = this.palletsExp.concat(sib.things.map(dt => dt.thing));
                    }
                    break;
            }
        }
        let truck = this.transport.things.map(dt => dt.thing).find(t => t.product.model.metaname === 'TRUCK');
        let truckCode = truck === undefined ? undefined : truck.properties.find(pr => pr.field.metaname === 'CODE');
        let driver = this.transport.person;
        let totalDuration: number = 0;

        if (apochecklist !== undefined && apochecklist.status === 'PREENCHIDO')
            this.model.tracker = apochecklist.fields.find(df => df.field.metaname === 'ATTRASTREADOR').value;
        else if (truck !== undefined) {
            this.http.get(environment.processserver + 'thing/fillunits/' + truck.id, { responseType: 'json' })
                .catch((err: HttpErrorResponse) => {
                    console.log('thingRest', err.message);
                    return Observable.empty();
                }).subscribe((t: HunterThing) => {
                    if (t != null && t !== undefined) {
                        truck = t;
                    }
                    let unit: HunterUnit = truck.unitModel.find(u => u.type === HunterUnitType.RTLS);
                    this.model.tracker = unit !== undefined ? unit.tagId : '';
                    console.log(truck);
                });
        }

        if (apochegada !== undefined)
            this.leadTimes.push(new LeadTime('Chegada', apochegada.createdAt, this.getTimeString(0), apochegada.user, false));
        if (apochamada !== undefined) {
            let duration: number = apochegada === undefined ? 0 : apochamada.createdAt.getTime() - apochegada.createdAt.getTime();

            totalDuration += duration;
            this.leadTimes.push(new LeadTime('Chamada', apochamada.createdAt, this.getTimeString(duration), apochamada.user, false));
        }
        if (apoentrada !== undefined) {
            let duration: number = apochamada === undefined ? 0 : apoentrada.createdAt.getTime() - apochamada.createdAt.getTime();

            totalDuration += duration;
            this.leadTimes.push(new LeadTime('Entrada', apoentrada.createdAt, this.getTimeString(duration), apoentrada.user, false));
        }
        if (apodoca !== undefined) {
            let duration: number = apoentrada === undefined ? 0 : apodoca.createdAt.getTime() - apoentrada.createdAt.getTime();

            totalDuration += duration;
            this.leadTimes.push(new LeadTime('Doca', apodoca.createdAt, this.getTimeString(duration), apodoca.user, false));
        }
        if (apodescarga !== undefined) {
            let duration: number = apodoca === undefined ? 0 : apodescarga.createdAt.getTime() - apodoca.createdAt.getTime();

            totalDuration += duration;
            this.leadTimes.push(new LeadTime('Descarga', apodescarga.createdAt, this.getTimeString(duration), apodescarga.user, false));
        }
        if (apocarga !== undefined) {
            let duration: number = 0;
            if (apodescarga === undefined) {
                if (apodoca !== undefined)
                    duration = apocarga.createdAt.getTime() - apodoca.createdAt.getTime()
            } else
                duration = apocarga.createdAt.getTime() - apodescarga.createdAt.getTime();

            totalDuration += duration;
            this.leadTimes.push(new LeadTime('Carga', apocarga.createdAt, this.getTimeString(duration), apocarga.user, false));
        }
        if (this.apolacre !== undefined) {
            let duration: number = 0;
            if (apocarga === undefined) {
                if (apodescarga === undefined) {
                    if (apodoca !== undefined)
                        duration = this.apolacre.updatedAt.getTime() - apodoca.createdAt.getTime()
                } else
                    duration = this.apolacre.updatedAt.getTime() - apodescarga.createdAt.getTime()
            } else
                duration = this.apolacre.updatedAt.getTime() - apocarga.createdAt.getTime();

            totalDuration += duration;
            this.leadTimes.push(new LeadTime('Lacre', this.apolacre.updatedAt, this.getTimeString(duration), this.apolacre.user, false));
        }
        if (apoliberacao !== undefined) {
            let duration: number = 0;

            if (this.apolacre === undefined) {
                if (apocarga === undefined) {
                    if (apodescarga !== undefined)
                        duration = apoliberacao.createdAt.getTime() - apodescarga.createdAt.getTime();
                } else
                    duration = apoliberacao.createdAt.getTime() - apocarga.createdAt.getTime();
            } else
                duration = apoliberacao.createdAt.getTime() - this.apolacre.updatedAt.getTime();

            totalDuration += duration;
            this.leadTimes.push(new LeadTime('Liberação', apoliberacao.createdAt, this.getTimeString(duration), apoliberacao.user, false));
        }
        if (aposaida !== undefined) {
            let duration: number = apoliberacao === undefined ? 0 : aposaida.createdAt.getTime() - apoliberacao.createdAt.getTime();

            totalDuration += duration;
            this.leadTimes.push(new LeadTime('Saída', aposaida.createdAt, this.getTimeString(duration), aposaida.user, false));
        }
        this.leadTimes.push(new LeadTime('TOTAL', null, this.getTimeString(totalDuration), new HunterUser(""), true));
        let dockField = this.transport.fields.find(f => f.field.metaname === 'DOCK');

        if (dockField !== undefined && dockField.value.length > 0) {
            this.restSubscription = this.http.get(environment.processserver + 'address/' + dockField.value, { responseType: 'json' })
                .catch((err: HttpErrorResponse) => {
                    return Observable.empty();
                })
                .subscribe((address: HunterAddress) => {
                    this.model.dock = address.name;
                });
        } else
            this.model.dock = 'N/D';
        let sortFunc = (a: HunterThing, b: HunterThing) => {
            if ((a.siblings === undefined) && (b.siblings === undefined)) return 0;
            let prdA = a.siblings.length === 0 ? a.product : a.siblings[0].product;
            let prdB = b.siblings.length === 0 ? b.product : b.siblings[0].product;
            if (prdA.sku !== prdB.sku) return prdA.sku.localeCompare(prdB.sku);
            let addrA = a.address;
            let addrB = b.address;
            if (addrA === undefined && addrB === undefined) return 0;
            if (addrB === undefined) return 1;
            if (addrA === undefined) return -1;
            return addrA.metaname.localeCompare(addrB.metaname);
        };

        this.txtDocCode.nativeElement.value = this.transport.code;
        this.checks.sort((a: HunterDocument, b: HunterDocument) => a.code.localeCompare(b.code));
        this.picks.sort((a: HunterDocument, b: HunterDocument) => a.code.localeCompare(b.code));
        this.movs.sort((a: HunterDocument, b: HunterDocument) => a.code.localeCompare(b.code));
        this.movs.forEach(ds => {
            let things = ds.things.map(dt => dt.thing);

            if (ds.code.startsWith('LDT') && ds.status !== 'CANCELADO')
                this.palletsExp = this.palletsExp.concat(things.filter(th => this.palletsExp.find(pal => pal.id === th.id) === undefined));
            else if (ds.code.startsWith('ROT') && ds.status !== 'CANCELADO')
                this.palletsExp = this.palletsExp.concat(things.filter(th => this.palletsExp.find(pal => pal.id === th.id) === undefined));
            else if (ds.code.startsWith('ULT') && ds.status !== 'CANCELADO')
                this.palletsRec = this.palletsRec.concat(things.filter(th => this.palletsRec.find(pal => pal.id === th.id) === undefined));
            else if (ds.code.startsWith('STR'))
                this.palletsRec = this.palletsRec.concat(things.filter(th => this.palletsRec.find(pal => pal.id === th.id) === undefined));
        });
        this.palletsRec = this.palletsRec.sort(sortFunc);
        this.palletsExp = this.palletsExp.sort(sortFunc);
        this.model.obs = obsField !== undefined ? obsField.value : '';
        this.model.driver = driver !== undefined ? driver.code + ' - ' + driver.name : 'N/D';
        this.model.truck = truck !== undefined ? (truck.metaname !== undefined && truck.metaname !== null ? truck.metaname : truckCode.value) + ' - ' + truck.name : 'N/D';
        this.model.carrier = truck !== undefined ? truck.properties.find(pr => pr.field.metaname === 'CARRIER').value : 'SOLAR BR';
        this.model.leftQty = truck !== undefined ? truck.properties.find(pr => pr.field.metaname === 'LEFT_SIDE_QUANTITY').value : '0';
        this.model.rightQty = truck !== undefined ? truck.properties.find(pr => pr.field.metaname === 'RIGHT_SIDE_QUANTITY').value : '0';
        this.summary = new TransportSummary(this.transport);
    }

    showChangeVehicle(): boolean {
        return this.transport != null && this.transport.fields.find(df => df.field.metaname === 'SERVICE_TYPE' && df.value === 'ROTA') !== undefined;
    }

    selectVehicle(truck: HunterThing) {
        this.changeVehicle = false;
        this.restSubscription = this.http.put(environment.customserver + 'yms/changeTruck/' + this.transport.id + '/' + truck.id, { responseType: 'json' })
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            })
            .subscribe((changed: RestStatus) => {
                if (changed.result) {
                    let truckCode = truck === undefined ? undefined : truck.properties.find(pr => pr.field.metaname === 'CODE');
                    this.model.truck = truck !== undefined ? (truck.metaname !== undefined && truck.metaname !== null ? truck.metaname : truckCode.value) + ' - ' + truck.name : 'N/D';
                    this.model.carrier = truck !== undefined ? truck.properties.find(pr => pr.field.metaname === 'CARRIER').value : 'SOLAR BR';
                    this.model.leftQty = truck !== undefined ? truck.properties.find(pr => pr.field.metaname === 'LEFT_SIDE_QUANTITY').value : '0';
                    this.model.rightQty = truck !== undefined ? truck.properties.find(pr => pr.field.metaname === 'RIGHT_SIDE_QUANTITY').value : '0';
                    this.msgSvc.add({ severity: "success", summary: "Veículo Alterado com Sucesso", detail: "Novo Veículo " + truck.name });
                } else {
                    this.msgSvc.add({ severity: "error", summary: 'ERRO AO ALTERAR VEÍCULO', detail: changed.message });
                }
            }, (errmsg: HttpErrorResponse) => {
                console.log(errmsg);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ALTERAR VEÍCULO', detail: errmsg.error });
            },
                () => { });
    }

    selectNFs(ev: HunterDocument[]) {
        let current = this.summary.outboundPASummary.map(si => si.paletes).reduce((a: number, b: number) => a + b, 0);
        let cap = +this.model.leftQty + +this.model.rightQty;
        let paletes = 0;
        let cc = this.model.custcode;

        this.model.custcode = '';
        this.model.cnpj = '';
        this.restSubscription = Observable.from(ev)
            .concatMap(nf => {
                console.log('Get NF ', nf.code);
                return this.http.get(environment.processserver + 'document/' + nf.id, { responseType: 'json' })
                    .catch((err: HttpErrorResponse) => {
                        console.log('Error', err);
                        return Observable.empty();
                    }).concatMap((nfs: HunterDocument) => {
                        console.log('Send NF ', nfs.code);
                        if (nfs.model.metaname === 'NFSAIDA') {
                            if (nfs !== null && nfs.id !== null) {
                                paletes += TransportSummary.calcPAPallets(nfs);
                                if (Math.floor(paletes + current) <= cap) {
                                    return this.sendNFs(nfs);
                                } else {
                                    this.confirmationService.confirm({
                                        message: 'Verifique a capacidade do caminhão (' + cap + ') ou a quantidade de itens na(s) Nota(s) Fiscal(is) (' + this.decPipe.transform(paletes, '0.2-2') + '). Já utilizado: ' + this.decPipe.transform(current, '0.2-2'),
                                        accept: () => {
                                            return this.sendNFs(nfs);
                                        },
                                        reject: () => {
                                            this.model.custcode = cc;
                                            return Observable.empty();
                                        }
                                    });
                                }
                            }
                        } else {
                            return this.sendNFs(nfs);
                        }
                    });
            }).subscribe((transp: HunterDocument) => {
                console.log('Subscribe');
                if (transp !== null && transp.id !== null) {
                    this.loadById(transp.id);
                    this.msgSvc.add({ severity: "success", summary: "Nota Fiscal Adicionada com Sucesso", detail: "Pressione a lupa para recarregar o transporte" });
                } else
                    this.msgSvc.add({ severity: "error", summary: "Movimentação em Execução", detail: "Marque a opção de aditivo" });
            });
    }

    async sendNFs(nf: HunterDocument): Promise<Object | HunterDocument> {
        let method = this.additive ? 'additiveNF' : 'addNF';

        return await this.http.put(environment.customserver + 'document/' + method + '/' + this.transport.id, { 'child-id': nf.id }, { responseType: 'json' })
            .timeout(36000000)
            .retry(0)
            .catch((err: HttpErrorResponse) => {
                console.log('Error', err);
                return Observable.empty();
            }).toPromise();
    }

    cancelSelect() {
        this.model.cnpj = undefined;
        this.model.custcode = undefined;
        this.checkCNPJ(undefined);
        this.checkCustCode(undefined);
        this.changeVehicle = false;
    }

    removeNF(nfToRem: HunterDocument) {
        let nfId = nfToRem.id;

        if (nfToRem.metaname === 'NFENTRADA')
            this.nfe.splice(this.nfe.indexOf(this.nfe.find(nf => nf.id === nfId)), 1);
        else if (nfToRem.metaname === 'NFSAIDA')
            this.nfs.splice(this.nfs.indexOf(this.nfs.find(nf => nf.id === nfId)), 1);

        this.restSubscription = this.http.put(environment.customserver + 'document/remNF/' + this.transport.id, { 'child-id': nfId }, { responseType: 'json' })
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            })
            .subscribe((transp: HunterDocument) => {
                if (transp !== null && transp.id !== null) {
                    this.loadById(this.transport.id);
                    this.msgSvc.add({ detail: "Nota Fiscal Removida com Sucesso", severity: "success", summary: "Transporte Recarregado" });
                } else
                    this.msgSvc.add({ detail: "Verifique o código do transporte", severity: "error", summary: "Transporte Inexistente" });
            });
    }

    checkCNPJ(code) {
        if (code !== undefined) {
            let valid = CPFCNPJ.validateCNPJ(code);

            this.btnNFEnabled = valid;
            if (!this.btnNFEnabled)
                this.msgSvc.add({ severity: 'error', summary: 'CNPJ INVÁLIDO', detail: 'Digite um CNPJ' });
        } else {
            this.btnNFEnabled = false;
        }
    }

    trimCustCode(code) {
        if (code.length > 0) {
            while (code.indexOf('0') === 0)
                code = code.substring(1);
            this.btnNFSnabled = false
        }
    }

    custCodeChanged() {
        if (this.txtCustCode.nativeElement.value === '')
            this.model.custcode = '';
        else if (this.txtCustCode.nativeElement.value.length === 10)
            this.checkCustCode(this.txtCustCode.nativeElement.value);
    }

    checkLoadProduct(out: SummaryItem): boolean {
        if (out.prdshrt && out.prdshrtqty > 0)
            return true;
        let prdQty = this.calcPrdQty(out.id);

        return prdQty < out.paletes;
    }

    calcPrdQty(prdId: string): number {
        let movsldt: HunterDocument[] = this.movs.filter(mov => mov.fields.find(df => df.field.metaname === 'MOV_TYPE' && df.value === 'LOAD') !== undefined);
        let things: HunterThing[] = movsldt.flatMap(m => m.transports).map(dtr => dtr.thing);

        return things.filter(th => th.siblings.filter(ts => ts.product.id === prdId).length > 0).length;
    }

    checkCustCode(code) {
        if (code !== undefined && code.length > 0) {
            if (code.length < 10) {
                for (let i = code.length; i < 10; i++) {
                    this.txtCustCode.nativeElement.value = '0' + this.txtCustCode.nativeElement.value;
                }
            }
            this.btnNFSnabled = true;
        } else
            this.btnNFSnabled = false
    }

    getSupplier(nf: HunterDocument) {
        return CPFCNPJ.format(nf.person.code) + ' - ' + nf.person.name;
    }

    fixRule() {
        this.http.put(environment.customserver + 'wms/buildnewrule/' + this.transport.id, null, { responseType: 'json' })
            .subscribe((res: RestStatus) => {
                if (res.result) {
                    this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: "Documento Reavaliado" });
                    this.ngOnInit();
                } else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO', detail: res.message });
            }, (errmsg: HttpErrorResponse) => {
                console.log(errmsg);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO REAVALIAR DOCUMENTO', detail: errmsg.error });
            });
    }

    addProductToMov(product_id: string, summaryCount: number): void {
        let movsPrd = this.calcPrdQty(product_id)
        let quantity: number = Math.ceil(summaryCount - movsPrd);

        this.restSubscription = this.http.put(environment.customserver + 'wms/addmovpallet/' + this.transport.id + '/' + product_id + '/' + Math.round(quantity), '', { responseType: 'json' })
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            })
            .subscribe((resp: RestStatus) => {
                if (resp.result) {
                    this.loadById(this.transport.id);
                    this.msgSvc.add({ detail: "Produto Adicionado com Sucesso", severity: "success", summary: "Transporte Recarregado" });
                } else
                    this.msgSvc.add({ detail: resp.message, severity: "error", summary: "Erro ao Adicionar Paletes" });
            });
    }

    addProductToTnp(product_id: string, summaryCount: number): void {
        let movsPrd = this.calcPrdQty(product_id)
        let quantity: number = Math.ceil(summaryCount - movsPrd);

        this.restSubscription = this.http.put(environment.customserver + 'wms/addtnppallet/' + this.transport.id + '/' + product_id + '/' + Math.round(quantity), '', { responseType: 'json' })
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            })
            .subscribe((resp: RestStatus) => {
                if (resp) {
                    this.loadById(this.transport.id);
                    this.msgSvc.add({ detail: "Produto Adicionado com Sucesso", severity: "success", summary: "Recarregando Transporte" });
                } else
                    this.msgSvc.add({ detail: resp.message, severity: "error", summary: "Erro ao Adicionar Produto" });
            });
    }

    detailStockProduct(row: SummaryItem): void {
        this.selRow = row;
        this.restSubscription = this.http.get(environment.customserver + 'wms/stockbyproduct/' + row.id, { responseType: 'json' })
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            })
            .subscribe((stk: StockDueDateStub[]) => {
                this.prdStock = stk;
                this.detailProduct = true;
            });
    }

    resendDocument() {
        if (this.transport.status === "CAMINHAO NA PORTARIA" || this.transport.status === "CAMINHAO NA ENTRADA" || this.transport.status === "CAMINHAO NO PATIO") {
            this.http.put(environment.customserver + 'wms/reevaluatetransportrule/' + this.transport.id, '', { responseType: 'json' })
                .subscribe((res: RestStatus) => {
                    if (res.result) {
                        this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: "Documento Reavaliado" });
                        this.ngOnInit();
                    } else
                        this.msgSvc.add({ severity: 'error', summary: 'ERRO', detail: res.message });
                }, (errmsg: HttpErrorResponse) => {
                    console.log(errmsg);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO REAVALIAR DOCUMENTO', detail: errmsg.error });
                });
        } else {
            this.msgSvc.add({ severity: 'error', summary: 'ERRO AO REAVALIAR DOCUMENTO', detail: 'Status não permite reavaliação (' + this.transport.status + '). Utilizar Sumário' });
        }
    }

    getTimeString = (ms, sep = ':') => {
        const sign = ~~ms < 0 ? '-' : '';
        const absMs = Math.abs(~~ms);
        const [h, m, s] = [1000 * 60 * 60, 1000 * 60, 1000].map(calcMs => ('0' + ~~((absMs / calcMs) % 60)).substr(-2));
        return `${sign}${parseInt(h, 10) ? `${h}${sep}` : `00${sep}`}${m}${sep}${s}`;
    }

    changeObs() {
        this.msgSvc.add({ severity: 'error', summary: 'Não Disponível', detail: 'Função em desenvolvimento' });
        this.editObs = false;
    }

    changeSeal() {
        this.editSeal = false;
        this.restSubscription = this.http.put(environment.customserver + 'yms/changeTransportSeals/' + this.transport.id + '/' + this.model.sealexp, { responseType: 'json' })
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            })
            .subscribe((changed: RestStatus) => {
                if (changed.result) {
                    this.msgSvc.add({ severity: "success", summary: "Lacres Alterados com Sucesso", detail: "Novos Lacres " + this.model.sealexp });
                } else {
                    this.msgSvc.add({ severity: "error", summary: 'ERRO AO ALTERAR LACRES', detail: changed.message });
                }
            }, (errmsg: HttpErrorResponse) => {
                console.log(errmsg);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ALTERAR LACRES', detail: errmsg.error });
            },
                () => { });
    }

    getMigo(nf: HunterDocument): string {
        let migoField = nf.fields.find(df => df.field.metaname === 'DOC_MIGO');

        return (migoField === null || migoField === undefined) ? '' : migoField.value;
    }
}