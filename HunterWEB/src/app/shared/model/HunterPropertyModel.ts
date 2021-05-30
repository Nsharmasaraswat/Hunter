import { HunterModelField } from "./HunterModelField";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterPropertyModel extends HunterUUIDModel {
    fields: HunterModelField[];

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.fields = Array.from([]);
            if (init.fields != undefined)
                init.fields.forEach(prmf => this.fields.push(new HunterModelField(prmf)));
        }
    }
}