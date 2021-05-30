import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './action.component.html'
})
export class ActionComponent implements OnInit {

    id: string;
    taskdefs: any[] = [];
    data: any = {};

    selectedTaskDef: any = null;

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
        this.selectedTaskDef = null;
        this.loadData();
        this.loadTaskDefs();
    }

    loadTaskDefs() {
        this.http.get(environment.processserver + 'taskdef/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.taskdefs = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.processserver + 'action/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg;
                if (this.data.model != null) {
                    this.selectedTaskDef = this.data.taskdef;
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedTaskDef == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT A TASK DEF', detail: 'A task def is necessary to perform this action' });
            return;
        }

        this.http.post(environment.processserver + 'action', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    if (this.id === '0')
                        this.router.navigate(['home/process/action/' + msg.id]);
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

    onTaskDefChange() {
        this.data.model = this.selectedTaskDef;
        console.log(this.data);
    }

    getHeader() {
        return this.id === '0' ? 'New Action' : 'Editing Action';
    }

    backRoute() {
        // go to listing page
        this.router.navigate(['home/process/actionList/']);
    }

}
