import { HunterAddress } from "../model/HunterAddress";
import { HunterDocument, HunterDocumentItem, HunterDocumentThing } from "../model/HunterDocument";
import { HunterField } from "../model/HunterField";
import { HunterProduct } from "../model/HunterProduct";
import { HunterThing } from "../model/HunterThing";

const QUANTITY_PROPERTY_FIELD_ID: string = '3901544d-6d1b-11e9-a948-0266c0e70a8c';
const LOT_PROPERTY_FIELD_ID: string = '3943501b-6d1b-11e9-a948-0266c0e70a8c';
const MANUFACTURE_PROPERTY_FIELD_ID: string = '395900b1-6d1b-11e9-a948-0266c0e70a8c';
const EXPIRE_PROPERTY_FIELD_ID: string = '392f72d2-6d1b-11e9-a948-0266c0e70a8c';

export class ConferenceProduct {
    product: HunterProduct;
    address: HunterAddress;
    lot_id: string;
    lot_expire: any;
    manufacturing_batch: Date;
    quantity: number;
    volumes: number;
    thing_ids: string[];
    measureUnit: string;
    internal_lot: string;
    rodape: string;
    qcl: string;
    wrong: boolean;
    type: string;

    constructor() {
        this.product = null;
        this.address = null;
        this.lot_id = '';
        this.lot_expire = new Date();
        this.manufacturing_batch = new Date();
        this.quantity = 0;
        this.volumes = 0;
        this.thing_ids = [];
        this.measureUnit = '';
        this.internal_lot = '';
        this.rodape = '';
        this.qcl = '';
        this.wrong = false;
    }

    public getPrintCount(dts: HunterDocumentThing[]): number {
        let things = dts.filter(dt => dt.thing.units.length !== 0 && dt.thing.product.id === this.product.id).map(dt => dt.thing);//has unit
        things = things.filter(t => t.properties.find(pf => pf.field.id == LOT_PROPERTY_FIELD_ID).value === this.lot_id);//same lot
        things = things.filter(t => +t.properties.find(pf => pf.field.id == QUANTITY_PROPERTY_FIELD_ID).value.replace(/,/g, '.') === this.quantity);//same quantity
        things = things.filter(t => {
            let tExp = t.properties.find(pf => pf.field.id == EXPIRE_PROPERTY_FIELD_ID).value;

            if (this.lot_expire != 'Indeterminado' && tExp != 'Indeterminado')
                return ConferenceProduct.toDate(tExp).getTime() === this.lot_expire.getTime();
            else
                return this.lot_expire === tExp;
        });//same expiry
        things = things.filter(t => ConferenceProduct.toDate(t.properties.find(pf => pf.field.id == MANUFACTURE_PROPERTY_FIELD_ID).value).getTime() === this.manufacturing_batch.getTime());//same manufacturing
        things = things.filter(t => (t.address === null && this.address === null) || (t.address.id == this.address.id));
        return things.length;
    }

    public filterThings(dts: HunterDocumentThing[]): HunterThing[] {
        let things = dts.filter(dt => dt.thing.product.id === this.product.id && dt.thing.units.length === 0).map(dt => dt.thing);//don't have unit
        things = things.filter(t => t.properties.find(pf => pf.field.id == LOT_PROPERTY_FIELD_ID).value === this.lot_id);//same lot
        things = things.filter(t => +t.properties.find(pf => pf.field.id == QUANTITY_PROPERTY_FIELD_ID).value.replace(/,/g, '.') === this.quantity);//same quantity
        things = things.filter(t => {
            let tExp = t.properties.find(pf => pf.field.id == EXPIRE_PROPERTY_FIELD_ID).value;

            if (this.lot_expire != 'Indeterminado' && tExp != 'Indeterminado')
                return ConferenceProduct.toDate(tExp).getTime() === this.lot_expire.getTime();
            else
                return this.lot_expire === tExp;
        });//same expiry
        things = things.filter(t => ConferenceProduct.toDate(t.properties.find(pf => pf.field.id == MANUFACTURE_PROPERTY_FIELD_ID).value).getTime() === this.manufacturing_batch.getTime());//same manufacturing
        things = things.filter(t => (t.address === null && this.address === null) || (t.address.id == this.address.id));
        return things;
    }

