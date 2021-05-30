import { DatePipe } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { Component, Inject, LOCALE_ID, OnInit } from "@angular/core";
import { MessageService } from "primeng/components/common/messageservice";
import { Observable, Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterAddress } from "../../shared/model/HunterAddress";
import { HunterDocument } from "../../shared/model/HunterDocument";


interface ProductionQuality {
    ordpro: string,
    ordstatus: string,
    start: Date,
    id: string,
    sku: string,
    prd: string,
    lot_id: string,
    manuf: Date,
    exp: Date,
    status: string,
    address: string,
    address_parent: string,
    qty: number,
    plt: number
}

interface ProductionRowQuality {
    id: string,
    ordstatus: string,
    sku: string,
    prd: string,
    lot_id: string,
    manuf: Date,
    exp: Date,
    status: string,
    address: string,
    cap: number,
    plt: number
}

@Component({
    selector: 'production-quality',
    templateUrl: 'production-quality.component.html',
    styleUrls: ['production-quality.component.scss']
})
export class ProductionQualityComponent implements OnInit {
    private restSubscription: Subscription;

    protected datePipe: DatePipe;

    displayDialog: boolean;
    dataLoaded: boolean;
    prods: HunterDocument[];
    data: ProductionQuality[];
    rows: ProductionRowQuality[];
    selectedRows: ProductionRowQuality[];
    colsRows: ReportColumn[] = [
        {
            field: 'sku',
            header: 'CÓDIGO',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'prd',
            header: 'PRODUTO',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'lot_id',
            header: 'LOTE',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'manuf',
            header: 'FABRICAÇÃO',
            type: 'DATE',
            nullString: ''
        },
        {
            field: 'exp',
            header: 'VALIDADE',
            type: 'DATE',
            nullString: ''
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'address',
            header: 'LOCAL',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'cap',
            header: 'CAPACIDADE',
            type: 'NUMBER',
            nullString: ''
        },
        {
            field: 'plt',
            header: 'PALETES',
            type: 'NUMBER',
            nullString: ''
        }
    ];
    cols: ReportColumn[] = [
        {
            field: 'ordpro',
            header: 'ORDEM',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'start',
            header: 'INÍCIO',
            type: 'TIMESTAMP',
            nullString: ''
        },
        {
            field: 'sku',
            header: 'CÓDIGO',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'prd',
            header: 'PRODUTO',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'lot_id',
            header: 'LOTE',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'manuf',
            header: 'FABRICAÇÃO',
            type: 'DATE',
            nullString: ''
        },
        {
            field: 'exp',
            header: 'VALIDADE',
            type: 'DATE',
            nullString: ''
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'address',
            header: 'LOCAL',
            type: 'TEXT',
            nullString: ''
        },
        {
            field: 'qty',
            header: 'CXF',
            type: 'NUMBER',
            nullString: ''
        }
    ];

    constructor(private msgSvc: MessageService, private http: HttpClient, @Inject(LOCALE_ID) protected locale: string) {
        this.datePipe = new DatePipe(locale);
    }

    ngOnInit(): void {
        // let url1 = this.http.get(environment.customserver + '/wms/production');
        // let url2 = this.http.get(environment.customserver + '');

        // Observable.forkJoin([url1, url2]);
        this.loadData('2021-03-13');
    }

