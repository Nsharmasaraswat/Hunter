import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';


@Component({
    templateUrl: './port.component.html'
})
export class PortComponent implements OnInit {

    id: string;
    devices: any[] = [];
    data: any = {};

    selectedDevice: any = null;

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
        this.selectedDevice = null;
        this.loadData();
        this.loadDevices();
    }

    loadDevices() {
        this.http.get(environment.coreserver + 'device/all')
            .subscribe((msg: any) => {
                console.log(msg);

                this.devices = msg;

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.coreserver + 'port/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);

                this.data = msg;
                if (this.data.device != null) {
                    this.selectedDevice = this.data.device;
                }

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedDevice == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN PORT', detail: 'A port is necessary to perform this action' });
            return;
        }

        this.http.post(environment.coreserver + 'port', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    this.router.navigate(['home/core/port/' + msg.id]);
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'PORT NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            }, error => {
                this.msgSvc.add({ severity: 'error', summary: 'PORT NOT SAVED', detail: 'Check console for more details' });
            });
    }

    onBack() {
        this.backRoute();
    }

    onDiscard() {
        // refresh
        this.refresh();
    }

    onDeviceChange() {
        this.data.device = this.selectedDevice;
        console.log(this.data);
    }

    getHeader() {
        return this.id === '0' ? 'New Port' : 'Editing Port';
    }

    backRoute() {
        // go to device list page
        this.router.navigate(['home/core/portList/']);
    }

}
