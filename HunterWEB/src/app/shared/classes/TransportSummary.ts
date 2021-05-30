import { HunterDocument, HunterDocumentItem } from '../model/HunterDocument';
import { HunterProduct, HunterProductField } from '../model/HunterProduct';

export class TransportSummary {
    public outboundPASummary: SummaryItem[];
    public inboundPASummary: SummaryItem[];
    public outboundMPSummary: SummaryItem[];
    public inboundMPSummary: SummaryItem[];
    public outboundPDSummary: SummaryItem[];
    public inboundPDSummary: SummaryItem[];

    constructor(private transport: HunterDocument) {
        try {
            let prdshrt: HunterDocument = transport.siblings.find(ds => ds.model.metaname === 'PRDSHORTAGE');
            let nfe = transport.siblings.filter(s => s.model.metaname === 'NFENTRADA');
            let nfs = transport.siblings.filter(s => s.model.metaname === 'NFSAIDA');

            this.outboundPASummary = Array.of(...[]);
            this.outboundMPSummary = Array.of(...[]);
            this.outboundPDSummary = Array.of(...[]);
            for (let nf of nfs) {
                for (let nfItem of nf.items) {
                    let p: HunterProduct = nfItem.product;
                    let genMov = p.model.properties['generate_ordmov'] !== undefined && p.model.properties['generate_ordmov'] === 'true'
                    let measure: string = nfItem.measureUnit;

                    if (genMov) {
                        let qty: number = nfItem.qty;
                        let sItem: SummaryItem = this.outboundPASummary.find(si => si.id === p.id);
                        let cpField: HunterProductField = p.fields.find(pf => pf.model.metaname === 'PALLET_BOX' && pf.value != '');
                        let ubField: HunterProductField = p.fields.find(pf => pf.model.metaname === 'UNIT_BOX' && pf.value != '');
                        let cp: number = cpField === undefined ? 1 : +cpField.value;
                        let ub: number = ubField === undefined ? 1 : +ubField.value;

                        if (cpField === undefined)
                            console.log('Invalida ProductField PALLET_BOX', p.sku + '' + p.name + '(' + p.id + ')');
                        if (ubField === undefined)
                            console.log('Invalida ProductField UNIT_BOX', p.sku + '' + p.name + '(' + p.id + ')');

                        if (sItem === undefined) {
                            let shrtItm: HunterDocumentItem = prdshrt === undefined ? undefined : prdshrt.items.find(spi => spi.product.id === p.id);
                            let shrtQty: number = shrtItm === undefined ? 0 : shrtItm.qty;

                            this.outboundPASummary.push(new SummaryItem(p.id, p.sku, p.name, qty, ub, cp, measure, shrtItm !== undefined, shrtQty));
                        }
                        else {
                            sItem.unidades += qty;
                            sItem.caixas = sItem.unidades / ub;
                            sItem.paletes = sItem.unidades / ub / cp;
                        }
                    } else if (p.model.metaname === 'MP') {
                        let qty: number = nfItem.qty;
                        let sItem: SummaryItem = this.outboundMPSummary.find(si => si.id === p.id);

                        if (sItem === undefined)
                            this.outboundMPSummary.push(new SummaryItem(p.id, p.sku, p.name, qty, 1, 1, measure, false, 0));
                        else {
                            sItem.unidades += qty;
                        }
                    } else {
                        let qty: number = nfItem.qty;
                        let sItem: SummaryItem = this.outboundPDSummary.find(si => si.id === p.id);

                        if (sItem === undefined)
                            this.outboundPDSummary.push(new SummaryItem(p.id, p.sku, p.name, qty, 1, 1, measure, false, 0));
                        else {
                            sItem.unidades += qty;
                        }
                    }
                }
            }

            this.inboundPASummary = Array.of(...[]);
            this.inboundMPSummary = Array.of(...[]);
            this.inboundPDSummary = Array.of(...[]);
            for (let nf of nfe) {
                for (let nfItem of nf.items) {
                    let p: HunterProduct = nfItem.product;
                    let genMov = p.model.properties['generate_ordmov'] !== undefined && p.model.properties['generate_ordmov'] === 'true'
                    let measure: string = nfItem.measureUnit;
                    let multip: number = 'FATOR_MULTIPLICATIVO' in nfItem.properties ? +nfItem.properties['FATOR_MULTIPLICATIVO'] : 1;

                    if(multip === 0) multip = 1;
                    if (genMov) {
                        let qty: number = nfItem.qty * multip;
                        let sItem: SummaryItem = this.inboundPASummary.find(si => si.id === p.id);
                        let cpField: HunterProductField = p.fields.find(pf => pf.model.metaname === 'PALLET_BOX' && pf.value != '');
                        let ubField: HunterProductField = p.fields.find(pf => pf.model.metaname === 'UNIT_BOX' && pf.value != '');
                        let cp: number = cpField === undefined ? 1 : +cpField.value;
                        let ub: number = ubField === undefined ? 1 : +ubField.value;

                        if(!nf.person.code.startsWith("07196033") && !nf.person.code.startsWith("08715757") && !nf.person.code.startsWith("10557540"))
                            qty *= ub;
                        if (cpField === undefined)
                            console.log('Invalid ProductField PALLET_BOX', p.sku + '' + p.name + '(' + p.id + ')');
                        if (ubField === undefined)
                            console.log('Invalid ProductField UNIT_BOX', p.sku + '' + p.name + '(' + p.id + ')');

                        if (sItem === undefined)
                            this.inboundPASummary.push(new SummaryItem(p.id, p.sku, p.name, qty, ub, cp, measure, false, 0));
                        else {
                            sItem.unidades += qty;
                            sItem.caixas = sItem.unidades / ub;
                            sItem.paletes = sItem.unidades / ub / cp;
                        }
                    } else if (p.model.metaname === 'MP') {
                        let qty: number = nfItem.qty;
                        let sItem: SummaryItem = this.inboundMPSummary.find(si => si.id === p.id);

                        if (sItem === undefined)
                            this.inboundMPSummary.push(new SummaryItem(p.id, p.sku, p.name, qty, 1, 1, measure, false, 0));
                        else {
                            sItem.unidades += qty;
                        }
                    } else {
                        let qty: number = nfItem.qty;
                        let sItem: SummaryItem = this.inboundPDSummary.find(si => si.id === p.id);

                        if (sItem === undefined)
                            this.inboundPDSummary.push(new SummaryItem(p.id, p.sku, p.name, qty, 1, 1, measure, false, 0));
                        else {
                            sItem.unidades += qty;
                        }
                    }
                }
            }
        } catch (e) {
            console.log(e as Error);
            throw e;
        }
    }

