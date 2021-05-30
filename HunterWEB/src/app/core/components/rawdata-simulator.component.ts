import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RawData } from '../../shared/classes/RawData';
import { RawDataType } from '../../shared/model/enum/RawDataType';
import { HunterDevice } from '../../shared/model/HunterDevice';
import { HunterFeature } from '../../shared/model/HunterFeature';
import { HunterPort } from '../../shared/model/HunterPort';
import { HunterSource } from '../../shared/model/HunterSource';
import { SocketService } from '../../shared/services/socket.service';

const NORMAL_CLOSURE_STATUS: number = 1000;

@Component({
    selector: 'rawdata-simulator',
    templateUrl: 'rawdata-simulator.component.html',
    styleUrls: ['rawdata-simulator.component.scss'],
    providers: [DatePipe]
})
export class RawdataSimulatorComponent implements OnInit, OnDestroy {

    private socketSubscription: Subscription;
    private stream: any;

    rd: RawData;
    origins: any = { 'Selecione': 'Selecione' };
    features: HunterFeature[] = [];
    sources: HunterSource[] = [];
    devices: HunterDevice[] = [];
    ports: HunterPort[] = [];
    filteredDevices: HunterDevice[] = [];
    filteredPorts: HunterPort[] = [];
    rawDataTypes = RawDataType;
    fieldPayload: string = "";
    origin: string;
    msgs: RawData[] = [];
    originConnected: boolean;

    constructor(private http: HttpClient, private socket: SocketService,
        private msgSvc: MessageService, private datePipe: DatePipe) {
        this.rd = new RawData({});
    }

    ngOnInit(): void {
        this.http.get(environment.processserver + "origin").subscribe(data => {
            this.origins = data;
        });
    }

    ngOnDestroy() {
        this.disconnectOrigin();
    }

    onSend() {
        this.rd.ts = new Date().getTime();
        this.rd.payload = this.fieldPayload.replace(/(?:\r\n|\r|\n|\t)/g, '');
        this.http.post(environment.coreserver + "rawdata/input", this.rd)
            .subscribe(() => this.msgSvc.add({ severity: 'info', summary: 'ENVIADO', detail: 'RawData enviado com sucesso' }));
    }

    onClear() {
        this.rd = new RawData({});
        this.fieldPayload = "";
        this.origin = 'Selecione';
        this.disconnectOrigin();
    }

    register(rdForm: NgForm) {
        console.log('Successful registration');
        console.log(rdForm);
    }

    disconnectOrigin() {
        if (this.stream !== undefined) {
            this.socket.disconnect();
        }
        if (this.socketSubscription != null) {
            this.socketSubscription.unsubscribe();
        }
        this.sources = this.sources.slice(this.sources.length);
        this.filteredDevices = this.filteredDevices.slice(this.filteredDevices.length);
        this.filteredPorts = this.filteredPorts.slice(this.filteredPorts.length);
        this.originConnected = false;
    }

    connectOrigin(event) {
        if (event.target.value !== 'Selecione') {
            this.disconnectOrigin();
            this.stream = this.socket.connect(environment.wsprocess + 'origin/' + event.target.value);

            this.socketSubscription = this.stream.subscribe(
                (message: RawData) => {
                    console.log('received message from server: ', message);
                    this.msgs.push(message);
                    let start = Math.max(0, this.msgs.length - 10);
                    let end = Math.max(0, this.msgs.length);
                    this.msgs = this.msgs.slice(start, end);
                    this.msgs.slice();
                }
            );
            this.getOriginInfo();
            this.socket.hideLoadIndicator();
            if (this.rd.source === undefined)
                this.rd.source = 'Selecione';
            if (this.rd.device === undefined)
                this.rd.device = 'Selecione';
            if (this.rd.port === undefined)
                this.rd.port = -1;
        } else
            this.originConnected = false;
    }

    getOriginInfo() {
        this.http.get(environment.processserver + "origin/full/" + this.origin).subscribe(data => {
            if (data != null) {
                console.log(data);
                this.features = data['featureList'];
                this.sources = data['sourceList'];
                this.devices = data['deviceList'].map(d => d.model);
                this.ports = data['portList'];
                this.originConnected = true;
            }
        });
    }

    onChange(_ev): void {
        let features = this.features.filter(f => f.origin.id === this.origin);

        if (features != undefined) {
            // let srcMetas = features.map(f=>f.source);
            let devMetas = features.map(f => f.device);
            let portMetas = features.map(f => f.port);

            if (this.rd.source === undefined)
                this.rd.source = 'Selecione';
            else if (this.rd.source === 'Selecione')
                this.filteredDevices = this.filteredDevices.slice(this.filteredDevices.length);
            else
                this.filteredDevices = this.devices
                    .filter(d => d.source.id === this.rd.source && devMetas.indexOf(d.metaname) >= 0)
            // .sort((d1, d2) => d1.metaname.localeCompare(d2.metaname));
            if (this.rd.device === undefined)
                this.rd.device = 'Selecione';
            else if (this.rd.device === 'Selecione')
                this.filteredPorts = this.filteredPorts.slice(this.filteredPorts.length);
            else
                this.filteredPorts = this.devices
                    .filter(d => d.id === this.rd.device && devMetas.indexOf(d.metaname) >= 0)
                    .map(d => d.ports)
                    .reduce((a, b) => a.concat(b), [])
                    .filter(p => portMetas.indexOf(p.metaname) >= 0)
            // .sort((p1, p2) => p1.metaname.localeCompare(p2.metaname));;
            if (this.rd.port === undefined)
                this.rd.port = -1;
        }
    }

