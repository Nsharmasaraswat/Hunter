import { HunterPermission } from './HunterPermission';
import { HunterUUIDModel } from './HunterUUIDModel';

export class HunterOrigin extends HunterUUIDModel {
    permissions: HunterPermission[];
    params: string;
    type: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            if (init.permissions != undefined)
                init.permissions.forEach((p: HunterPermission) => this.permissions.push(new HunterPermission(p)));
            this.params = init.params;
            this.type = init.type;
        }
    }
}