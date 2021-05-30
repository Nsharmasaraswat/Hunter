import { HttpClient } from "@angular/common/http";
import { AfterViewInit, Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterDocument } from "../../shared/model/HunterDocument";
import { SocketService } from "../../shared/services/socket.service";

interface StkSnapshot {
    address: string,
    count: number,
    date: Date,
    product: string,
    sku: string,
    type: string;
}

interface StkDiff {
    address: string,
    sku: string,
    product: string,
    countInv: number,
    countSnap: number,
    diff: number;
    diffPercent: number;
    classe: string;
}

interface StkDiffPrd {
    sku: string,
    product: string,
    countInv: number,
    countSnap: number,
    diff: number;
    diffPercent: number;
    classe: string;
}

interface StkResume {
    totalInv: number,
    totalSnap: number,
    totalDiff: number,
    totalDiffPercent: number,
}

@Component({
    selector: 'check-inventory',
    templateUrl: 'check-inventory.component.html',
    styleUrls: ['inventory.component.scss']
})
export class CheckInventoryComponent implements OnInit, OnDestroy, AfterViewInit {
    private routeSubscription: Subscription;

    type: string = 'ADDRESS';
    document: HunterDocument;
    selSib: HunterDocument;
    docsCount: HunterDocument[];
    snapshot: StkSnapshot[];
    inventory: StkSnapshot[];
    diffPrd: StkDiffPrd[];
    diff: StkDiff[];
    total: StkResume;

    columns: ReportColumn[] = [
        {
            field: "address",
            header: "ENDEREÇO",
            type: "TEXT",
            nullString: "-",
            width: "10%"
        },
        {
            field: "sku",
            header: "CÓDIGO",
            type: "TEXT",
            nullString: "-",
            width: "10%"
        },
        {
            field: "product",
            header: "PRODUTO",
            type: "TEXT",
            nullString: "-",
            width: "10%"
        },
        {
            field: "countSnap",
            header: "ESTOQUE",
            type: "NUMBER",
            nullString: "-",
            width: "10%"
        },
        {
            field: "countInv",
            header: "CONTADO",
            type: "NUMBER",
            nullString: "-",
            width: "10%"
        },
        {
            field: "diff",
            header: "DIFERENÇA",
            type: "NUMBER",
            nullString: "-",
            width: "10%"
        },
        {
            field: "diffPercent",
            header: "DIF(%)",
            type: "PERCENT",
            nullString: "0",
            width: "10%"
        }
    ];

    columnsPrd: ReportColumn[] = [
        {
            field: "sku",
            header: "CÓDIGO",
            type: "TEXT",
            nullString: "-",
            width: "10%"
        },
        {
            field: "product",
            header: "PRODUTO",
            type: "TEXT",
            nullString: "-",
            width: "10%"
        },
        {
            field: "countSnap",
            header: "ESTOQUE",
            type: "NUMBER",
            nullString: "-",
            width: "10%"
        },
        {
            field: "countInv",
            header: "CONTADO",
            type: "NUMBER",
            nullString: "-",
            width: "10%"
        },
        {
            field: "diff",
            header: "DIFERENÇA",
            type: "NUMBER",
            nullString: "-",
            width: "10%"
        },
        {
            field: "diffPercent",
            header: "DIF(%)",
            type: "PERCENT",
            nullString: "0",
            width: "10%"
        }
    ];

    columnsSiblings: ReportColumn[] = [
        {
            field: "code",
            header: "CÕDIGO",
            type: "TEXT",
            nullString: "-",
            width: "15%"
        },
        {
            field: "name",
            header: "NOME",
            type: "TEXT",
            nullString: "-",
            width: "55%"
        },
        {
            field: "createdAt",
            header: "DATA/HORA",
            type: "TIMESTAMP",
            nullString: "-",
            width: "30%"
        }
    ];

