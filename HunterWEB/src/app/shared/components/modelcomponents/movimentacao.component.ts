import { AfterViewInit, ChangeDetectorRef, Component, Input } from "@angular/core";
import 'rxjs/add/operator/catch';
import { HunterDocument } from "../../model/HunterDocument";
import { HunterUser } from "../../model/HunterUser";

class TransportStub {
    constructor(public seq: number, public sku: string, public product: string, public origin: string, public destination: string, public complete: boolean) {

    }
}

@Component({
    selector: 'ordem-movimentacao',
    templateUrl: './movimentacao.component.html',
    styleUrls: ['../styles/modelcomponent.scss']
})
export class OrdemMovimentacaoComponent implements AfterViewInit {
    @Input("model") ordmov: HunterDocument;
    @Input("single") single: string;
    userComp: HunterUser;
    movStatus: string;
    viewInitialized: boolean;
    transpStubs: TransportStub[];
    constructor(private cdRef: ChangeDetectorRef) { }

    ngAfterViewInit(): void {
        let apoComp = this.ordmov.siblings.find(ds => ds.model.metaname === 'APOCOMPLETEMOV');
        if (apoComp !== undefined)
            this.userComp = apoComp.user;
        this.transpStubs = Array.of(...[]);
        switch (this.ordmov.status) {
            case "LOAD":
                this.movStatus = 'CARREGAR';
                break;
            case "UNLOAD":
                this.movStatus = 'DESCARREGAR';
                break;
            case "ARMPROD":
                this.movStatus = 'ARMAZENAR_PRODUCAO';
                break;
            case "ARMCAM":
                this.movStatus = 'ARMAZENAR_CAMINHAO';
                break;
            case "REPACK":
                this.movStatus = 'REEMBALAGEM';
                break;
            default:
                this.movStatus = this.ordmov.status;
        }
        this.ordmov.transports.map(dtr => {
            let seq = dtr.seq;
            let thing = dtr.thing;
            let address = dtr.address;
            let origin = dtr.origin;
            let product = thing.product;
            let sku = product.sku;
            let prdName = product.name;
            let dest = address.name;
            let orig = origin === null || origin === undefined ? '' : origin.name;

            if (thing.siblings.length > 0) {
                let products = thing.siblings.map(ts => ts.product);

                sku = products.map(prd => prd.sku).reduce((prev: string, curr: string) => curr + (prev.length === 0 ? '' : ' \| ' + prev), '');
                prdName = products.map(prd => prd.name).reduce((prev: string, curr: string) => curr + (prev.length === 0 ? '' : ' \| ' + prev), '');
            }

            if (this.single === null || this.single === undefined || this.single === thing.id)
                this.transpStubs.push(new TransportStub(seq, sku, prdName, orig, dest, thing.address !== null && thing.address !== undefined && thing.address.id === address.id));
        });
        this.cdRef.detectChanges();
        this.viewInitialized = true;
    }
}
