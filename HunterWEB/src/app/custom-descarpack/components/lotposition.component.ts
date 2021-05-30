import { HttpClient } from "@angular/common/http";
import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { environment } from "../../../environments/environment";

@Component({
    templateUrl: 'lotposition.component.html'
})
export class LotPositionComponent {

    public lotnumber: string;
    public data: any[] = [];
    public review: any = {'IMPRESSO': 0, 'EMBARCADO':0, 'RECEBIDO':0, 'BLOQUEADO':0, 'ARMAZENADO':0, 'SEPARADO':0,'EXPEDIDO':0};
    public showReview = false;

    constructor(private route: ActivatedRoute, private http: HttpClient,
        private msgSvc: MessageService, private router: Router) { }


    getLotPosition() {
        this.showReview = false;
        this.http.get(environment.customserver + 'task/lotposition/' + this.lotnumber).subscribe((data: any[]) => {
            //console.log(data);
            this.data = data;
            this.review = {'IMPRESSO': 0, 'EMBARCADO':0, 'RECEBIDO':0, 'BLOQUEADO':0, 'ARMAZENADO':0, 'SEPARADO':0,'EXPEDIDO':0};
            for(let itm of data) {
                this.review[itm.status]++;
            }
            this.showReview = true;
        });
    }

    receiveLot() {
        this.showReview = false;
        this.http.get(environment.customserver + 'task/receivelot/' + this.lotnumber).subscribe((data: any[]) => {
            //console.log(data);
            this.data = data;
            this.review = {'IMPRESSO': 0, 'EMBARCADO':0, 'RECEBIDO':0, 'BLOQUEADO':0, 'ARMAZENADO':0, 'SEPARADO':0,'EXPEDIDO':0};
            for(let itm of data) {
                this.review[itm.status]++;
            }
            this.showReview = true;
        });
    }

    storeLot() {
        this.showReview = false;
        this.http.get(environment.customserver + 'task/storelot/' + this.lotnumber).subscribe((data: any[]) => {
            //console.log(data);
            this.data = data;
            this.review = {'IMPRESSO': 0, 'EMBARCADO':0, 'RECEBIDO':0, 'BLOQUEADO':0, 'ARMAZENADO':0, 'SEPARADO':0,'EXPEDIDO':0};
            for(let itm of data) {
                this.review[itm.status]++;
            }
            this.showReview = true;
        });
    }

    blockLot() {
        this.showReview = false;
        this.http.get(environment.customserver + 'task/blocklot/' + this.lotnumber).subscribe((data: any[]) => {
            //console.log(data);
            this.data = data;
            this.review = {'IMPRESSO': 0, 'EMBARCADO':0, 'RECEBIDO':0, 'BLOQUEADO':0, 'ARMAZENADO':0, 'SEPARADO':0,'EXPEDIDO':0};
            for(let itm of data) {
                this.review[itm.status]++;
            }
            this.showReview = true;
        });
    }

    unblockLot() {
        this.showReview = false;
        this.http.get(environment.customserver + 'task/unblocklot/' + this.lotnumber).subscribe((data: any[]) => {
            //console.log(data);
            this.data = data;
            this.review = {'IMPRESSO': 0, 'EMBARCADO':0, 'RECEBIDO':0, 'BLOQUEADO':0, 'ARMAZENADO':0, 'SEPARADO':0,'EXPEDIDO':0};
            for(let itm of data) {
                this.review[itm.status]++;
            }
            this.showReview = true;
        });
    }

}