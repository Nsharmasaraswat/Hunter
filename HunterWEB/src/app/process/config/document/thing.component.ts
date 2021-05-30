import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './thing.component.html'
})
export class ThingComponent implements OnInit {

    documentId: any = {};
    type: any = {};
    data: any[] = [];

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.documentId = data.id;
            this.type = data.type;
            this.refresh();
        });
    }

    refresh() {
        this.loadData();
    }

    loadData() {
        console.log("loading data...")
        this.http.get(environment.processserver + 'documentthing/documentid/' + this.documentId)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
            });
    }

    onSave() {
        console.log(this.data);
        this.http.post(environment.processserver + 'documentthing/list', this.data, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onDiscard() {
        // go back to previous route
        this.router.navigate(['home/process/listDocuments/' + this.type + '/', this.documentId]);
        this.msgSvc.add({ severity: 'error', summary: 'CHANGES DISCARDED', detail: 'Changes were not applied' });
    }

    onBack() {
        this.backRoute();
    }

    backRoute() {
        // back to route
        this.router.navigate(['home/process/listDocuments/' + this.type]);
    }

    onDocument() {
        // go to document route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.documentId]);
    }

    onItem() {
        // go to item route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.documentId + '/item']);
    }

    onThing() {
        // go to thing route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.documentId + '/thing']);
    }

    onField() {
        // go to field route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.documentId + '/field']);
    }
}