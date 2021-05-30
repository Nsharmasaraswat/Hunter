import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';
import { ReportColumn } from '../../report/interfaces/report.interface';
import { TokenService } from '../../security/services/token.service';
import { PrintTagOrder } from '../../shared/classes/PrintTagOrder';
import { HunterProduct } from '../../shared/model/HunterProduct';
import { HunterThing } from '../../shared/model/HunterThing';
import { HunterUnit } from '../../shared/model/HunterUnit';
import { LabelData } from '../classes/LabelData';

@Component({
    selector: 'reprint',
    templateUrl: 'reprint.component.html',
    styleUrls: ['reprint.component.scss']
})
export class ReprintComponent {
    deviceId: string = '210e54e2-55b1-11e9-a948-0266c0e70a8c';
    code: string;
    data: any[];
    selectedThing: any;
    dataLoaded: boolean;
    columns: ReportColumn[] = [
        {
            field: 'sku',
            header: 'CODIGO',
            type: '',
            nullString: '',
            width: '5%'
        },
        {
            field: 'product',
            header: 'PRODUTO',
            type: '',
            nullString: '',
            width: '30%'
        },
        {
            field: 'lot_id',
            header: 'LOTE',
            type: '',
            nullString: '',
            width: '10%'
        },
        {
            field: 'manufacturing_batch',
            header: 'FABRICAÇÃO',
            type: 'DATE',
            nullString: '',
            width: '10%'
        },
        {
            field: 'lot_expire',
            header: 'VALIDADE',
            type: 'DATE',
            nullString: '',
            width: '10%'
        },
        {
            field: 'quantity',
            header: 'QUANTIDADE POR VOLUME',
            type: 'NUMBER',
            nullString: '',
            width: '10%'
        },
        {
            field: 'tag',
            header: 'SERIAL',
            type: '',
            nullString: '',
            width: '25%'
        }
    ];

    constructor(private msgSvc: MessageService, private token: TokenService, private http: HttpClient, private router: Router, private route: ActivatedRoute) {
    }

    listTags() {
        this.data = Array.from([]);
        this.http.get(environment.processserver + 'product/bysku/' + this.code).subscribe((p: HunterProduct) => {
            this.http.get(environment.processserver + 'thing/quickByProduct/' + p.id).subscribe((tList: HunterThing[]) => {
                for (let t of tList) {
                    if (t.units != undefined && t.units.length > 0) {
                        this.http.get(environment.coreserver + 'unit/' + t.units[0]).subscribe((u: HunterUnit) => {
                            t.unitModel.push(u);
                            this.data.push({
                                sku: t.product.sku,
                                product: t.product.name,
                                lot_id: t.properties.find(pr=>pr.field.metaname === 'LOT_ID').value,
                                manufacturing_batch: t.properties.find(pr=>pr.field.metaname === 'MANUFACTURING_BATCH').value,
                                lot_expire: t.properties.find(pr=>pr.field.metaname === 'LOT_EXPIRE').value,
                                quantity: t.properties.find(pr=>pr.field.metaname === 'QUANTITY').value,
                                tag: u.tagId,
                                thing: t
                            });
                        });
                    }
                }
                this.dataLoaded = true;
            });
        });
    }

    printTag() {
        let pto: PrintTagOrder = new PrintTagOrder(this.deviceId, null, null);
        let thing = this.selectedThing.thing;
        let labelData = new LabelData();

        labelData.sku = thing.product.sku;
        pto.thing = thing.id;
        pto.properties = labelData;
        console.log(thing);
        this.http.post(environment.processserver + 'print/reprint/' + thing.units[0], pto)
            .subscribe((data: any) => {
                if (data.result) {
                    this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: 'Etiqueta Reimpressa Com Sucesso' });
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'Não foi possível imprimir etiqueta', detail: data.message });
                }
            },
                (error: Error) => {
                    this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar documento', detail: error.message });
                }
            )
    }
}
