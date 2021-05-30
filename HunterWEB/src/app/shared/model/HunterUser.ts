import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterUser extends HunterUUIDModel {
    properties: any[];

    
    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.properties = init.properties;
        }
    }
}