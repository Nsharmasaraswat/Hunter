import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';
import { TokenService } from '../../../security/services/token.service';

@Component({
    templateUrl: './view-documents.component.html'
})
export class ViewDocumentsComponent implements OnInit {

    selectedItem: any = null;
    data: any[] = [];
    type: string;

    stream: any;

    constructor(private msgSvc: MessageService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) { }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.type = data.type;
            this.refresh();
        });
    }

    refresh() {
        this.selectedItem = null;
        this.loadData(this.type);
    }

    loadData(type) {
        this.http.get(environment.processserver + 'document/bytype/' + type)
            .subscribe((lst: any[]) => {
                this.data = lst.map(d => {
                    d.createdAtText = this.formatDate(new Date(d.createdAt));
                    return d;
                });
            });
    }

    onEdit() {

        if (this.selectedItem == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        this.router.navigate(['home/process/listDocuments/' + this.type + '/', this.selectedItem.id]);
    }

    onNew() {
        this.router.navigate(['/home/process/listDocuments/' + this.type + '/', 0]);
    }

    onView() {
        if (this.selectedItem == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN ITEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        this.router.navigate(['home/process/viewDocument/', this.selectedItem.id]);
    }

    onDelete() {

        if (this.selectedItem == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN ITEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        this.http.delete(environment.processserver + 'document/' + this.selectedItem.id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add(
                        {
                            severity: 'success',
                            summary: 'DOCUMENT DELETED',
                            detail: 'Document was deleted with success'
                        });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DOCUMENT NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onBack() {
        // back route
        this.backRoute();
    }

    backRoute() {
        // go to previous route
        this.router.navigate(['/home/process/listDocuments/']);
    }

    formatDate(date) {
        const d = new Date(date);
        let month = '' + (d.getMonth() + 1);
        let day = '' + d.getDate();
        const year = d.getFullYear();

        if (month.length < 2) {
            month = '0' + month;
        }

        if (day.length < 2) {
            day = '0' + day;
        }

        return [year, month, day].join('-');
    }
}
