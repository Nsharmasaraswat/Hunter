import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, Inject, LOCALE_ID, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from "rxjs";
import { Observable } from 'rxjs/Rx';
import { environment } from '../../../environments/environment';
import { Report, ReportColumn } from '../../report/interfaces/report.interface';
import { NavigationService } from '../../shared/services/navigation.service';
import RestResponse from "../../shared/utils/restResponse";
// import * as pdfMake from 'pdfmake/build/pdfmake';
// import * as pdfFonts from 'pdfmake/build/vfs_fonts';

declare var require: any;

var pdfMake = require('pdfmake/build/pdfmake.js');
var pdfFonts = require('pdfmake/build/vfs_fonts.js');

@Component({
    templateUrl: './romaneio.component.html'
})

export class RomaneioComponent implements OnInit {

    public data: any[] = [];
    public routeSubscription: Subscription;
    public params: string;
    selectedReport: Report;
    protected columns: ReportColumn[];
    public cabecalho: any = {};
    private endl: string = '\n';
    private sum_qtd_nf: number = 0;
    private sum_qtd_contada: number = 0;

    static readonly pr = 3; // pr is precision the number (QTD_CONTADA, QTD_NF), only '0.000'

    pdf: any;

    constructor(protected msgSvc: MessageService, protected http: HttpClient, protected route: ActivatedRoute,
        protected navSvc: NavigationService, protected router: Router, @Inject(LOCALE_ID) protected locale: string) {
    }

    ngOnInit() {
        this.routeSubscription = this.route.params.subscribe((params: Params) => {
            this.params = params.docId;
        });

        this.loadReport();
    }
    dataLoaded = false;