    payloadAlmox() {
        this.fieldPayload = "{\"latitude\":-5.875985,\"longitude\":-35.315161,\"altitude\":0.0,\"relative-altitude\":0.0}";
        this.rd.type = RawDataType.LOCATION;
        this.prettyPrint();
    }
    payloadPreforma() {
        this.fieldPayload = "{\"latitude\":-5.876677,\"longitude\":-35.315995,\"altitude\":0.0,\"relative-altitude\":0.0}";
        this.rd.type = RawDataType.LOCATION;
        this.prettyPrint();
    }
    payloadExpedicao() {
        this.fieldPayload = "{\"latitude\":-5.875722,\"longitude\":-35.316775,\"altitude\":0.0,\"relative-altitude\":0.0}";
        this.rd.type = RawDataType.LOCATION;
        this.prettyPrint();
    }
    payloadSaida() {
        this.fieldPayload = "{\"latitude\":-5.874750051359184,\"longitude\":-35.3165436920859,\"altitude\":0.0,\"relative-altitude\":0.0}";
        this.rd.type = RawDataType.LOCATION;
        this.prettyPrint();
    }
    payloadEntrada() {
        this.fieldPayload = "{\"latitude\":-5.874056280040142,\"longitude\":-35.31619324818566,\"altitude\":0.0,\"relative-altitude\":0.0}";
        this.rd.type = RawDataType.LOCATION;
        this.prettyPrint();
    }
    payloadRota() {
        this.fieldPayload = "{\"latitude\":-5.873449697257153,\"longitude\":-35.31738163791758,\"altitude\":0.0,\"relative-altitude\":0.0}";
        this.rd.type = RawDataType.LOCATION;
        this.prettyPrint();
    }

    payloadArmazenagem() {
        let today = new Date();
        let weekms = 6 * 30 * 24 * 60 * 60 * 1000;
        this.rd.tagId = '1201211';
        this.fieldPayload = "{\"fab\":\"" + this.datePipe.transform(today, "dd/MM/yyyy") + "\",\"val\":\"" + this.datePipe.transform(new Date(today.getTime() + weekms), "dd/MM/yyyy") + "\",\"lot\":\"CNAT" + this.datePipe.transform(new Date(), "ddMMyy") + "\",\"qty\":286,\"count\":1,\"truck\":false}";
        this.rd.type = RawDataType.SENSOR;
        this.prettyPrint();
    }

    payloadPeso() {
        this.rd.tagId = '00256F84';
        this.fieldPayload = "{\"value\":0,\"unit\":\"g\",\"variance\":0}"
        this.rd.type = RawDataType.SENSOR;
        this.prettyPrint();
    }

    payloadLocal() {
        this.rd.tagId = '00256F84';
        this.fieldPayload = "{\"x\":5722,\"y\":-775,\"z\":0.0}";
        this.rd.type = RawDataType.LOCATION;
        this.prettyPrint();
    }

    payloadLinha() {
        this.rd.tagId = 'DPRDLN1';
        this.fieldPayload = "{\"status\":0,\"port\":7}";
        this.rd.type = RawDataType.STATUS;
        this.prettyPrint();
    }

    startDroneInventorySimulation() {
        let eventListCopy = Array.of(...this.eventList);
        let varLoc = 15;
        this.rd.tagId = '00258B7F';
        this.eventTimer = setInterval(() => {
            if (eventListCopy.length > 0) {
                let event = this.simCount++ % 5 == 0 ? eventListCopy.shift() : eventListCopy[0];//eventListCopy[eventListCopy.length - 1];

                this.rd.type = event.rdType;
                eventListCopy[0].rdPayload.y -= 25;
                if (event.rdType === RawDataType.LOCATION) {
                    let x = this.randomVariance(varLoc, event.rdPayload.x);
                    let y = this.randomVariance(varLoc, event.rdPayload.y);

                    this.fieldPayload = JSON.stringify({ "x": x, "y": y, "z": event.rdPayload.z }, undefined, 6);
                }
                this.onSend();
                // if (Math.round(this.randomVariance(3, this.simCount)) % Math.round(this.randomVariance(1, 3)) === 0) {
                if (eventListCopy.length > 3)
                    this.sendHeight(this.randomVariance(20, 248));
                else if (eventListCopy.length > 2)
                    this.sendHeight(this.randomVariance(20, 424));
                else
                    this.sendHeight(this.randomVariance(50, 600));
                // }
            } else
                clearInterval(this.eventTimer);
        }, 300);
    }