    public filterThingsNoQuantity(dts: HunterDocumentThing[]): HunterThing[] {
        let things = dts.filter(dt => dt.thing.product.id === this.product.id && dt.thing.units.length === 0).map(dt => dt.thing);//don't have unit
        things = things.filter(t => t.properties.find(pf => pf.field.id == LOT_PROPERTY_FIELD_ID).value === this.lot_id);//same lot
        things = things.filter(t => {
            let tExp = t.properties.find(pf => pf.field.id == EXPIRE_PROPERTY_FIELD_ID).value;

            if (this.lot_expire != 'Indeterminado' && tExp != 'Indeterminado')
                return ConferenceProduct.toDate(tExp).getTime() === this.lot_expire.getTime();
            else
                return this.lot_expire === tExp;
        });//same expiry
        things = things.filter(t => ConferenceProduct.toDate(t.properties.find(pf => pf.field.id == MANUFACTURE_PROPERTY_FIELD_ID).value).getTime() === this.manufacturing_batch.getTime());//same manufacturing
        things = things.filter(t => (t.address === null && this.address === null) || (t.address.id == this.address.id));
        return things;
    }

    public static getSPAPrdArray(retordconf: HunterDocument): ConferenceProduct[] {
        let prdConf = new Array();

        for (let retOrdConfItem of retordconf.items) {
            let exp = retOrdConfItem.properties['lot_expire'];
            let sQty = retOrdConfItem.qty;
            let lot: string = retOrdConfItem.properties['lot_id'];
            let expire: any = exp == 'Indeterminado' ? exp : this.toDate(exp);
            let manufacture: Date = this.toDate(retOrdConfItem.properties['manufacturing_batch']);
            let quantity: number = +(+sQty).toFixed(3);
            let volumes: number = 1;
            let insert: boolean = true;
            let thing = new HunterThing({ id: "123" });

            thing.address = retordconf.code.indexOf('.') > 0 ? new HunterAddress({ metaname: 'EXTERNA', name: 'EXPEDIDO' }) : new HunterAddress({ id: '27998254-563a-11e9-b375-005056a19775', name: 'ALMOXARIFADO' });
            for (let ind = 0; ind < prdConf.length; ind++) {
                let prdCnf: ConferenceProduct = prdConf[ind];
                let sameProduct = prdCnf.product.id === retOrdConfItem.product.id;
                let sameLot = prdCnf.lot_id === lot;
                let sameExp = (prdCnf.lot_expire == 'Indeterminado' && expire == 'Indeterminado') || (prdCnf.lot_expire != 'Indeterminado' && expire != 'Indeterminado' && prdCnf.lot_expire.getTime() === expire.getTime());
                let sameMan = prdCnf.manufacturing_batch.getTime() === manufacture.getTime();
                let sameQty = prdCnf.quantity === quantity;

                if (sameProduct && sameLot && sameExp && sameMan && sameQty) {
                    prdConf[ind].thing_ids.push(thing.id);
                    prdConf[ind].volumes = volumes;
                    insert = false;
                    break;
                }
            }

            if (insert) {
                let prdCnf: ConferenceProduct = new ConferenceProduct();

                prdCnf.address = thing.address;
                prdCnf.product = retOrdConfItem.product;
                prdCnf.lot_id = lot;
                prdCnf.lot_expire = expire;
                prdCnf.manufacturing_batch = manufacture;
                prdCnf.quantity = quantity;
                prdCnf.volumes = volumes;
                prdCnf.thing_ids.push(thing.id);
                prdCnf.measureUnit = retOrdConfItem.measureUnit;
                prdConf.push(prdCnf);
            }
        }
        return prdConf.sort((a, b) => {
            if (a.product.sku < b.product.sku) return -1;
            if (a.product.sku > b.product.sku) return 1;
            if (a.lot_id < b.lot_id) return -1;
            if (a.lot_id > b.lot_id) return 1;
            return 0;
        });
    }

