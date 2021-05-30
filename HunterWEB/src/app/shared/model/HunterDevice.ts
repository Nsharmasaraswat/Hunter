import { HunterPort } from "./HunterPort";
import { HunterSource } from "./HunterSource";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterDevice extends HunterUUIDModel {
    ports: HunterPort[];
    source: HunterSource;
    properties: any;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.ports = Array.from([]);
            if (init.ports != undefined)
                init.ports.forEach((p: HunterPort) => this.ports.push(new HunterPort(p)));
            this.properties = Object.assign({});
            if (init.properties != undefined)
                for (let i in init.properties)
                    this.properties[i] = init.properties[i];
            this.source = new HunterSource(init.source);
        }
    }
}