    simRack() {
        let eventListCopy = Array.of(...this.eventRackList);
        let varLoc = 15;
        this.rd.tagId = '00258B7F';
        this.eventTimer = setInterval(() => {
            if (eventListCopy.length > 0) {
                let event = this.simCount++ % 5 == 0 ? eventListCopy.shift() : eventListCopy[0];//eventListCopy[eventListCopy.length - 1];

                this.rd.type = event.rdType;
                eventListCopy[0].rdPayload.y -= 25;
                if (event.rdType === RawDataType.LOCATION) {
                    let x = this.randomVariance(varLoc, event.rdPayload.x);
                    let y = this.randomVariance(varLoc, event.rdPayload.y);

                    this.fieldPayload = JSON.stringify({ "x": x, "y": y, "z": event.rdPayload.z }, undefined, 6);
                }
                this.onSend();
                if (eventListCopy.length > 9) {
                    this.sendHeight(this.randomVariance(20, 150));
                    this.sendFrontDist(90);
                } else if (eventListCopy.length > 6) {
                    this.sendHeight(this.randomVariance(20, 250));
                    this.sendFrontDist(90);
                } else if (eventListCopy.length > 3) {
                    this.sendHeight(this.randomVariance(20, 450));
                    this.sendFrontDist(90);
                } else {
                    this.sendHeight(this.randomVariance(20, 650));
                    this.sendFrontDist(90);
                }
            } else
                clearInterval(this.eventTimer);
        }, 300);
    }

    randomVariance(maxVar: number, n: number) {
        return n + Math.round((Math.random() * maxVar * (Math.random() > 0.5 ? -1 : 1)) * 100 + Number.EPSILON) / 100;
    }

    sendHeight(height: number) {
        let rdStr = JSON.stringify(this.rd);
        let rawData: RawData = new RawData(rdStr);
        let val = this.randomVariance(15, height);

        rawData.source = '6018880e-c4e5-11e9-837e-005056a19775';
        rawData.device = '0617767e-c4e6-11e9-837e-005056a19775';
        rawData.port = 0;
        rawData.tagId = '00258B7F';
        rawData.type = RawDataType.SENSOR;
        rawData.ts = new Date().getTime();
        rawData.payload = "{\"name\":\"DISTANCE\",\"value\":" + val + ",\"unit\":\"cm\",\"variance\":" + val + "}";
        this.http.post(environment.coreserver + "rawdata/input", rawData).subscribe();
    }

    sendFrontDist(dist: number) {
        let rdStr = JSON.stringify(this.rd);
        let rawData: RawData = new RawData(rdStr);
        let val = this.randomVariance(15, dist);

        rawData.source = '6018880e-c4e5-11e9-837e-005056a19775';
        rawData.device = '0617767e-c4e6-11e9-837e-005056a19775';
        rawData.port = 1;
        rawData.tagId = '00258B7F';
        rawData.type = RawDataType.SENSOR;
        rawData.ts = new Date().getTime();
        rawData.payload = "{\"name\":\"DISTANCE\",\"value\":" + val + ",\"unit\":\"cm\",\"variance\":" + val + "}";
        this.http.post(environment.coreserver + "rawdata/input", rawData).subscribe();
    }

    prettyPrint() {
        if (this.fieldPayload.length > 7) {
            try {
                var obj = JSON.parse(this.fieldPayload);
                var pretty = JSON.stringify(obj, undefined, 6);
                this.fieldPayload = pretty;
            } catch { }
        }
    }

    preFill() {
        switch (this.rd.type) {
            case RawDataType.LOCATION:
                this.fieldPayload = "{\"latitude\":0.0,\"longitude\":0.0,\"altitude\":0.0,\"relative-altitude\":0.0}";
                break;
            case RawDataType.SENSOR:
                this.fieldPayload = "{\"name\":\"\",\"value\":0,\"unit\":\"cm\",\"variance\":0}";
                break;
            case RawDataType.IDENT:
                this.fieldPayload = "{\"RSSI\":0}";
                break;
            case RawDataType.STATUS:
                this.fieldPayload = "{\"message\":\"\"}";
                break;
        }
        this.prettyPrint();
    }
    private eventTimer;
    private simCount: number = 0;
    private eventList: InventorySimulation[] = [
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -8297.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -8187.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -8077.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7967.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7857.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7747.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7637.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7527.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7417.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7307.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7197.5, "z": 600.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 6049, "y": -7087.5, "z": 600.0 }
        }
    ];

    private eventRackList: InventorySimulation[] = [
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 150.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 350.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 550.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 150.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 350.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 550.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 150.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 350.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 550.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 150.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 350.0 }
        },
        {
            rdType: RawDataType.LOCATION,
            rdPayload: { "x": 5560, "y": 2150, "z": 550.0 }
        }
    ];
}

class InventorySimulation {
    rdType: RawDataType;
    rdPayload: any;
}
