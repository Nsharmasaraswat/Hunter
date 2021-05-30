import { HunterModelField } from "./HunterModelField";
import { HunterPropertyModel } from "./HunterPropertyModel";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterProduct extends HunterUUIDModel {
    model: HunterProductModel;
    fields: HunterProductField[];
    sku: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.model = new HunterProductModel(init.model);
            this.fields = Array.from([]);
            if (init.fields != undefined)
                init.fields.forEach((pf: HunterProductField) => this.fields.push(new HunterProductField(pf)));
            this.sku = init.sku;
        }
    }
}

export class HunterProductModel extends HunterUUIDModel {
    fields: HunterModelField[];
    propertymodel: HunterPropertyModel;
    properties: any;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.fields = Array.from([]);
            if (init.fields != undefined)
                init.fields.forEach((pmf: HunterModelField) => this.fields.push(new HunterModelField(pmf)));
            this.propertymodel = new HunterPropertyModel(init.propertymodel);
            this.properties = Object.assign({});
			if (init.properties != undefined)
				for (let i in init.properties)
					this.properties[i] = init.properties[i];
        }
    }
}

export class HunterProductField extends HunterUUIDModel {
    model: HunterModelField;
    value: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.model = new HunterModelField(init.model);
            this.value = init.value;
        }
    }
}