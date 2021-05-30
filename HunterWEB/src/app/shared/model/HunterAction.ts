import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterAction extends HunterUUIDModel {
    actionDef: string;
    classe: string;
    route: string;
    params: string;
    defparams: string;

    constructor(init: any) {
        super(init);
        if (init != undefined) {
            this.actionDef = init.actionDef;
            this.classe = init.classe;
            this.route = init.route;
            this.params = init.params;
            this.defparams = init.defparams;
        }
    }
}

export class HunterTask {
    id: string;
    docname: string;
    doccode: string;
    contents: string;
    cancel: boolean;
    created_at: string;
    created_at2: string;
    cancel_task: boolean;
    actions: HunterAction[];
    priority: number;

    constructor(init: any) {
        if (init != undefined) {
            this.id = init.id;
            this.doccode = init.doccode;
            this.docname = init.docname;
            this.contents = init.contents;
            this.cancel = init.cancel;
            this.created_at = init.created_at;
            this.created_at2 = init.created_at2;
            this.cancel_task = init.cancel_task;
            this.priority = init.priority;
            this.actions = Array.from([]);
            if (init.actions != undefined)
                init.actions.forEach((act: HunterAction) => this.actions.push(new HunterAction(act)));
        }
    }
}