    public static getPrdArray(retordconf: HunterDocument): ConferenceProduct[] {
        let prdConf = new Array();

        for (let retOrdConfItem of retordconf.items) {
            let exp = retOrdConfItem.properties['lot_expire'];
            let sQty = retOrdConfItem.properties['quantity'].replace(/,/g, '.');
            let lot: string = retOrdConfItem.properties['lot_id'];
            let expire: any = exp == 'Indeterminado' ? exp : this.toDate(exp);
            let manufacture: Date = this.toDate(retOrdConfItem.properties['manufacturing_batch']);
            let quantity: number = +(+sQty).toFixed(3);
            let volumes: number = retOrdConfItem.properties.hasOwnProperty('volumes') ? +retOrdConfItem.properties['volumes'] : 0;
            let insert: boolean = true;
            let thing = new HunterThing({ id: "123" });

            thing.address = retordconf.code.indexOf('.') > 0 ? new HunterAddress({ metaname: 'EXTERNA', name: 'EXPEDIDO' }) : new HunterAddress({ id: '27998254-563a-11e9-b375-005056a19775', name: 'ALMOXARIFADO' });
            for (let ind = 0; ind < prdConf.length; ind++) {
                let prdCnf: ConferenceProduct = prdConf[ind];
                let sameProduct = prdCnf.product.id === retOrdConfItem.product.id;
                let sameLot = prdCnf.lot_id === lot;
                let sameExp = (prdCnf.lot_expire == 'Indeterminado' && expire == 'Indeterminado') || (prdCnf.lot_expire != 'Indeterminado' && expire != 'Indeterminado' && prdCnf.lot_expire.getTime() === expire.getTime());
                let sameMan = prdCnf.manufacturing_batch.getTime() === manufacture.getTime();
                let sameQty = prdCnf.quantity === quantity;

                if (sameProduct && sameLot && sameExp && sameMan && sameQty) {
                    prdConf[ind].thing_ids.push(thing.id);
                    prdConf[ind].volumes = volumes;
                    insert = false;
                    break;
                }
            }

            if (insert) {
                let prdCnf: ConferenceProduct = new ConferenceProduct();

                prdCnf.address = thing.address;
                prdCnf.product = retOrdConfItem.product;
                prdCnf.lot_id = lot;
                prdCnf.lot_expire = expire;
                prdCnf.manufacturing_batch = manufacture;
                prdCnf.quantity = quantity;
                prdCnf.volumes = volumes;
                prdCnf.thing_ids.push(thing.id);
                prdCnf.measureUnit = retOrdConfItem.measureUnit;
                prdConf.push(prdCnf);
            }
        }
        return prdConf.sort((a, b) => {
            if (a.product.sku < b.product.sku) return -1;
            if (a.product.sku > b.product.sku) return 1;
            if (a.lot_id < b.lot_id) return -1;
            if (a.lot_id > b.lot_id) return 1;
            return 0;
        });
    }

