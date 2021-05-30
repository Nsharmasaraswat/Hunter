import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './model-field-list.component.html'
})
export class ModelFieldListComponent implements OnInit {

    selectedItem: any = null;
    data: any[] = [];
    type: any = {};

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.type = data.type;
            this.refresh();
        });
    }

    refresh() {
        this.selectedItem = null;
        this.data = [];
        this.loadData();
    }

    loadData() {
        this.http.get(environment.processserver + 'documentmodelfield/metaname/' + this.type)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg.map(d => {
                    let obj = {
                        id: d.id,
                        modelId: d.model.id,
                        name: d.name,
                        metaname: d.metaname,
                        type: d.type,
                        status: d.status,
                        creation: this.formatDate(new Date(d.createdAt))
                    };
                    return obj;
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
            });
    }

    onEdit() {

        if (this.selectedItem == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        // go to model-field.component with id = id do model field
        this.router.navigate(['home/process/listDocuments/' + this.type + '/edit/modelfields/' + this.selectedItem.id]);
    }

    onDelete() {

        if (this.selectedItem == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        this.http.delete(environment.processserver + 'documentmodelfield/' + this.selectedItem.id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA DELETED', detail: 'Data deleted from database' });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onNew() {
        console.log("new clicked");
        // go to model-field.component with id = 0
        this.router.navigate(['home/process/listDocuments/' + this.type + '/edit/modelfields/0']);
    }

    onBack() {
        // back route
        this.backRoute();
    }

    backRoute() {
        // go to previous route
        this.router.navigate(['/home/process/listDocuments/' + this.type + '/edit/']);
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