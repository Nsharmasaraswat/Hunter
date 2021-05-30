import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";

@Component({
    templateUrl: './state-change.component.html'
})

export class StateChangeComponent implements OnInit {

    constructor(private route: ActivatedRoute, private http: HttpClient,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.route.params.subscribe(lstPrm => {
            console.log(lstPrm);
        });

    }

}