import { HunterField } from "./HunterField";
import { HunterModelField } from "./HunterModelField";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterPerson extends HunterUUIDModel {
    code: string;
    model: HunterPersonModel;
    fields: HunterField[];

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.code = init.code;
            this.model = new HunterPersonModel(init.model);
            this.fields = Array.from([]);
            if (init.fields != undefined)
                init.fields.forEach((psf: HunterField) => this.fields.push(new HunterField(psf)));
        }
    }
}

export class HunterPersonModel extends HunterUUIDModel {
    fields: HunterModelField[];
    codedesc: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.fields = Array.from([]);
            if (init.fields != undefined)
                init.fields.forEach((psmf: HunterModelField) => this.fields.push(psmf));
            this.codedesc = init.codedesc;
        }
    }
}