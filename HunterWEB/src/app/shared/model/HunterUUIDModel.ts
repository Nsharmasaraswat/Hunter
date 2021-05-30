export class HunterUUIDModel {
    id: string;
    metaname: string;
    name: string;
    status: string;
    createdAt: Date;
    updatedAt: Date;

    constructor(initial: any) {
        if (initial != undefined) {
            this.id = initial.id;
            this.metaname = initial.metaname;
            this.name = initial.name;
            this.status = initial.status;
            this.createdAt = new Date(initial.createdAt);
            this.updatedAt = new Date(initial.updatedAt);
        }
    }
}