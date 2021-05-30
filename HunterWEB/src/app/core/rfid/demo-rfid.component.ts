import { Component, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { environment } from '../../../environments/environment';
import { RawData } from '../../shared/classes/RawData';
import { SocketService } from '../../shared/services/socket.service';

interface DemoMaterial {
    code: string;
    desc: string;
}

interface DemoMessage {
    cnt: number;
    tagId: string;
    ts: Date;
    code: string;
    desc: string;
}

@Component({
    selector: 'rfid-demo',
    templateUrl: './demo-rfid.component.html',
    styleUrls: ['./demo-rfid.component.scss']
})
export class DemoRFIDComponent implements OnDestroy {
    private tagIds: Map<string, DemoMaterial> = new Map();
    private socketSubscription: Subscription;
    private stream: any;

    lstDemoData: any[] = [];
    cols: any[];
    cnt: number = 0;

    lstDemoMaterials = [
        {
            code: '12345-1',
            desc: 'Tubo de Perfuração 01'
        },
        {
            code: '54321-2',
            desc: 'Tubo de Perfuração 02'
        },
        {
            code: '23145-3',
            desc: 'Tubo de Perfuração 03'
        },
        {
            code: '32415-4',
            desc: 'Tubo de Perfuração 04'
        },
        {
            code: '342510-5',
            desc: 'Tubo de Perfuração 05'
        }
    ]

    constructor(private socket: SocketService) {
        this.cols = [
            { field: 'cnt', header: '#' },
            { field: 'code', header: 'Código' },
            { field: 'desc', header: 'Descrição' },
            { field: 'tagId', header: 'Tag' },
            { field: 'ts', header: 'Data/Hora' }
        ];
        this.stream = this.socket.connect(environment.wscore + 'user');

        this.socketSubscription = this.stream.subscribe(
            (message: RawData) => {
                console.log(message);
                let start = Math.max(0, this.lstDemoData.length - 50);
                let end = Math.max(0, this.lstDemoData.length);
                let material: DemoMaterial = { code: '', desc: '' };
                this.cnt++;
                if (this.tagIds.has(message.tagId)) {
                    material = this.tagIds.get(message.tagId);
                } else {
                    material = this.lstDemoMaterials[Math.floor(Math.random() * this.lstDemoMaterials.length)];
                    this.tagIds.set(message.tagId, material);
                }
                console.log({cnt: this.cnt, tagId: message.tagId, ts: new Date(message.ts), code: material.code, desc: material.desc});
                this.lstDemoData.push({cnt: this.cnt, tagId: message.tagId, ts: new Date(message.ts), code: material.code, desc: material.desc});
                this.lstDemoData = this.lstDemoData.slice(start, end);
                console.log(this.lstDemoData.slice());
            }
        );
    }

    ngOnDestroy() {
        this.socketSubscription.unsubscribe()
    }
}