    protected loadReport() {
        this.http.get(environment.reportserver + 'query/loadFile/romaneio.json')
            .catch((err: HttpErrorResponse) => {
                return Observable.of<RestResponse>({ status: { result: false, message: err.error }, data: [] });
            }).subscribe((resp: RestResponse) => {
                if (resp.status.result) {
                    this.selectedReport = {
                        file: 'romaneio.json',
                        name: resp.data['name'],
                        query: resp.data['query'],
                        variables: resp.data['variables'],
                        columns: resp.data['columns'].slice(7),
                        actions: resp.data['actions']
                    };

                    let parms = {
                        id: this.params
                    }

                    this.http.get(environment.reportserver + 'query/byFile/' + this.selectedReport.file, { params: parms })
                        .subscribe((resp: RestResponse) => {
                            if (resp.data.length === 0) {
                                this.msgSvc.add({ severity: 'error', summary: 'NÃO EXISTE DADOS PARA ESSA CONSULTA', detail: 'Verificar Console' });
                                this.router.navigate(['home', 'report', 'fixed', 'inbound']);
                            }
                            else {
                                this.data = resp.data;
                                this.cabecalho = this.data[0] // Save cabecalho                        
                                this.dataLoaded = true;

                                this.data.map(
                                    (data) => {
                                        if (data.QTD_CONTADA != "-") {
                                            let qtd_contada = parseFloat(data.QTD_CONTADA)
                                            data.QTD_CONTADA = qtd_contada.toFixed(RomaneioComponent.pr)

                                            this.sum_qtd_contada = this.sum_qtd_contada + qtd_contada; // make sum
                                        }
                                        if (data.QTD_NF != "-") {
                                            let qtd_nota = parseFloat(data.QTD_NF)
                                            data.QTD_NF = qtd_nota.toFixed(RomaneioComponent.pr)
                                            this.sum_qtd_nf = this.sum_qtd_nf + qtd_nota // make sum
                                        }
                                    }
                                ) // End Map
                            }
                            console.log('Sum Qtd NF: ', this.sum_qtd_nf.toFixed(RomaneioComponent.pr))
                            console.log('Sum Qtd Contada: ', this.sum_qtd_contada.toFixed(RomaneioComponent.pr))
                        }, error => {
                            this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
                        });
                } else {
                    //console.log(resp.status.message);
                    this.msgSvc.add({ severity: 'error', summary: "Falha arquivo Json", detail: "Problema na consulta" })
                }
            });
    }
    savePdf() {
        const header_itens: any = [ // hearder columns item
            { text: 'Código', fontSize: 10, bold: true, alignment: 'center' },
            { text: 'Descrição', fontSize: 10, bold: true, alignment: 'center' },
            { text: 'Qtd NF', fontSize: 10, bold: true, alignment: 'center' },
            { text: 'Und', fontSize: 10, bold: true, alignment: 'center' },
            { text: 'Qtd Contada', fontSize: 10, bold: true, alignment: 'center' },
            //{ text: 'Nome conferente', fontSize: 10, bold: true, alignment: 'left' }
        ]
        let corpo: any = [];

        corpo.push(header_itens);

        this.data.map( // data item
            (data) => {
                let arr: any = [
                    { text: data.CODIGO_PRODUTO, fontSize: 9, alignment: 'center' },
                    { text: data.NOME_PRODUTO, fontSize: 9, alignment: 'left' },
                    { text: data.QTD_NF, fontSize: 9, alignment: 'center' },
                    { text: data.UM, fontSize: 9, alignment: 'center' },
                    { text: data.QTD_CONTADA, fontSize: 9, alignment: 'center' },
                    //{ text: data.NOME_CONFERENTE, fontSize: 9, alignment: 'center' }
                ]
                corpo.push(arr);
            }
        );

        this.pdf = {
            content: [
                { // Title report
                    text: 'Relatório Romaneio de descarga\n\n',
                    style: 'header',
                    alignment: 'center',
                    bold: true,
                    fontSize: 18
                },
                { // Name company                                       
                    table: {
                        widths: ['*', 'auto'],
                        body: [
                            [
                                {
                                    text: ['EMPRESA: NORSA REFRIGERENTES S.A.' + this.endl,
                                    'UNIDADE: NOLT - NATAL' + this.endl,
                                        'ENDEREÇO: ROD BR 304 KM-5, 8 450'
                                    ],

                                    border: [true, true, false, true]
                                },
                                {
                                    text: ['MACAIBA'],
                                    border: [false, true, true, true]
                                }
                            ],
                        ]
                    }
                },

                this.endl,
                {
                    table: { // header report
                        widths: ['*', 'auto'],
                        body: [
                            [
                                {
                                    text: [
                                        { text: 'DATA DE DESCARGA: ', bold: true }, this.getDate(this.cabecalho.DATA_DESCARGA), ' às ' + this.getHour(this.cabecalho.DATA_DESCARGA) + 'hs' + this.endl,
                                        { text: 'FORNECEDOR: ', bold: true }, this.cabecalho.NOME_FORNECEDOR + this.endl,
                                        { text: 'PLACA DE VEÍCULO: ', bold: true }, this.cabecalho.PLACA_VEICULO + this.endl,
                                        { text: 'PEDIDO: ', bold: true }, this.cabecalho.PEDIDO
                                    ],

                                    border: [true, true, false, false]
                                },
                                {
                                    text: [
                                        //{ text: 'HORA: ', bold: true, }, this.getHour(this.cabecalho.DATA_DESCARGA) + this.endl,
                                        { text: 'NOTA FISCAL: ', bold: true }, this.cabecalho.NOTA_FISCAL + this.endl,
                                        { text: 'DATA NF: ', bold: true }, this.AlterSimbolDate(this.cabecalho.DATA_NOTA_FISCAL) + this.endl,
                                        { text: 'DATA REC: ', bold: true }, this.getDate(this.cabecalho.DATA_RECEBIMENTO)
                                    ],
                                    border: [false, true, true, false]
                                }
                            ],
                        ]
                    }
                },

                { // data itens report
                    table: {
                        widths: [50, 150, '*', '*', '*'],
                        body: corpo
                    }
                },

                this.endl, this.endl,

                { // Sing Almox and Recebedor
                    table: {
                        widths: ['*'],
                        body: [
                            [
                                {
                                    text: [
                                        { text: 'Almox./ Recebedor: _________________________________ ' },
                                        { text: 'Nº. Doc. SAP Material:_________________' + this.endl },
                                        { text: 'Assinatura: ___________________________________' + this.endl },
                                        { text: 'Conferente: ' + this.data[0].NOME_CONFERENTE + this.endl },
                                        //{ text: 'Assinatura: ___________________________________', }
                                    ],

                                    border: [true, true, true, true],
                                    alignment: 'left'
                                }
                            ]
                        ]
                    }
                },

                this.endl, this.endl,

                {
                    table: {
                        widths: ['*', 'auto'],
                        body: [
                            [
                                {
                                    text: ['FRETE: ______________________________' + this.endl,
                                    'MB1C/J1B1N: _______________________' + this.endl,
                                    'ESTORNO EM: _______________________' + this.endl,
                                    'NOVO EM: ___________________________' + this.endl,
                                    'ESTORNO EF: ________________________' + this.endl,
                                        'NOVO EF: ____________________________'
                                    ],
                                    alignment: 'justify',
                                    border: [true, true, false, true]
                                },
                                {
                                    text: ['Assinatura: ________________________________' + this.endl,
                                    'Assinatura: ________________________________' + this.endl,
                                    'Assinatura: ________________________________' + this.endl,
                                    'Assinatura: ________________________________' + this.endl,
                                    'Assinatura: ________________________________' + this.endl,
                                    'Assinatura: ________________________________' + this.endl
                                    ],
                                    border: [false, true, true, true]
                                }
                            ],
                        ]
                    }
                }
            ], // End Content
            footer: {
                columns: [
                    { text: 'GTP Automation - Hunter IoT Visibility Manager', alignment: 'center' },
                ],
                fontSize: 8
            },
        }
        pdfMake.vfs = pdfFonts.pdfMake.vfs;
        pdfMake.createPdf(this.pdf).download('hunter_romaneio_' + this.cabecalho.NOTA_FISCAL);
    }
    public getDate(date: String): String {
        return date.substr(0, 10);
    }
    public getHour(date: String): String {
        return date.substr(11, date.length);
    }
    public AlterSimbolDate(date: String): String {
        while (date.indexOf("-") >= 0) {
            date = date.replace("-", "/");
        }
        return date;
    }
}