    loadData(date: string): void {
        let rowMap = new Map<string, ProductionQuality>();
        this.dataLoaded = false;
        this.data = undefined;
        this.restSubscription = this.http.get(environment.processserver + 'document/byTypeFrom/ORDPROD/' + this.datePipe.transform(new Date(date), 'yyyy-MM-dd HH:mm:ss.SSS'))
            .catch((err, caught) => {
                console.log('Call Error', err);
                console.log('Call Caught', caught);
                return Observable.empty();
            })
            .subscribe((docs: HunterDocument[]) => {
                let addrPrnts: string[] = Array.of(...[]);

                this.prods = docs;
                this.data = Array.of(...[]);
                this.rows = Array.of(...[]);
                docs.map(x => Object.assign({}, x))//DEEP COPY
                    .forEach(d => {
                        console.log('Doc', d);
                        for (let ds of d.siblings) {
                            if (ds.model.metaname === 'ORDCRIACAO' && ds.things.length > 0) {
                                let th = ds.things[0].thing.siblings[0];

                                if (th !== undefined) {
                                    let qtypr = th.properties.find(pr => pr.field.metaname === 'QUANTITY');
                                    let lotpr = th.properties.find(pr => pr.field.metaname === 'LOT_ID');
                                    let manufpr = th.properties.find(pr => pr.field.metaname === 'MANUFACTURING_BATCH');
                                    let exppr = th.properties.find(pr => pr.field.metaname === 'LOT_EXPIRE');

                                    if (th.address !== null && th.address.metaname.indexOf("PET") < 0 && th.address.metaname.indexOf("LATA") < 0 && th.address.metaname.indexOf("RGB") < 0 && !addrPrnts.includes(th.address.parent_id))
                                        addrPrnts.push(th.address.parent_id);
                                    this.data.push({
                                        ordpro: d.code,
                                        ordstatus: d.status,
                                        start: d.createdAt,
                                        id: th.id,
                                        sku: th.product.sku,
                                        prd: th.product.name,
                                        lot_id: lotpr.value,
                                        manuf: this.parseDate(manufpr.value),
                                        exp: this.parseDate(exppr.value),
                                        status: th.status,
                                        address: th.address === null ? '' : th.address.name,
                                        address_parent: th.address === null ? '' : th.address.parent_id,
                                        qty: +qtypr.value,
                                        plt: 1
                                    });
                                }
                            }
                        }
                    });
                this.restSubscription.unsubscribe();
                this.loadResume(addrPrnts);
            },
                error => {
                    console.log('subscription error', error);
                },
                () => {
                    console.log('Complete');
                });
    }
    loadResume(addrPrnts: string[]) {
        let urls = addrPrnts.map(p => this.http.get(environment.processserver + 'address/' + p));

        Observable.forkJoin(urls).subscribe((parents: HunterAddress[]) => {
            parents.forEach(prnt => {
                let resume = this.data.map(x => Object.assign({}, x))
                    .filter(d => d.address_parent === prnt.id)
                    .reduce((p, c, i, a) => {
                        let sameAddr = a.filter(r => r.address_parent === c.address_parent);
                        let idx = a.length;

                        while (idx--) {
                            if (a[idx].address_parent === c.address_parent)
                                a.splice(idx, 1);
                        }
                        c.qty = sameAddr.map(r => r.qty).reduce((a, b) => a + b);
                        c.plt = sameAddr.map(r => r.plt).reduce((a, b) => a + b);
                        a.push(c);
                        return a;
                    }, Array.of(...[]));
                let agg = resume.find(r => r.address_parent === prnt.id);
                this.rows.push({
                    id: prnt.id,
                    ordstatus: agg.ordstatus,
                    sku: agg.sku,
                    prd: agg.prd,
                    lot_id: agg.lot_id,
                    manuf: agg.manuf,
                    exp: agg.exp,
                    status: agg.status,
                    address: prnt.name,
                    cap: +prnt.fields.find(af => af.model.metaname === 'CAPACITY').value,
                    plt: agg.plt
                });
            });
        },
            error => {
                console.log('forkjoin error', error);
            },
            () => {
                this.rows = this.rows.sort((a1, a2) => {
                    if (a1.manuf.getTime() != a2.manuf.getTime())
                        return a1.manuf.getTime() - a2.manuf.getTime();
                    else if (a1.sku !== a2.sku)
                        return a1.sku.localeCompare(a2.sku);
                    // else if (a1.address !== a2.address)
                    return a1.address.localeCompare(a2.address);
                });
                this.dataLoaded = true;
                console.log('Forkjoin Complete');
            });
    }

    parseDate(value1: string): Date {
        if (value1.match(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/))
            return new Date(value1.replace(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/, "$3-$2-$1 $4:$5:$6"));
        else if (value1.match(/(\d{2})\/(\d{2})\/(\d{4})/))
            return new Date(value1.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$3-$2-$1"));
        return new Date(value1);
    }

    calcStyle(rowData: ProductionRowQuality) {
        return (rowData.cap === rowData.plt) ? {} : { 'background-color': '#fff1ba', 'color': '#000000' };
    }

    unblockProducts() {
        let addrList: Array<string> = this.selectedRows.map(sr => sr.id);

        this.restSubscription = this.http.put(environment.customserver + 'wms/unblockproducts', addrList)
            .subscribe((result: any[]) => {
                this.selectedRows = this.selectedRows.map(cr => {
                    cr.lot_id = result.find(r => r.hasOwnProperty(cr.id))[cr.id];
                    cr.status = 'ARMAZENADO';
                    return cr;
                });
                this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: 'DESBLOQUEADO' });
            });
    }
}