    constructor(private route: ActivatedRoute, private http: HttpClient, private socket: SocketService,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.http.get(environment.processserver + "document/" + routeParams.docId)
                .subscribe(
                    (data: HunterDocument) => {
                        console.log(data);
                        this.document = new HunterDocument(data);
                        if (this.document.siblings !== undefined) {
                            this.docsCount = Array.of(...[]);
                            this.docsCount = this.document.siblings.filter(ds => ds.metaname === 'APOCONTINV');
                        }
                        if (this.docsCount !== undefined && this.docsCount.length === 1) {
                            this.selSib = this.docsCount[0];
                            setTimeout(()=>this.selectSibling(this.selSib.id), 100);
                        }
                        this.http.get(environment.customserver + "wms/stksnapshot/" + routeParams.docId)
                            .subscribe(
                                (data: StkSnapshot[]) => {
                                    this.snapshot = data;
                                },
                                (error: Error) => {
                                    this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar documento', detail: error.message });
                                },
                                () => { }
                            );
                    },
                    (error: Error) => {
                        this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar documento', detail: error.message });
                    },
                    () => { }
                );
        });
    }

    selectSibling(docId: string): void {
        this.http.get(environment.customserver + "wms/stksnapshot/" + docId)
            .subscribe(
                (data: StkSnapshot[]) => {
                    this.inventory = data;
                    this.calcDiffs();
                },
                (error: Error) => {
                    this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar documento', detail: error.message });
                },
                () => { }
            );
    }

    calcDiffs() {
        let extra = this.inventory.filter(ss => this.snapshot.findIndex(sss => ss.address === sss.address) < 0);
        let extraPrd = this.inventory.filter(ss => this.snapshot.findIndex(sss => ss.sku === sss.sku) < 0);
        this.diff = this.snapshot.map(ss => {
            let invItem = this.inventory.find(ssi => ss.address === ssi.address);
            let countInv = 0;
            if (invItem !== undefined) countInv = invItem.count;
            let diff = countInv - ss.count;
            let classe = '';
            let diffPercent = ss.count === 0 ? 1 : (diff / ss.count);

            if (invItem === undefined) classe = 'inv_missing';
            else if (diff > 0) classe = 'inv_smaller';
            else if (diff < 0) classe = 'inv_greater';
            return {
                'address': ss.address,
                'sku': ss.sku,
                'product': ss.product,
                'countInv': countInv,
                'countSnap': ss.count,
                'diff': diff,
                'diffPercent': diffPercent,
                'classe': classe
            }
        });

        this.diff = this.diff.concat(extra.map(ss => {
            let classe: string = 'snap_missing';

            return {
                'address': ss.address,
                'sku': ss.sku,
                'product': ss.product,
                'countInv': ss.count,
                'countSnap': 0,
                'diff': ss.count,
                'diffPercent': 1,
                'classe': classe
            }
        }));


        this.diffPrd = this.snapshot.map(ss => {
            let invItems = this.inventory.filter(ssi => ss.sku === ssi.sku);
            let invItem = invItems === undefined || invItems.length === 0 ? undefined : invItems.reduce((ss1: StkSnapshot, ss2: StkSnapshot) => {
                return {
                    'address': '',
                    'count': ss1.count + ss2.count,
                    'date': ss1.date,
                    'product': ss1.product,
                    'sku': ss1.sku,
                    'type': ss1.type
                }
            });
            let countInv = 0;
            if (invItem !== undefined) countInv = invItem.count;
            let diff = countInv - ss.count;
            let classe = '';
            let diffPercent = ss.count === 0 ? 1 : (diff / ss.count);

            if (invItem === undefined) classe = 'inv_missing';
            else if (diff > 0) classe = 'inv_smaller';
            else if (diff < 0) classe = 'inv_greater';
            return {
                'sku': ss.sku,
                'product': ss.product,
                'countInv': countInv,
                'countSnap': ss.count,
                'diff': diff,
                'diffPercent': diffPercent,
                'classe': classe
            }
        });
        this.diffPrd = this.diffPrd.concat(extraPrd.map(ss => {
            let classe: string = 'snap_missing';

            return {
                'sku': ss.sku,
                'product': ss.product,
                'countInv': ss.count,
                'countSnap': 0,
                'diff': ss.count,
                'diffPercent': 100,
                'classe': classe
            }
        }));

        this.calcTotal(this.type === 'ADDRESS' ? this.diff : this.diffPrd);
    }

    calcTotal(diff) {
        this.total = diff.map(sd => {
            return {
                'totalInv': sd.countInv,
                'totalSnap': sd.countSnap,
                'totalDiff': sd.diff,
                'totalDiffPercent': 0
            };
        }).reduce((sd1: StkResume, sd2: StkResume) => {
            let totalInv = sd1.totalInv + sd2.totalInv;
            let totalSnap = sd1.totalSnap + sd2.totalSnap;
            let totalDiff = totalInv - totalSnap;
            let totalDiffPercent = totalSnap === 0 ? 1 : totalDiff / totalSnap;

            return {
                'totalInv': totalInv,
                'totalSnap': totalSnap,
                'totalDiff': totalDiff,
                'totalDiffPercent': totalDiffPercent
            };
        }, {
            'totalInv': 0,
            'totalSnap': 0,
            'totalDiff': 0,
            'totalDiffPercent': 0
        });
    }

    ngAfterViewInit() {

    }

    ngOnDestroy(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
    }

    sendInventory(doc: HunterDocument) {

    }

    invRowClass(row: StkSnapshot): string {
        let itmInv = this.inventory.find(ss => ss.address === row.address);

        if (itmInv === undefined) return 'inv_missing';
        if (itmInv.count > row.count) return 'inv_greater';
        if (itmInv.count < row.count) return 'inv_smaller';
        return '';
    }
}