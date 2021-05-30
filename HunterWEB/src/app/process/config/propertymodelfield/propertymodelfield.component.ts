import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './propertymodelfield.component.html'
})
export class PropertyModelFieldComponent implements OnInit {

    id: string;
    metaname: string;
    types: any[] = [];
    data: any = {};
    model: any = {};

    selectedType: any = null;

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.id = data.id;
            this.metaname = data.metaname;
            this.refresh();
        });
    }

    refresh() {
        this.selectedType = null;
        this.loadData();
        this.loadModel();
        this.loadTypes();
    }

    loadTypes() {
        this.http.get(environment.processserver + 'propertymodelfield/type')
            .subscribe((msg: any) => {
                console.log(msg);
                this.types = msg.map(e => {
                    let obj = { name: e };
                    return obj;
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.processserver + 'propertymodelfield/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg;
                if (this.data.type != null)
                    this.selectedType = { name: this.data.type }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadModel() {
        this.http.get(environment.processserver + 'propertymodel/metaname/' + this.metaname)
            .subscribe((msg: any) => {
                this.model = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedType == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE MAKE ALL SELECTIONS', detail: 'Selections are necessary to perform this action' });
            return;
        }

        let jsonData = {
            id: this.data.id,
            metaname: this.data.metaname,
            name: this.data.name,
            type: this.data.type,
            status: this.data.status,
            model: this.model.id
        };

        this.http.post(environment.processserver + 'propertymodelfield', jsonData)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    if (this.id === '0')
                        this.router.navigate(['home/process/propertymodelfield/' + this.metaname + '/' + msg.id]);
                    else
                        this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            }, error => {
                this.msgSvc.add({ severity: 'error', summary: 'DATA NOT SAVED', detail: 'Check console for more details' });
            });
    }

    onBack() {
        this.backRoute();
    }

    onDiscard() {
        // refresh
        this.refresh();
    }

    onTypeChange() {
        this.data.type = this.selectedType.name;
    }

    getHeader() {
        return this.id === '0' ? 'New Property Model Field' : 'Editing Property Model Field';
    }

    backRoute() {
        // go to taskdef list page
        this.router.navigate(['home/process/propertymodelfieldList/' + this.metaname]);
    }

}
