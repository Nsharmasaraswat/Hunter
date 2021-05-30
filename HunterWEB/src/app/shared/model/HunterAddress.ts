import { HunterField } from "./HunterField";
import { HunterModelField } from "./HunterModelField";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterAddress extends HunterUUIDModel {
    model: HunterAddressModel;
    fields: HunterField[];
    siblings: HunterAddress[];
    parent_id: string;
    wkt: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.model = new HunterAddressModel(init.model);
            this.fields = Array.from([]);
            if (init.fields != undefined)
                init.fields.forEach((af: HunterField) => this.fields.push(new HunterField(af)));
            this.siblings = Array.from([]);
            if (init.siblings !== undefined)
                init.siblings.forEach((a: HunterAddress) => this.siblings.push(new HunterAddress(a)));
            this.parent_id = init.parent_id;
            this.wkt = init.wkt;
        }
    }
}

export class HunterAddressModel extends HunterUUIDModel {
    fields: HunterModelField[];
    classe: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.fields = Array.from([]);
            if (init.fields != undefined)
                init.fields.forEach((amf: HunterModelField) => this.fields.push(new HunterModelField(amf)));
            this.classe = init.classe;
        }
    }
}