import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './processfilter.component.html'
})
export class ProcessFilterComponent implements OnInit {

    id: string;
    processes: any[] = [];
    phases: any[] = [];
    data: any = {};

    selectedProcess: any = null;
    selectedPhase: any = null;

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
        this.selectedProcess = null;
        this.selectedPhase = null;
        this.loadData();
        this.loadPhases();
        this.loadProcesses();
    }

    loadProcesses() {
        this.http.get(environment.processserver + 'process/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.processes = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadPhases() {
        this.http.get(environment.processserver + 'processfilter/phase')
            .subscribe((msg: any) => {
                console.log(msg);
                this.phases = msg.map(e => {
                    let obj = { name: e };
                    return obj;
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.processserver + 'processfilter/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg;
                if (this.data.process != null) {
                    this.selectedProcess = this.data.process;
                }
                if (this.data.phase != null) {
                    this.selectedPhase = { name: this.data.phase };
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedProcess == null || this.selectedPhase == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT A PROCESS AND PHASE', detail: 'Selections are necessary to perform this action' });
            return;
        }

        this.http.post(environment.processserver + 'processfilter', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    if (this.id === '0')
                        this.router.navigate(['home/process/processfilter/' + msg.id]);
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

    onProcessChange() {
        this.data.process = this.selectedProcess;
    }

    onPhaseChange() {
        this.data.phase = this.selectedPhase.name;
    }

    getHeader() {
        return this.id === '0' ? 'New Process Filter' : 'Editing Process Filter';
    }

    backRoute() {
        // go to listing page
        this.router.navigate(['home/process/processfilterList/']);
    }

}
