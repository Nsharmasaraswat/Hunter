import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterPermission extends HunterUUIDModel {
    app: string;
    icon: string;
    params: any;
    route: string;
    properties: any;
    category: HunterPermissionCategory;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.app = init.app;
            this.icon = init.icon;
            this.params = JSON.parse(init.params);
            this.route = init.route;
            if (init.properties !== undefined)
                this.properties = init.properties;
            else
                this.properties = {};
            if (init.category !== undefined)
                this.category = new HunterPermissionCategory(init.category);
        }
    }
}

export class HunterPermissionCategory extends HunterUUIDModel {
    icon: string;
    parent: HunterPermissionCategory;
    permissions: HunterPermission[];

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.icon = init.icon;
            this.parent = new HunterPermissionCategory(init.category);
            this.permissions = Array.from([]);
            if (init.fields != undefined)
                init.permissions.forEach((p: HunterPermission) => this.permissions.push(p));
        }
    }
}