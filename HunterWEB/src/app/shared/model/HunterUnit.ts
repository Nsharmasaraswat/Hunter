import { HunterUnitType } from "./enum/HunterUnitType";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterUnit extends HunterUUIDModel {
    tagId: string;
    type: HunterUnitType;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.tagId = init.tagId;
            this.type = init.type;
        }
    }
}