    public static getPrdCPAArray(retordconf: HunterDocument): ConferenceProduct[] {
        let prdConf = new Array();

        for (let tp of retordconf.things.map(dt => dt.thing)) {
            if (tp.siblings === undefined || tp.siblings.length === 0) {
                console.log('Thing with no siblings', tp.id);
                continue;
            }
            let t = tp.siblings[0];
            let thAddress: HunterAddress = t.address === null || t.address === undefined ? t.address = new HunterAddress({ metaname: 'EXTERNA', name: 'EXPEDIDO' }) : t.address

            if (t.name !== 'CONTAINER') {
                let exp = t.properties.find(pr => pr.field.metaname === 'LOT_EXPIRE').value;
                let lot: string = t.properties.find(pr => pr.field.metaname === 'LOT_ID').value;
                let expire: any = exp == 'Indeterminado' ? exp : this.toDate(exp);
                let manufacture: Date = this.toDate(t.properties.find(pr => pr.field.metaname === 'MANUFACTURING_BATCH').value);
                let quantity: number = +t.properties.find(pr => pr.field.metaname === 'QUANTITY').value.replace(/,/g, '.');
                let fILot: HunterField = t.properties.find(pr => pr.field.metaname === 'INTERNAL_LOT');
                let fObs: HunterField = t.properties.find(pr => pr.field.metaname === 'LABEL_OBS');
                let insert: boolean = true;


                for (let ind = 0; ind < prdConf.length; ind++) {
                    let prdCnf: ConferenceProduct = prdConf[ind];
                    let sameProduct = prdCnf.product.id === t.product.id;
                    let sameLot = prdCnf.lot_id === lot;
                    let sameExp = (prdCnf.lot_expire === 'Indeterminado' && expire === 'Indeterminado') || (prdCnf.lot_expire !== 'Indeterminado' && expire !== 'Indeterminado' && prdCnf.lot_expire.getTime() === expire.getTime());
                    let sameMan = prdCnf.manufacturing_batch.getTime() === manufacture.getTime();
                    let sameQty = prdCnf.quantity === quantity;
                    let sameAddress = false;

                    if ((prdCnf.address === undefined || prdCnf.address === null) && (thAddress === undefined || thAddress === null))
                        sameAddress = true;
                    else if (prdCnf.address === undefined || prdCnf.address === null)
                        sameAddress = false;
                    else if (thAddress === undefined || thAddress === null)
                        sameAddress = false;
                    else if (prdCnf.address.id === thAddress.id)
                        sameAddress = true;

                    if (sameProduct && sameLot && sameExp && sameMan && sameQty && sameAddress) {
                        prdConf[ind].thing_ids.push(t.id);
                        prdConf[ind].volumes++;
                        insert = false;
                        break;
                    }
                }

                if (insert) {
                    let prdCnf: ConferenceProduct = new ConferenceProduct();
                    let prodUM = t.product.fields.find(pf => pf.model.metaname === 'packing_type');

                    prdCnf.address = thAddress;
                    prdCnf.product = t.product;
                    prdCnf.lot_id = lot;
                    prdCnf.lot_expire = expire;
                    prdCnf.manufacturing_batch = manufacture;
                    prdCnf.quantity = quantity;
                    prdCnf.volumes = 1;
                    prdCnf.thing_ids.push(t.id);
                    prdCnf.internal_lot = fILot === undefined ? '' : fILot.value;
                    prdCnf.rodape = fObs === undefined ? '' : fObs.value;
                    prdCnf.measureUnit = prodUM === undefined ? '' : prodUM.value.toUpperCase();
                    prdConf.push(prdCnf);
                }
            }
        }
        return prdConf.sort((a, b) => {
            if ((a.address === undefined || a.address === null) && (b.address === undefined || b.address === null)) return 0;
            if (a.address === undefined || a.address === null) return 1;
            if (b.address === undefined || b.address === null) return -1;
            if (a.address.metaname !== b.address.metaname) return a.address.metaname.localeCompare(b.address.metaname);
            if (a.product.sku !== b.product.sku) return a.product.sku.localeCompare(b.product.sku);
            return b.quantity - a.quantity;
        });
    }

    public static getPrdThArray(things: HunterThing[]): ConferenceProduct[] {
        let prdConf = new Array();

        for (let tp of things) {
            let t = tp.siblings === undefined || tp.siblings.length === 0 ? tp : tp.siblings[0];

            let exp = t.properties.find(pr => pr.field.metaname === 'LOT_EXPIRE').value;
            let lot: string = t.properties.find(pr => pr.field.metaname === 'LOT_ID').value;
            let expire: any = exp == 'Indeterminado' ? exp : this.toDate(exp);
            let manufacture: Date = this.toDate(t.properties.find(pr => pr.field.metaname === 'MANUFACTURING_BATCH').value);
            let quantity: number = +t.properties.find(pr => pr.field.metaname === 'QUANTITY').value.replace(/,/g, '.');
            let fILot: HunterField = t.properties.find(pr => pr.field.metaname === 'INTERNAL_LOT');
            let fObs: HunterField = t.properties.find(pr => pr.field.metaname === 'LABEL_OBS');
            let address: HunterAddress = t.address;
            let insert: boolean = true;


            for (let ind = 0; ind < prdConf.length; ind++) {
                let prdCnf: ConferenceProduct = prdConf[ind];
                let sameProduct = prdCnf.product.id === t.product.id;
                let sameLot = prdCnf.lot_id === lot;
                let sameExp = (prdCnf.lot_expire === 'Indeterminado' && expire === 'Indeterminado') || (prdCnf.lot_expire !== 'Indeterminado' && expire !== 'Indeterminado' && prdCnf.lot_expire.getTime() === expire.getTime());
                let sameMan = prdCnf.manufacturing_batch.getTime() === manufacture.getTime();
                let sameQty = prdCnf.quantity === quantity;
                let sameAddress = false;

                if (prdCnf.address === undefined && address === undefined)
                    sameAddress = true;
                else if (prdCnf.address === undefined)
                    sameAddress = false;
                else if (address === undefined)
                    sameAddress = false;
                else if (prdCnf.address.id === address.id)
                    sameAddress = true;

                if (sameProduct && sameLot && sameExp && sameMan && sameQty && sameAddress) {
                    prdConf[ind].thing_ids.push(t.id);
                    prdConf[ind].volumes++;
                    insert = false;
                    break;
                }
            }

            if (insert) {
                let prdCnf: ConferenceProduct = new ConferenceProduct();
                let prodUM = t.product.fields.find(pf => pf.model.metaname === 'packing_type');

                prdCnf.address = t.address;
                prdCnf.product = t.product;
                prdCnf.lot_id = lot;
                prdCnf.lot_expire = expire;
                prdCnf.manufacturing_batch = manufacture;
                prdCnf.quantity = quantity;
                prdCnf.volumes = 1;
                prdCnf.thing_ids.push(t.id);
                prdCnf.internal_lot = fILot === undefined ? '' : fILot.value;
                prdCnf.rodape = fObs === undefined ? '' : fObs.value;
                prdCnf.measureUnit = prodUM === undefined ? '' : prodUM.value.toUpperCase();
                prdConf.push(prdCnf);
            }
        }
        return prdConf.sort((a, b) => {
            if (a.address === undefined && b.address === undefined) return 0;
            if (a.address === undefined) return 1;
            if (b.address === undefined) return -1;
            if (a.address.metaname < b.address.metaname) return -1;
            if (a.address.metaname > b.address.metaname) return 1;
            return 0;
        });
    }

