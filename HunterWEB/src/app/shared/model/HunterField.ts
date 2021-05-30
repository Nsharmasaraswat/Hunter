import { HunterModelField } from './HunterModelField';
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterField extends HunterUUIDModel {

    modelfield_id: string;
    field: HunterModelField;
    model: HunterModelField;
    value: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.modelfield_id = init.modelfield_id;
            this.field = new HunterModelField(init.field);
            this.model = new HunterModelField(init.model);
            this.value = init.value;
        }
    }
}