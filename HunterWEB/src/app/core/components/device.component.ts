import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';


@Component({
    templateUrl: './device.component.html'
})
export class DeviceComponent implements OnInit {

    id: string;
    sources: any[] = [];
    data: any = {};

    selectedSource: any = null;

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
        this.selectedSource = null;
        this.loadData();
        this.loadSources();
    }

    loadSources() {
        this.http.get(environment.coreserver + 'source/all')
            .subscribe((msg: any) => {
                console.log(msg);

                this.sources = msg;

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.coreserver + 'device/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);

                this.data = msg;
                if (this.data.source != null) {
                    this.selectedSource = this.data.source;
                }

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedSource == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN SOURCE', detail: 'A Source is necessary to perform this action' });
            return;
        }

        this.http.post(environment.coreserver + 'device', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    this.router.navigate(['home/core/device/' + msg.id]);
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DEVICE NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            }, error => {
                this.msgSvc.add({ severity: 'error', summary: 'DEVICE NOT SAVED', detail: 'Check console for more details' });
            });
    }

    onBack() {
        this.backRoute();
    }

    onDiscard() {
        // refresh
        this.refresh();
    }

    onSourceChange() {
        this.data.source = this.selectedSource;
        console.log(this.data);
    }

    getHeader() {
        return this.id === '0' ? 'New Device' : 'Editing Device';
    }

    backRoute() {
        // go to device list page
        this.router.navigate(['home/core/deviceList/']);
    }

}
