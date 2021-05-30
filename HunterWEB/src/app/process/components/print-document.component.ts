import { HttpClient } from "@angular/common/http";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { PrintTagOrder } from "../../shared/classes/PrintTagOrder";
import { HunterFieldType } from "../../shared/model/enum/HunterFieldType";
import { HunterDocument } from "../../shared/model/HunterDocument";
import { HunterProduct } from "../../shared/model/HunterProduct";

@Component({
    templateUrl: './print-document.component.html'
})

export class PrintDocumentComponent implements OnInit, OnDestroy {
    private docId: string;
    private routeSubscription: Subscription;
    fieldTypes = HunterFieldType;
    deviceId: string;
    document: HunterDocument;
    product: HunterProduct;
    columns: ReportColumn[] = [{
        field: 'product.name',
        header: 'PRODUTO',
        type: '',
        nullString: '',
        width: '70%'
    },
    {
        field: 'qty',
        header: 'QUANTIDADE',
        type: '',
        nullString: '',
        width: '20%'
    }, {
        field: 'measureUnit',
        header: 'UNIDADE',
        type: '',
        nullString: '',
        width: '10%'
    }];

    printOrders: PrintTagOrder[] = [];

    constructor(private route: ActivatedRoute, private http: HttpClient,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(lstPrm => {
            this.docId = lstPrm.docId;
            this.deviceId = lstPrm.devId;
            this.http.get(environment.processserver + "document/" + this.docId)
                .subscribe(
                    (data: HunterDocument) => {
                        this.document = data;
                        console.log(this.document);
                    },

                    (error: Error) => {
                        this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar documento', detail: error.message });
                    },

                    () => { console.log("GotDocument") });
        });
    }

    ngOnDestroy(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
    }

    getProductDetails(event): void {
        this.http.get(environment.processserver + "product/" + event.data.product.id)
            .subscribe(
                (data: HunterProduct) => {
                    this.product = data;
                    console.log(this.product);
                },

                (error: Error) => {
                    this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar produto', detail: error.message });
                },

                () => { console.log("GotProduct") });
    }
}