import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterLocation extends HunterUUIDModel {
    wkt: string;
    center: [number, number];
    mapfile: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.wkt = init.wkt;
            this.center = init.center;
            this.mapfile = init.mapfile;
        }
    }
}