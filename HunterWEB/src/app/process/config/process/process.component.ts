import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './process.component.html'
})
export class ProcessComponent implements OnInit {

    id: string;
    data: any = {};

    origins: any[] = [];
    workflows: any[] = [];

    selectedOrigin: any = null;
    selectedWorkflow: any = null;

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
        this.selectedOrigin = null;
        this.selectedWorkflow = null;
        this.loadData();
        this.loadOrigins();
        this.loadWorkflows();
    }

    loadOrigins() {
        this.http.get(environment.processserver + 'origin/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.origins = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadWorkflows() {
        this.http.get(environment.processserver + 'workflow/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.workflows = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.processserver + 'process/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg;
                if (this.data.origin != null) {
                    this.selectedOrigin = this.data.origin;
                }
                if (this.data.workflow != null) {
                    this.selectedWorkflow = this.data.workflow;
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedOrigin == null || this.selectedWorkflow == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT A ORIGIN AND A WORKFLOW', detail: 'Selections are necessary to perform this action' });
            return;
        }

        this.http.post(environment.processserver + 'process', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    if (this.id === '0')
                        this.router.navigate(['home/process/process/' + msg.id]);
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

    onOriginChange() {
        this.data.origin = this.selectedOrigin;
        console.log(this.data);
    }

    onWorkflowChange() {
        this.data.workflow = this.selectedWorkflow;
        console.log(this.data);
    }

    getHeader() {
        return this.id === '0' ? 'New Process' : 'Editing Process';
    }

    backRoute() {
        // go to taskdef list page
        this.router.navigate(['home/process/processList/']);
    }
}
