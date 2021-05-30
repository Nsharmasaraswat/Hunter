import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from "@angular/core";
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterDocument, HunterDocumentThing } from '../../shared/model/HunterDocument';

declare var require: any;

var pdfMake = require('pdfmake/build/pdfmake.js');
var pdfFonts = require('pdfmake/build/vfs_fonts.js');


const VOLUME_PROPERTY_FIELD_ID: string = '472e791f-6076-4b45-bf96-9b82d2f7ef93';
const QUANTITY_PROPERTY_FIELD_ID: string = '60b4ad62-b399-11e9-afa2-049226d943d2';
const SECTORS_PROPERTY_FIELD_ID: string = '60b4b3e5-b399-11e9-afa2-049226d943d2';

@Component({
    templateUrl: './volume-inventory.component.html',
    providers: [DatePipe]
})

export class VolumeInventoryComponent implements OnInit {
    routeSubscription: Subscription;
    inventory: HunterDocument;
    gridImg: string;
    columns: ReportColumn[] = [
        {
            field: 't5',
            header: 'T5',
            type: '',
            nullString: '',
            width: '5%'
        },
        {
            field: 'code',
            header: 'CODE',
            type: '',
            nullString: '',
            width: '5%'
        },
        {
            field: 'desc',
            header: 'DESCRIPTION',
            type: '',
            nullString: '',
            width: '35%'
        },
        {
            field: 'vol',
            header: 'VOLUME',
            type: 'NUMBER',
            nullString: '',
            width: '5%'
        },
        {
            field: 'qty',
            header: 'QUANTITY',
            type: 'NUMBER',
            nullString: '',
            width: '5%'
        },
        {
            field: 'sec',
            header: 'SECTORS',
            type: '',
            nullString: '',
            width: '45%'
        }
    ];
    data: any[];
    constructor(private msgSvc: MessageService, private http: HttpClient, private route: ActivatedRoute, private datePipe: DatePipe, private sanitizer: DomSanitizer) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.http.get(environment.processserver + 'document/' + routeParams.docId, { responseType: 'json' })
                .subscribe((doc: HunterDocument) => {
                    this.inventory = doc;
                    this.gridImg = doc.fields.find(df => df.field.metaname === 'GRIDFILE').value;
                    this.data = new Array();
                    doc.things.map( // data item
                        (dt: HunterDocumentThing) => {
                            let obj: any = {
                                t5: dt.thing.product.fields.find(pf => pf.model.metaname === 'T5NUMBER').value,
                                code: dt.thing.product.fields.find(pf => pf.model.metaname === 'CODE').value,
                                desc: dt.thing.product.name,
                                vol: dt.thing.properties.find(pr => pr.field.id === VOLUME_PROPERTY_FIELD_ID).value,
                                qty: dt.thing.properties.find(pr => pr.field.id === QUANTITY_PROPERTY_FIELD_ID).value,
                                sec: dt.thing.properties.find(pr => pr.field.id === SECTORS_PROPERTY_FIELD_ID).value,
                            };
                            this.data.push(obj);
                        }
                    );
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERROR LOADING DOCUMENT', detail: error });
                });
        });
    }

    export(): void {
        let endl: string = '\n';
        const header_itens: any = [ // hearder columns item
            { text: 'T5', fontSize: 9, bold: true, alignment: 'center' },
            { text: 'CODE', fontSize: 9, bold: true, alignment: 'center' },
            { text: 'DESCRIPTION', fontSize: 9, bold: true, alignment: 'left' },
            { text: 'VOLUME', fontSize: 9, bold: true, alignment: 'center' },
            { text: 'QUANTITY', fontSize: 9, bold: true, alignment: 'center' },
            { text: 'SECTORS', fontSize: 9, bold: true, alignment: 'center' }
        ]
        let corpo: any = [];

        corpo.push(header_itens);

        this.inventory.things.map( // data item
            (dt: HunterDocumentThing) => {
                let arr: any = [
                    { text: dt.thing.product.fields.find(pf => pf.model.metaname === 'T5NUMBER').value, fontSize: 8, alignment: 'center' },
                    { text: dt.thing.product.fields.find(pf => pf.model.metaname === 'CODE').value, fontSize: 8, alignment: 'center' },
                    { text: dt.thing.product.name, fontSize: 8, alignment: 'center' },
                    { text: dt.thing.properties.find(pr => pr.field.id === VOLUME_PROPERTY_FIELD_ID).value, fontSize: 8, alignment: 'center' },
                    { text: dt.thing.properties.find(pr => pr.field.id === QUANTITY_PROPERTY_FIELD_ID).value, fontSize: 8, alignment: 'center' },
                    { text: dt.thing.properties.find(pr => pr.field.id === SECTORS_PROPERTY_FIELD_ID).value, fontSize: 8, alignment: 'center' }
                ]
                corpo.push(arr);
            }
        );
        let pdf = {
            content: [
                { // Title report
                    text: 'LC Volume Inventory Report' + endl + endl,
                    style: 'header',
                    alignment: 'center',
                    bold: true,
                    fontSize: 16
                },
                { // Name company                                       
                    table: {
                        widths: ['*', 'auto'],
                        body: [
                            [
                                {
                                    text: ['CUSTOMER: DAIMLER' + endl,
                                        'LOCATION: Schleifwiesenstraße, 27\t',
                                        'GROßBOTTWAR'
                                    ],

                                    border: [true, true, false, true]
                                },
                                {
                                    text: ['SITE: LGI' + endl],
                                    border: [false, true, true, true]
                                }
                            ],
                        ]
                    }
                },

                endl,
                {
                    table: { // header report
                        widths: ['*'],
                        body: [
                            [
                                {
                                    text: [
                                        { text: 'INVENTORY DATE: ', bold: true }, this.datePipe.transform(this.inventory.createdAt, "dd/MM/yyyy"), ' @ ' + this.datePipe.transform(this.inventory.createdAt, "hh:mm:ss") + 'h' + endl
                                    ],

                                    border: [true, true, false, false]
                                }
                            ],
                        ]
                    }
                },

                { // data itens report
                    table: {
                        widths: [30, 25, '*', 40, 50, 110],
                        body: corpo
                    }
                },

                endl, endl,

            ], // End Content
            footer: {
                columns: [
                    { text: 'GTP Automation - Hunter IoT Visibility Manager', alignment: 'center' },
                ],
                fontSize: 7
            },
        }
        pdfMake.vfs = pdfFonts.pdfMake.vfs;
        pdfMake.createPdf(pdf).download('hunter_' + this.inventory.code);
    }
}