    public static getPrdQCArray(retordconf: HunterDocument): ConferenceProduct[] {
        let prdConf = new Array();

        for (let t of retordconf.things.map(dt => dt.thing)) {
            if (t.name !== 'CONTAINER') {
                let exp = t.properties.find(pr => pr.field.metaname === 'LOT_EXPIRE').value;
                let lot: string = t.properties.find(pr => pr.field.metaname === 'LOT_ID').value;
                let expire: any = exp == 'Indeterminado' ? exp : this.toDate(exp);
                let manufacture: Date = this.toDate(t.properties.find(pr => pr.field.metaname === 'MANUFACTURING_BATCH').value);
                let quantity: number = +t.properties.find(pr => pr.field.metaname === 'QUANTITY').value.replace(/,/g, '.');
                let fILot: HunterField = t.properties.find(pr => pr.field.metaname === 'INTERNAL_LOT');
                let fObs: HunterField = t.properties.find(pr => pr.field.metaname === 'LABEL_OBS');
                let address: HunterAddress = t.address;
                let prod: HunterProduct = t.product;
                let insert: boolean = true;


                for (let ind = 0; ind < prdConf.length; ind++) {
                    let prdCnf: ConferenceProduct = prdConf[ind];
                    let sameProduct = prdCnf.product.id === prod.id;
                    let sameLot = prdCnf.lot_id === lot;
                    let sameExp = (prdCnf.lot_expire === 'Indeterminado' && expire === 'Indeterminado') || (prdCnf.lot_expire !== 'Indeterminado' && expire !== 'Indeterminado' && prdCnf.lot_expire.getTime() === expire.getTime());
                    let sameMan = prdCnf.manufacturing_batch.getTime() === manufacture.getTime();
                    let sameQty = prdCnf.quantity === quantity;
                    let sameAddress = (prdCnf.address === null && address === null) || (prdCnf.address === undefined && address === undefined) || ((prdCnf.address !== undefined && address !== undefined) && (prdCnf.address.id === address.id));

                    if (sameProduct && sameLot && sameExp && sameMan && sameQty && sameAddress) {
                        prdConf[ind].thing_ids.push(t.id);
                        prdConf[ind].volumes++;
                        insert = false;
                        break;
                    }
                }

                if (insert) {
                    let prdCnf: ConferenceProduct = new ConferenceProduct();
                    let prodUM = t.product.fields.find(pf => pf.model.metaname === 'packing_type');

                    prdCnf.address = address;
                    prdCnf.product = prod;
                    prdCnf.lot_id = lot;
                    prdCnf.lot_expire = expire;
                    prdCnf.manufacturing_batch = manufacture;
                    prdCnf.quantity = quantity;
                    prdCnf.volumes = 1;
                    prdCnf.thing_ids.push(t.id);
                    prdCnf.internal_lot = fILot === undefined ? '' : fILot.value;
                    prdCnf.rodape = fObs === undefined ? '' : fObs.value;
                    prdCnf.measureUnit = prodUM === undefined ? '' : prodUM.value.toUpperCase();
                    prdConf.push(prdCnf);
                }
            }
        }
        return prdConf.sort((a, b) => {
            if (a.product.sku < b.product.sku) return -1;
            if (a.product.sku > b.product.sku) return 1;
            if (a.lot_id < b.lot_id) return -1;
            if (a.lot_id > b.lot_id) return 1;
            return 0;
        });
    }

