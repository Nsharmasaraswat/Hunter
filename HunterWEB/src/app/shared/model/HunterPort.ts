import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterPort extends HunterUUIDModel {
    portId: number;
    properties: any;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.portId = init.portId;
            this.properties = Object.assign({});
			if (init.properties != undefined)
				for (let i in init.properties)
					this.properties[i] = init.properties[i];
        }
    }
}