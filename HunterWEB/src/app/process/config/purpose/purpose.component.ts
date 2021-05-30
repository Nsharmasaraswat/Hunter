import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './purpose.component.html'
})
export class PurposeComponent implements OnInit {

    id: string;
    data: any = {};

    tasks: any[] = [];
    processes: any[] = [];
    origins: any[] = [];

    allTasks: any[] = [];
    allProcesses: any[] = [];
    allOrigins: any[] = [];

    selectedTask: any = null;
    selectedProcess: any = null;
    selectedOrigin: any = null;

    selectedTaskTable: any = null;
    selectedProcessTable: any = null;
    selectedOriginTable: any = null;

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
        this.selectedTask = null;
        this.selectedProcess = null;
        this.selectedOrigin = null;
        this.selectedTaskTable = null;
        this.selectedProcessTable = null;
        this.selectedOriginTable = null;
        this.loadData();
        this.loadAllTasks();
        this.loadAllProcesses();
        this.loadAllOrigins();
    }

    loadData() {
        this.http.get(environment.processserver + 'purpose/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg;

                if (this.data != null) {
                    this.loadTasks();
                    this.loadOrigins();
                    this.loadProcesses();
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadTasks() {
        this.http.get(environment.processserver + 'taskdef/bypurpose/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.tasks = msg;
                this.data.tasks = this.tasks;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadOrigins() {
        this.http.get(environment.processserver + 'origin/bypurpose/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.origins = msg;
                this.data.origins = this.origins;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadProcesses() {
        this.http.get(environment.processserver + 'process/bypurpose/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.processes = msg;
                this.data.processes = this.processes;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadAllTasks() {
        this.http.get(environment.processserver + 'taskdef/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.allTasks = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadAllOrigins() {
        this.http.get(environment.processserver + 'origin/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.allOrigins = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadAllProcesses() {
        this.http.get(environment.processserver + 'process/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.allProcesses = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onAddOrigin() {
        if (this.selectedOrigin == null)
            return;

        this.origins.push(this.selectedOrigin);
        this.origins = this.origins.slice();
        this.data.origins = this.origins;

        console.log(this.data);
    }

    onRemoveOrigin() {
        if (this.selectedOriginTable == null)
            return;

        let index = this.origins.indexOf(this.selectedOriginTable);

        if (index == -1)
            return;

        this.origins.splice(index, 1);
        this.data.origins = this.origins;

        console.log(this.data);
    }

    onAddTask() {
        if (this.selectedTask == null)
            return;

        this.tasks.push(this.selectedTask);
        this.tasks = this.tasks.slice();
        this.data.tasks = this.tasks;

        console.log(this.data);
    }

    onRemoveTask() {
        if (this.selectedTaskTable == null)
            return;

        let index = this.tasks.indexOf(this.selectedTaskTable);

        if (index == -1)
            return;

        this.tasks.splice(index, 1);
        this.data.tasks = this.tasks;

        console.log(this.data);
    }

    onAddProcess() {
        if (this.selectedProcess == null)
            return;

        this.processes.push(this.selectedProcess);
        this.processes = this.processes.slice();
        this.data.processes = this.processes;

        console.log(this.data);
    }

    onRemoveProcess() {
        if (this.selectedProcessTable == null)
            return;

        let index = this.processes.indexOf(this.selectedProcessTable);

        if (index == -1)
            return;

        this.processes.splice(index, 1);
        this.data.processes = this.processes;

        console.log(this.data);
    }

    onSave() {

        console.log(this.data);

        if (!this.data.hasOwnProperty('origins') || !this.data.hasOwnProperty('tasks') || !this.data.hasOwnProperty('processes')) {
            console.log("You shall not pass!");
            return;
        }

        if (this.data.id == null)
            this.data.id = this.id;

        this.http.post(environment.processserver + 'purpose', this.data)
            .subscribe((msg: any) => {
                console.log(msg);
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    if (this.id === '0')
                        this.router.navigate(['home/process/purpose/' + msg.id]);
                    else
                        this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onDiscard() {
        // refreshing data to discard changes
        this.refresh();
    }

    onBack() {
        this.backRoute();
    }

    getHeader() {
        return this.id === '0' ? 'New Purpose' : 'Editing Purpose';
    }

    backRoute() {
        // go to list page
        this.router.navigate(['home/process/purposeList/']);
    }
}