    public static getPrdInvArray(inventory: HunterDocument): ConferenceProduct[] {
        let prdConf = new Array();

        for (let dt of inventory.things) {
            let thing = dt.thing;
            let exp = thing.properties.find(pr => pr.field.metaname === 'LOT_EXPIRE').value;
            let lot: string = thing.properties.find(pr => pr.field.metaname === 'LOT_ID').value;
            let expire: any = exp == 'Indeterminado' ? exp : this.toDate(exp);
            let manufacture: Date = this.toDate(thing.properties.find(pr => pr.field.metaname === 'MANUFACTURING_BATCH').value);
            let quantity: number = +thing.properties.find(pr => pr.field.metaname === 'QUANTITY').value.replace(/,/g, '.');
            let address: HunterAddress = thing.address;
            let insert: boolean = true;

            for (let ind = 0; ind < prdConf.length; ind++) {
                let prdCnf: ConferenceProduct = prdConf[ind];
                let sameProduct = prdCnf.product.id === thing.product.id
                let sameLot = prdCnf.lot_id === lot
                let sameExp = (prdCnf.lot_expire == 'Indeterminado' && expire == 'Indeterminado') || (prdCnf.lot_expire != 'Indeterminado' && expire != 'Indeterminado' && prdCnf.lot_expire.getTime() === expire.getTime());
                let sameMan = prdCnf.manufacturing_batch.getTime() === manufacture.getTime()
                let sameQty = prdCnf.quantity === quantity;
                let sameAddress = (prdCnf.address === null && address === null) || (prdCnf.address === undefined && address === undefined) || ((prdCnf.address !== undefined && address !== undefined) && (prdCnf.address.id === address.id));

                if (sameProduct && sameLot && sameExp && sameMan && sameQty && sameAddress) {
                    prdConf[ind].thing_ids.push(thing.id);
                    prdConf[ind].volumes += 1;
                    insert = false;
                    break;
                }
            }

            if (insert) {
                let prdCnf: ConferenceProduct = new ConferenceProduct();

                prdCnf.product = thing.product;
                prdCnf.lot_id = lot;
                prdCnf.lot_expire = expire;
                prdCnf.manufacturing_batch = manufacture;
                prdCnf.quantity = quantity;
                prdCnf.address = address;
                prdCnf.volumes = 1;
                prdCnf.thing_ids.push(thing.id);
                prdCnf.measureUnit = inventory.items.find(di => di.product.id === thing.product.id).measureUnit;
                prdConf.push(prdCnf);
            }
        }
        return prdConf.sort((a, b) => {
            if (a.product.sku < b.product.sku) return -1;
            if (a.product.sku > b.product.sku) return 1;
            if (a.lot_id < b.lot_id) return -1;
            if (a.lot_id > b.lot_id) return 1;
            return 0;
        });
    }

    public getDocumentItem(): HunterDocumentItem {
        let di = new HunterDocumentItem({
            id: null,
            name: null,
            metaname: null,
            status: 'NOVO',
            createdAt: new Date(),
            updatedAt: new Date(),
            product: new HunterProduct(this.product),
            qty: this.quantity * this.volumes,
            measureUnit: this.measureUnit,
            properties: {
                lot_id: this.lot_id,
                lot_expire: this.lot_expire === 'Indeterminado' ? this.lot_expire : this.lot_expire,
                manufacturing_batch: this.manufacturing_batch,
                quantity: this.quantity,
                volumes: this.volumes,
                qcl: this.qcl
            }
        });
        return di;
    }

    private static toDate(dt: string): any {
        if (dt === null || dt === undefined)
            return new Date(10800000);
        else if (dt === 'Indeterminado')
            return dt;
        else
            return new Date(dt.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$2/$1/$3"));
    }
}