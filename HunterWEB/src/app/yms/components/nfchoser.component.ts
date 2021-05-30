import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, SimpleChange, SimpleChanges } from "@angular/core";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterDocument } from "../../shared/model/HunterDocument";
import CPFCNPJ from "../../shared/utils/cpfcnpjutils";

@Component({
    selector: 'nfchooser',
    templateUrl: './nfchoser.component.html',
    styleUrls: ['../process/yms-process.scss']
})

export class NFChoserComponent implements OnInit {
    displayDialog: boolean;
    selectedDocuments: HunterDocument[] = Array.from([]);
    @Output("chosen") onChosen: EventEmitter<HunterDocument[]> = new EventEmitter();
    @Output("closed") onClosed: EventEmitter<void> = new EventEmitter();
    @Input("cnpj") cnpj: any;
    @Input("custcode") custcode: any;
    documents: HunterDocument[];

    columns: ReportColumn[] = [
        {
            field: 'code',
            header: 'CÓDIGO NF',
            type: '',
            nullString: '-',
            width: '7ch'
        },
        {
            field: 'props.serie_nf',
            header: 'SÉRIE NF',
            type: '',
            nullString: '-',
            width: '5ch'
        },
        {
            field: 'props.data_nf',
            header: 'DATA NF',
            type: '',
            nullString: '-',
            width: '3ch'
        },
        {
            field: 'props.transp_sap',
            header: 'TRANSPORTE SAP',
            type: '',
            nullString: '-',
            width: '8ch'
        }
    ];

    constructor(private changeDetector: ChangeDetectorRef, private http: HttpClient) {
        this.selectedDocuments;
    }

    ngOnInit() {

    }

    ngOnChanges(changes: SimpleChanges) {
        const cnpj: SimpleChange = changes.cnpj;
        const custcode: SimpleChange = changes.custcode;

        if (cnpj !== undefined) {
            if (cnpj.currentValue === undefined || cnpj.currentValue === '' || !CPFCNPJ.validateCNPJ(cnpj.currentValue))
                this.displayDialog = false;
            else {
                this.cnpj = cnpj.currentValue.toUpperCase();
                this.loadDocument('SUPPLIER', CPFCNPJ.toPlain(this.cnpj));
            }
        }
        if (custcode !== undefined) {
            if (custcode.currentValue === undefined || custcode.currentValue === '')
                this.displayDialog = false;
            else {
                this.custcode = custcode.currentValue.toUpperCase();
                this.loadDocument('CUSTOMER', this.custcode);
            }
        }
    }

    nfChosen(): void {
        this.onChosen.emit(this.selectedDocuments);
        this.displayDialog = false;
    }

    close() {
        this.displayDialog = false;
        this.onClosed.emit();
    }

    loadDocument(personType: string, personCode: string) {
        this.http.get(environment.customserver + 'document/bypersontypecode/' + personType + '/' + personCode)
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            })
            .subscribe((msg: HunterDocument[]) => {
                console.log('NFChoser',msg);
                this.documents = msg.map(nf => {
                    let ret = new HunterDocument(nf);
                    let dfTranspSap = ret.fields.find(df => df.field.metaname === 'TICKET' || df.field.metaname === 'TRANSPORTE_SAP');
                    let dfDtNf = ret.fields.find(df => df.field.metaname === 'DATA_NF');
                    let dfSerieNf = ret.fields.find(df => df.field.metaname === 'SERIE_NF');

                    ret.props['data_nf'] = dfDtNf === undefined ? '' : dfDtNf.value;
                    ret.props['serie_nf'] = dfSerieNf === undefined ? '' : dfSerieNf.value;
                    ret.props['transp_sap'] = dfTranspSap === undefined ? '' : dfTranspSap.value;
                    return ret;
                });
                this.displayDialog = true;
                this.changeDetector.detectChanges();
            });
    }
}
