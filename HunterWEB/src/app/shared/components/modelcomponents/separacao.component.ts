import { AfterViewInit, ChangeDetectorRef, Component, Input } from "@angular/core";
import 'rxjs/add/operator/catch';
import { HunterDocument } from "../../model/HunterDocument";
import { HunterUser } from "../../model/HunterUser";

class OSGItemStub {
    constructor(public seq: number, public layer: number, public sku: string, public product: string, public qty: number, public measure: string, public complete: boolean) {

    }
}

@Component({
    selector: 'ordem-separacao',
    templateUrl: './separacao.component.html',
    styleUrls: ['../styles/modelcomponent.scss']
})
export class OrdemSeparacaoComponent implements AfterViewInit {
    @Input("model") picking: HunterDocument;
    osgStubs: OSGItemStub[];
    userIni: HunterUser;
    userComp: HunterUser;
    fullPallet: boolean;
    viewInitialized: boolean;
    constructor(private cdRef: ChangeDetectorRef) { }

    ngAfterViewInit(): void {
        let apoIni = this.picking.siblings.find(ds => ds.model.metaname === 'APOINICIO');
        let apoComp = this.picking.siblings.find(ds => ds.model.metaname === 'APOFINAL');

        this.osgStubs = Array.of(...[]);
        this.userIni = apoIni !== undefined ? apoIni.user : undefined;
        this.userComp = apoComp !== undefined ? apoComp.user : undefined;
        let ifpf = this.picking.fields.find(df => df.field.metaname === 'IS_FULL_PALLET');
        this.fullPallet = ifpf !== undefined && ifpf.value.toUpperCase() === 'TRUE';
        this.picking.siblings
            .filter(ds => ds.model.metaname === 'OSG' || ds.model.metaname === 'ORDMOV')
            .forEach(ds =>
                ds.items.forEach(di => {
                    let seq = di.properties['SEQ'] || '1';
                    let layer = di.properties['LAYER'] || '1';
                    let qty = di.qty;
                    let um = di.measureUnit;
                    let product = di.product;
                    let sku = product.sku;
                    let prdName = product.name;

                    this.osgStubs.push(new OSGItemStub(+seq, layer, sku, prdName, qty, um, ds.status === 'SUCESSO'));
                })
            );
        this.cdRef.detectChanges();
        this.viewInitialized = true;
    }
}
