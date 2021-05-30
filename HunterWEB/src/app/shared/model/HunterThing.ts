import { HunterAddress } from "./HunterAddress";
import { HunterField } from "./HunterField";
import { HunterProduct } from "./HunterProduct";
import { HunterPropertyModel } from "./HunterPropertyModel";
import { HunterUnit } from "./HunterUnit";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterThing extends HunterUUIDModel {
    units: string[];
    unitModel: HunterUnit[];
    model: HunterPropertyModel;
    product: HunterProduct;
    product_id: string;
    address: HunterAddress;
    properties: HunterField[];
    payload: string;
    siblings: HunterThing[];
    parent: HunterThing;
    parent_id: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.units = Array.from([]);
            if (init.units != undefined)
                init.units.forEach((uid: string) => this.units.push(uid));
            this.unitModel = Array.from([]);
            if (init.unitModel != undefined)
                init.unitModel.forEach((u: HunterUnit) => this.unitModel.push(new HunterUnit(u)));
            if (init.model != null)
                this.model = new HunterPropertyModel(init.model);
            if (init.product != null)
                this.product = new HunterProduct(init.product);
            if (init.address != null)
                this.address = new HunterAddress(init.address);
            if (init.parent != null)
                this.parent = new HunterThing(init.parent);
            this.product_id = init.product_id;
            this.properties = Array.from([]);
            if (init.properties != undefined)
                init.properties.forEach((pr: HunterField) => this.properties.push(new HunterField(pr)));
            this.payload = init.payload;
            this.siblings = Array.from([]);
            if (init.siblings != undefined)
                init.siblings.forEach((t: HunterThing) => this.siblings.push(new HunterThing(t)));
            this.parent_id = init.parent_id;
        }
    }
}

export class HunterThingField extends HunterField {
    propertymodel_id: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.propertymodel_id = init.propertymodel_id
        }
    }
}