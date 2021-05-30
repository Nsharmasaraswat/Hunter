import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './model-field.component.html'
})
export class ModelFieldComponent implements OnInit {

    type: any = {};
    id: any = {};
    data: any = {};
    typeList: any[] = [];
    selectedType: any = {};

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.type = data.type;
            this.id = data.id;
            this.refresh();
        });
    }

    refresh() {
        this.selectedType = {};
        this.loadData();
        this.loadTypeList();
    }

    loadData() {
        this.http.get(environment.processserver + 'documentmodelfield/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = {
                    id: this.id,
                    model: this.type,
                    metaname: msg.metaname,
                    name: msg.name,
                    status: msg.status,
                    type: msg.type
                };

                this.selectedType = { name: msg.type };
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
            });
    }

    loadTypeList() {
        this.http.get(environment.processserver + 'documentmodelfield/type')
            .subscribe((msg: any) => {
                this.typeList = msg.map(d => {
                    let obj = {
                        name: d
                    }
                    return obj;
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
            });
    }

    onSave() {
        console.log(this.data);
        this.http.post(environment.processserver + 'documentmodelfield/', this.data, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    // go back to previous route
                    this.backRoute();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onDelete() {
        console.log(this.data);
        this.http.delete(environment.processserver + 'documentmodelfield/' + this.id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA DELETED', detail: 'Data deleted from database' });
                    // go back to previous route
                    this.backRoute();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onBack() {
        // back route
        this.backRoute();
    }

    onDiscard() {
        // reload page
        this.refresh();
    }

    onTypeChange() {
        this.data.type = this.selectedType.name;
    }

    getHeader() {
        // check if new or editing by the passed id
        return this.id == "0" ? "Creating Model Field" : "Editing Model Field";
    }

    backRoute() {
        // back route
        this.router.navigate(['home/process/listDocuments/' + this.type + '/edit/modelfields']);
    }

}