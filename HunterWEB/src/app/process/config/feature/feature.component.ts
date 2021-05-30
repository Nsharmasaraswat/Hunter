import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './feature.component.html'
})
export class FeatureComponent implements OnInit {

    id: string;
    data: any = {};

    origins: any[] = [];
    sources: any[] = [];
    devices: any[] = [];
    ports: any[] = [];

    selectedOrigin: any = null;
    selectedSource: any = null;
    selectedDevice: any = null;
    selectedPort: any = null;

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
        this.selectedSource = null;
        this.selectedDevice = null;
        this.selectedPort = null;
        this.devices = [];
        this.ports = [];
        this.loadData();
        this.loadOrigins();
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

    loadSources() {
        this.http.get(environment.processserver + 'source/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.sources = msg;

                if (this.data != null && this.data.source != null) {
                    this.selectedSource = this.findSource(this.data.source);
                }

                if (this.selectedSource != null) {
                    this.loadDevices();
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadDevices() {

        if (this.selectedSource == null)
            return;

        this.http.get(environment.processserver + 'device/bysource/' + this.selectedSource.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.devices = msg;

                if (this.data != null && this.data.device != null) {
                    this.selectedDevice = this.findDevice(this.data.device);
                }

                if (this.selectedDevice != null) {
                    this.loadPorts();
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadPorts() {

        if (this.selectedDevice == null)
            return;

        this.http.get(environment.processserver + 'port/bydevice/' + this.selectedDevice.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.ports = msg;

                if (this.data != null && this.data.port != null) {
                    console.log("Verdadeiro");
                    this.selectedPort = this.findPort(this.data.port);
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.processserver + 'feature/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);

                this.data = msg;
                if (this.data.origin != null) {
                    this.selectedOrigin = this.data.origin;
                }

                this.loadSources();
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedOrigin == null || this.selectedSource == null || this.selectedDevice == null || this.selectedPort == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE MAKE SURE YOU FILLED EVERY FIELD', detail: 'Selection is necessary to perform this action' });
            return;
        }

        this.http.post(environment.processserver + 'feature', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    this.router.navigate(['home/process/feature/' + msg.id]);
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'FEATURE NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onBack() {
        this.backRoute();
    }

    onDiscard() {
        // refresh
        this.refresh();
    }

    findSource(metaname) {
        for (let i = 0; i < this.sources.length; i++) {
            if (this.sources[i].metaname == metaname)
                return this.sources[i];
        }
    }

    findDevice(metaname) {
        for (let i = 0; i < this.devices.length; i++) {
            if (this.devices[i].metaname == metaname)
                return this.devices[i];
        }
    }

    findPort(metaname) {
        for (let i = 0; i < this.ports.length; i++) {
            if (this.ports[i].metaname == metaname)
                return this.ports[i];
        }
    }

    onSelectedOrigin() {
        this.data.origin = this.selectedOrigin;
    }

    onSelectedSource() {
        this.data.source = this.selectedSource.metaname;
        this.loadDevices();
    }

    onSelectedDevice() {
        this.data.device = this.selectedDevice.metaname;
        this.loadPorts();
    }

    onSelectedPort() {
        this.data.port = this.selectedPort.metaname;
    }

    getHeader() {
        return this.id === '0' ? 'New Feature' : 'Editing Feature';
    }

    backRoute() {
        // go to list page
        this.router.navigate(['home/process/featureList/']);
    }

}