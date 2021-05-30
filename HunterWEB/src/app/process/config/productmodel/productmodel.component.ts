import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './productmodel.component.html'
})
export class ProductModelComponent implements OnInit {

    id: string;
    models: any[] = [];
    data: any = {};

    selectedModel: any = null;

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.id = data.id;
            this.refresh();
        });
    }

    refresh() {
        this.selectedModel = null;
        this.loadData();
        this.loadModels();
    }

    loadModels() {
        this.http.get(environment.processserver + 'propertymodel/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.models = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.processserver + 'productmodel/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg;
                if (this.data.propertymodel != null) {
                    this.selectedModel = this.data.propertymodel;
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedModel == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT A MODEL', detail: 'A selection is necessary to perform this action' });
            return;
        }

        this.http.post(environment.processserver + 'productmodel', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    if (this.id === '0')
                        this.router.navigate(['home/process/productmodel/' + msg.id]);
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

    onPropertyModelChange() {
        this.data.propertymodel = this.selectedModel;
        console.log(this.data);
    }

    getHeader() {
        return this.id === '0' ? 'New Product Model' : 'Editing Product Model';
    }

    backRoute() {
        // go to listing page
        this.router.navigate(['home/process/productmodelList/']);
    }

}
