import { HttpClient } from "@angular/common/http";
import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { environment } from "../../../environments/environment";

@Component({
    selector: 'document-summary',
    templateUrl: 'document-summary.component.html',
    styleUrls: ['document-summary.component.scss']
})
export class DocumentSummaryComponent {
    public docSummary: any[] = [];
    columns: string[] = ["IMPRESSO", "EMBARCADO", "RECEBIDO", "ARMAZENADO", "SEPARADO", "EXPEDIDO"];

    constructor(private route: ActivatedRoute, private http: HttpClient,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.docSummary = [];
        this.route.params.subscribe(lstPrm => {
            this.getDocs();
        });
    }

    getDocs(): void {
        this.http.get(environment.customserver + 'task/docSummary').subscribe((data: any[]) => {
            console.log(data);
            this.docSummary = data;
        });
    }
}
