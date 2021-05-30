import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterSource extends HunterUUIDModel {
    online: boolean;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.online = init.online;
        }
    }
}