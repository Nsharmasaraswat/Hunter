import { RawDataType } from "../model/enum/RawDataType";
import { HunterUnit } from "../model/HunterUnit";

export class RawData {
    tagId: string;
    source: string;
    device: string;
    port: number;
    payload: any;
    ts: number;
    type: RawDataType;
    unit: HunterUnit;

    constructor(init: any) {
        if (init != undefined) {
            this.tagId = init.tagId;
            this.source = init.source;
            this.device = init.device;
            this.port = init.port;
            this.payload = init.payload;
            this.ts = init.ts;
            if (init.type !== undefined)
                this.type = init.type;
            if (init.unit !== undefined)
                this.unit = new HunterUnit(init.unit);
        }
    }
}