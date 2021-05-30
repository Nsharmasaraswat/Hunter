import { AfterViewInit, ChangeDetectorRef, Component, Input } from "@angular/core";
import 'rxjs/add/operator/catch';
import { ConferenceProduct } from "../../classes/ConferenceProduct";
import { HunterDocument, HunterDocumentItem } from "../../model/HunterDocument";
import { HunterField } from "../../model/HunterField";

@Component({
    selector: 'ordem-conferencia',
    templateUrl: './conferencia.component.html',
    styleUrls: ['../styles/modelcomponent.scss']
})
export class OrdemConferenciaComponent implements AfterViewInit {
    @Input("model") ordconf: HunterDocument;
    retordconfs: HunterDocument[];
    selRetordconf: HunterDocument;
    prdConf: ConferenceProduct[];
    confType: HunterField;

    constructor(private cdRef: ChangeDetectorRef) { }

    ngAfterViewInit(): void {
        this.confType = this.ordconf.fields.find(df => df.field.metaname === 'CONF_TYPE');

        this.retordconfs = this.ordconf.siblings.filter(ds => ds.model.metaname === 'RETORDCONF');
        this.ordconf.items = this.ordconf.items.sort((a: HunterDocumentItem, b: HunterDocumentItem) => {
            if (a.product.sku < b.product.sku) return -1;
            if (a.product.sku > b.product.sku) return 1;
            return 0;
        });
        if (this.retordconfs.length === 1) {
            this.selRetordconf = this.retordconfs[0];
            this.displayConference();
        }
        this.cdRef.detectChanges();
    }

    selectRetordconf(ev) {
        this.selRetordconf = ev.data;
        this.displayConference();
    }

    displayConference() {
        let sortFunc = (a: ConferenceProduct, b: ConferenceProduct) => {
            if (a.product.sku < b.product.sku) return -1;
            if (a.product.sku > b.product.sku) return 1;
            if (this.confType !== undefined && (this.confType.value === 'EPAPD' || this.confType.value === 'SPAPD')) {
                if (a.address === undefined && b.address === undefined) return 0;
                if (a.address === undefined) return 1;
                if (b.address === undefined) return -1;
                return a.address.metaname.localeCompare(b.address.metaname);
            }
            if (a.lot_id < b.lot_id) return -1;
            if (a.lot_id > b.lot_id) return 1;
            return 0;
        };

        if (this.confType !== undefined) {
            switch (this.confType.value) {
                case 'EPAPD'://por things
                    this.prdConf = ConferenceProduct.getPrdCPAArray(this.selRetordconf).sort(sortFunc)
                    break;
                case 'SPA'://por item
                case 'SPAPD'://por itens
                case 'RPAPD':
                    this.prdConf = ConferenceProduct.getSPAPrdArray(this.selRetordconf).sort(sortFunc);
                    break;
                case 'EMP'://por itens
                    this.prdConf = ConferenceProduct.getPrdArray(this.selRetordconf).sort(sortFunc);
                    break;
            }
        }
    }
}
