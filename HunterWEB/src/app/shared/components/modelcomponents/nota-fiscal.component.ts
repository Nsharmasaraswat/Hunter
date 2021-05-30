import { AfterViewInit, ChangeDetectorRef, Component, Input } from "@angular/core";
import 'rxjs/add/operator/catch';
import { HunterDocument } from "../../model/HunterDocument";
import CPFCNPJ from "../../utils/cpfcnpjutils";

class NfStub {
    constructor(public sku: string, public product: string, public qty: number, public measure: string, public po: string) {

    }
}

@Component({
    selector: 'nota-fiscal',
    templateUrl: './nota-fiscal.component.html',
    styleUrls: ['../styles/modelcomponent.scss']
})
export class NotaFiscalComponent implements AfterViewInit {
    @Input("model") nf: HunterDocument;
    items: NfStub[];
    personName: string;
    personLabel: string;
    origdestLabel: string;
    origdestName: string;
    docMIGOLabel: string;
    docMIGO: string;
    transpSAP: string;
    ztrans: boolean;
    constructor(private cdRef: ChangeDetectorRef) { }

    ngAfterViewInit(): void {
        let trSapField = this.nf.fields.find(df => df.field.metaname === 'TRANSPORTE_SAP' || df.field.metaname === 'TICKET');
        let origdestField = this.nf.fields.find(df => df.field.metaname === 'ORIGIN' || df.field.metaname === 'DESTINATION');
        let docMigoField = this.nf.fields.find(df => df.field.metaname === 'DOC_MIGO');
        let ztransField = this.nf.fields.find(df => df.field.metaname === 'ZTRANS');

        switch (this.nf.model.metaname) {
            case 'NFENTRADA':
                this.personName = CPFCNPJ.format(this.nf.person.code) + ' - ' + this.nf.person.name;
                this.personLabel = 'Fornecedor: ';
                this.origdestLabel = 'Origem: ';
                this.docMIGOLabel = 'MIGO: ';
                break;
            case 'NFSAIDA':
                this.personName = this.nf.person.code + ' - ' + this.nf.person.name;
                this.personLabel = 'Cliente: ';
                this.origdestLabel = 'Destino: ';
                this.docMIGOLabel = '';
                break;
            default:
                this.personName = '';
                this.personLabel = '';
                this.transpSAP = '';
                this.origdestLabel = '';
                this.origdestName = '';
                this.docMIGOLabel = '';
                break;
        }
        this.ztrans = ztransField === undefined || ztransField.value !== 'N'
        this.transpSAP = trSapField === undefined ? '' : trSapField.value;
        this.origdestName = origdestField === undefined ? '' : origdestField.value;
        this.docMIGO = docMigoField === undefined ? '' : docMigoField.value;
        this.items = Array.of(...[]);
        this.nf.items.map(item => this.items.push(new NfStub(item.product.sku, item.product.name, item.qty, item.measureUnit, item.properties['DOC_COMPRAS'])));
        this.items.sort((a: NfStub, b: NfStub) => {
            if (a === null && b === null) return 0;
            if (a === null) return 1;
            if (b === null) return -1;
            if (a.sku === null && b.sku === null) return 0;
            if (a.sku === null) return 1;
            if (b.sku === null) return -1;
            if (a.sku === b.sku) return 0;
            return a.sku > b.sku ? 1 : -1;
        });
        this.cdRef.detectChanges();
    }
}
