import { HttpClient } from "@angular/common/http";
import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { environment } from "../../../environments/environment";

@Component({
    selector: 'thing-summary',
    templateUrl: 'thing-summary.component.html',
    styleUrls: ['thing-summary.component.scss']
})
export class ThingSummaryComponent {
    public thingSummary: any[] = [];

    constructor(private route: ActivatedRoute, private http: HttpClient,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.thingSummary = [];
        this.route.params.subscribe(lstPrm => {
            this.getDocs();
        });
    }

    getDocs(): void {
        this.http.get(environment.customserver + 'task/thingSummary').subscribe((data: any[]) => {
            console.log(data);
            this.thingSummary = data;
        });
    }
}
