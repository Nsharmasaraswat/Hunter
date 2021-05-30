import { HttpClient } from "@angular/common/http";
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Observable, Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { HunterDocument } from "../../shared/model/HunterDocument";
import { HunterPermission } from "../../shared/model/HunterPermission";
import { HunterThing } from "../../shared/model/HunterThing";
import RestStatus from "../../shared/utils/restStatus";

interface PalletHistory {
    thing: HunterThing;
    parents: HunterDocument[];
}

@Component({
    selector: 'view-pallet-history',
    templateUrl: './view-pallet-history.component.html'
})
export class ViewPalletHistoryComponent implements OnInit, OnDestroy {

    private routeSubscription: Subscription;

    permission: HunterPermission;
    th: HunterThing;
    docs: HunterDocument[];
    movs: HunterDocument[];
    picks: HunterDocument[];
    checks: HunterDocument[];

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private changeDetector: ChangeDetectorRef, private route: ActivatedRoute) {
        console.log('ViewPalletHistory Constructor');
    }

    ngOnInit(): void {
        console.log('ViewPalletHistory OnInit');
        this.routeSubscription = this.route.params.subscribe(data => {
            this.http.get(environment.processserver + 'ui/palletHistory/' + data.thId).catch(error => {
                console.error("error catch", error);
                return Observable.of({ result: false, message: "Error Value Emitted" });
            }).subscribe((resp: PalletHistory) => {
                console.log(resp);
                if (resp.thing !== null && resp.parents !== null) {
                    this.th = resp.thing;
                    this.docs = resp.parents.map(d => new HunterDocument(d))
                        .filter(d => (d.model.metaname === 'ORDCONF' && d.fields.findIndex(df => df.value === 'EPAPD') >= 0) || (d.model.metaname !== 'ORDCONF' && d.model.metaname !== 'RETORDCONF' && d.model.metaname !== 'ORDCRIACAO'))
                        .sort((d1, d2) => d1.createdAt.getTime() - d2.createdAt.getTime());
                    this.movs = this.docs.filter(d => d.model.metaname === 'ORDMOV');
                    this.picks = this.docs.filter(d => d.model.metaname === 'PICKING');
                    this.checks = this.docs.filter(d => d.model.metaname === 'ORDCONF' && d.fields.findIndex(df => df.value === 'EPAPD') >= 0);
                }
            }, (error: RestStatus) => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR PALLET', detail: error.message });
            });
        });
    }

    ngOnDestroy(): void {
        this.unsubscribeObservers();
    }

    getDocStatus(doc: HunterDocument) {
        switch (doc.status) {
            case "LOAD":
                return 'CARREGAR';
            case "UNLOAD":
                return 'DESCARREGAR';
            case "ARMPROD":
                return 'ARMAZENAR_PRODUCAO';
            case "ARMCAM":
                return 'ARMAZENAR_CAMINHAO';
            case "REPACK":
                return 'REEMBALAGEM';
            default:
                return doc.status;
        }
    }

    unsubscribeObservers(): void {
        if (this.routeSubscription !== undefined && this.routeSubscription !== null)
            this.routeSubscription.unsubscribe();
    }
}