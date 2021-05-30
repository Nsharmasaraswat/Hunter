import { HunterFieldType } from "./enum/HunterFieldType";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterModelField extends HunterUUIDModel {
    type: HunterFieldType;
    visible: boolean;
    ordem: number;
    ownerId: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.type = init.type;
            this.visible = init.visible;
            this.ordem = init.ordem;
            this.ownerId = init.ownerId;
        }
    }
}