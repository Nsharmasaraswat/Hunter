import { DatePipe } from "@angular/common";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Component, HostListener, Inject, LOCALE_ID, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Observable, Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DynamicReportTableComponent } from "../../report/components/dynamic-table.component";
import { Report, ReportColumn, ReportVariable } from "../../report/interfaces/report.interface";
import { HunterProduct } from "../../shared/model/HunterProduct";
import RestResponse from "../../shared/utils/restResponse";

interface expPlt {
    data_exp: Date,
    cliname: string,
    nf: string,
    qty: number
}

interface strPlt {
    addr: string,
    qty: number,
    status: string
}

@Component({
    templateUrl: './traceability.component.html'
})

export class TraceabilityComponent implements OnInit, OnDestroy {
    @ViewChild('tbl') dynTable: DynamicReportTableComponent;

    private routeSubscription: Subscription;
    private navigationSubscription: Subscription;
    private reportSubscription: Subscription;
    private updateTimer: any;

    protected datePipe: DatePipe;
    protected rowCount: number = 10;

    data: any[];
    dataStrRes: strPlt[];
    dataExpRes: expPlt[];
    ddPrd;
    selectedProduct: HunterProduct;
    selectedDate: Date;
    selectedLot: string;
    selectedReport: Report;
    dataLoaded: boolean;
    displayDialog: boolean;
    colsStored: ReportColumn[] = [
        {
            field: 'addr',
            header: 'LOCAL',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'qty',
            header: 'QUANTIDADE',
            type: 'NUMBER',
            nullString: ''
        }
    ];
    colsExp: ReportColumn[] = [
        {
            field: 'data_exp',
            header: 'DATA',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'cliname',
            header: 'CLIENTE',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'nf',
            header: 'NFs',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'qty',
            header: 'QUANTIDADE',
            type: 'NUMBER',
            nullString: ''
        }
    ];

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private route: ActivatedRoute, @Inject(LOCALE_ID) protected locale: string) {
        this.datePipe = new DatePipe(locale);
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.dataLoaded = false;
            if (window.innerHeight > 720)
                this.rowCount = 20;
            else if (window.innerHeight > 640)
                this.rowCount = 10;
            if (this.ddPrd === null || this.ddPrd === undefined || this.ddPrd.length === 0) {
                this.navigationSubscription = this.http.get(environment.processserver + 'product/bytypeandsiblings/PA', { responseType: 'json' })
                    .subscribe((products: HunterProduct[]) => {
                        this.ddPrd = products.map(p => {
                            let ubField = p.fields.find(pf => pf.model.metaname === 'UNIT_BOX');

                            return {
                                label: p.sku + ' - ' + p.name + (ubField === null || ubField === undefined ? '' : ' - C' + ubField.value),
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
            }
            this.reportSubscription = this.http.get(environment.reportserver + 'query/loadFile/traceability.json')
                .catch((err: HttpErrorResponse) => {
                    return Observable.of<RestResponse>({ status: { result: false, message: err.error }, data: [] });
                }).subscribe((resp: RestResponse) => {
                    if (resp.status.result) {
                        let variables: ReportVariable[] = resp.data['variables'];

                        this.selectedReport = {
                            file: 'traceability',
                            name: resp.data['name'],
                            query: resp.data['query'],
                            variables: variables.sort((a1: ReportVariable, a2: ReportVariable) => a1.type.localeCompare(a2.type)),
                            columns: resp.data['columns'],
                            actions: resp.data['actions']
                        };
                    } else {
                        console.log(resp.status.message);
                        this.msgSvc.add({ severity: 'error', summary: "Falha arquivo Json", detail: "Problema na consulta" })
                    }
                });
        });
    }

    ngOnDestroy(): void {
        this.unsubscribeObservables();
    }

    @HostListener('window:beforeunload', ['$event'])
    beforeUnloadHander(event) {
        console.log("beforeUnload");
        this.unsubscribeObservables();
    }


    unsubscribeObservables() {
        if (this.navigationSubscription != null)
            this.navigationSubscription.unsubscribe();
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
        if (this.reportSubscription != null)
            this.reportSubscription.unsubscribe();
    }

    formatLot(): void {
        if (this.selectedDate !== null && this.selectedDate !== undefined && this.selectedProduct !== null && this.selectedProduct !== undefined)
            this.selectedLot = "____" + this.datePipe.transform(this.selectedDate, 'ddMMyy') + this.selectedProduct.sku;
    }

    loadResult(): void {
        this.dataLoaded = false;
        if (this.reportSubscription != null)
            this.reportSubscription.unsubscribe();
        this.reportSubscription = this.http.get(environment.reportserver + 'query/byFile/traceability?lot=' + this.selectedLot)
            .catch((err: HttpErrorResponse) => {
                return Observable.of<RestResponse>({ status: { result: false, message: err.error }, data: [] });
            }).subscribe((resp: RestResponse) => {
                console.log('Result', resp);
                if (resp.status.result) {
                    // let strCp = resp.data.map(x => Object.assign({}, x))
                    //     .filter(d => d['LOCAL'] !== '-');
                    // let dataStored = Array.from(new Set(strCp.map(a => a.RUA)))
                    //     .map(id => {
                    //         return strCp.filter(a => a.RUA === id).reduce
                    //     });
                    let dataStored = resp.data.map(x => Object.assign({}, x))//DEEP COPY
                        .filter(d => d['RUA'] !== '-')
                        .reduce((p, c, i, a) => {
                            let sameAddr = a.filter(r => r['RUA'] === c['RUA']);
                            let idx = a.length;

                            while (idx--) {
                                if (a[idx]['RUA'] === c['RUA'])
                                    a.splice(idx, 1);
                            }
                            c['QTY'] = sameAddr.map(r => +r['QTY']).reduce((a, b) => a + b);
                            a.push(c);
                            return a;
                        }, Array.of(...[]));
                    let expCp = resp.data.map(x => Object.assign({}, x))
                        .filter(d => d['NFSAIDA'] !== '-');
                    let dataExp = Array.from(new Set(expCp.map(a => a.NFSAIDA)))
                        .map(id => {
                            return expCp.find(a => a.NFSAIDA === id)
                        });

                    this.selectedReport.columns = resp['columns']
                        .filter((c: ReportColumn) => c.field !== 'RUA')
                        .map(col => {
                            if (col.width === undefined || col.width === '')
                                col.width = (col.header.length + 7) + 'ch';//Accounts for the sortable icon and space between
                            if (col.min_width === undefined || col.min_width === '')
                                col.min_width = (col.header.length + 7) + 'ch';
                            return col;
                        });
                    this.selectedReport.query = resp['query'];
                    this.selectedReport.name = resp['name'];
                    this.data = resp.data;

                    this.dataStrRes = dataStored.map(c => {
                        let addr = c['RUA'];
                        let qty = +c['QTY'];
                        let status = c['STATUS'];
                        let plt: strPlt = { addr, qty, status };

                        return plt;
                    });
                    this.dataExpRes = dataExp.map(c => {
                        let data_exp = c['EXPEDICAO'];
                        let cliname = c['CLINAME'];
                        let nf = c['NFSAIDA'];
                        let qty = +c['EXPQTY'];
                        let plt: expPlt = { data_exp, cliname, nf, qty };

                        return plt;
                    });
                    this.dataLoaded = true;
                    this.updateTable();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO NA CONSULTA', detail: resp.status.message });
                }
            });
    }

    displayAll() {
        this.displayDialog = true;
        this.updateTable();
    }

    updateTable(): void {
        clearTimeout(this.updateTimer);
        this.updateTimer = setTimeout(() => {
            if (this.dynTable !== undefined) {
                this.dynTable.fixTableSize(window.innerHeight);
            }
        }, 500);
    }
}
