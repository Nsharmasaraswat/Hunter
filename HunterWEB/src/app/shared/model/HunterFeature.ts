import { HunterOrigin } from "./HunterOrigin";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterFeature extends HunterUUIDModel {
    origin: HunterOrigin;
    source: string;
    device: string;
    port: string;
    input: boolean;
    output: boolean;
    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.origin = new HunterOrigin(init.origin);
            this.source = init.source;
            this.device = init.device;
            this.port = init.port;
            this.input = init.input;
            this.output = init.output;
        }
    }
}