    static calcPAPallets(nf: HunterDocument): number {
        let ret: SummaryItem[] = Array.of(...[]);

        for (let nfItem of nf.items) {
            let p: HunterProduct = nfItem.product;
            let genMov = p.model.properties['generate_ordmov'] !== undefined && p.model.properties['generate_ordmov'] === 'true'
            let measure: string = nfItem.measureUnit;

            if (genMov) {
                let qty: number = nfItem.qty;
                let sItem: SummaryItem = ret.find(si => si.id === p.id);
                let cpField: HunterProductField = p.fields.find(pf => pf.model.metaname === 'PALLET_BOX' && pf.value != '');
                let ubField: HunterProductField = p.fields.find(pf => pf.model.metaname === 'UNIT_BOX' && pf.value != '');
                let cp: number = cpField === undefined ? 1 : +cpField.value;
                let ub: number = ubField === undefined ? 1 : +ubField.value;

                if (cpField === undefined)
                    console.log('Invalida ProductField PALLET_BOX', p.sku + '' + p.name + '(' + p.id + ')');
                if (ubField === undefined)
                    console.log('Invalida ProductField UNIT_BOX', p.sku + '' + p.name + '(' + p.id + ')');

                if (sItem === undefined)
                    ret.push(new SummaryItem(p.id, p.sku, p.name, qty, ub, cp, measure, false, 0));
                else {
                    sItem.unidades += qty;
                    sItem.caixas = sItem.unidades / ub;
                    sItem.paletes = sItem.unidades / ub / cp;
                }
            }
        }

        return ret.map(si => si.paletes).reduce((plA:number, plB:number) => plA + plB, 0);
    }
}

export class SummaryItem {
    public caixas: number;
    public paletes: number;

    constructor(public id: string, public sku: string, public produto: string, public unidades: number, public ub: number, public cp: number, public measure: string, public prdshrt: boolean, public prdshrtqty: number) {
        this.caixas = unidades / ub;
        this.paletes = this.caixas / cp;
    }
}