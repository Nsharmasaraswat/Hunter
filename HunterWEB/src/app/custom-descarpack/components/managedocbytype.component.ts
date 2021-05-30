import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { HttpClient } from "@angular/common/http";
import { MessageService } from "primeng/components/common/messageservice";
import { environment } from "../../../environments/environment";

@Component({
    templateUrl: './managedocbytype.component.html'
})
export class ManageDocByTypeComponent implements OnInit {

    public doctype: string;
    public docs: any[] = [];

    constructor(private route: ActivatedRoute, private http: HttpClient,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.docs = [];
        this.route.params.subscribe(lstPrm => {
            this.doctype = lstPrm.doctype;
            this.getDocs(this.doctype);
        });
    }

    getDocs(doctype: string): void {
        console.log(doctype);
        this.http.get(environment.processserver + 'docs/quickByType/' + doctype).subscribe((data: any[]) => {
            console.log(data);
            this.docs = data;
            if (data.length > 0) {

            }
        });
    }
}