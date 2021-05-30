import { HunterAddress } from "./HunterAddress";
import { HunterField } from "./HunterField";
import { HunterModelField } from "./HunterModelField";
import { HunterPerson } from "./HunterPerson";
import { HunterProduct } from "./HunterProduct";
import { HunterThing } from "./HunterThing";
import { HunterUser } from "./HunterUser";
import { HunterUUIDModel } from "./HunterUUIDModel";

export class HunterDocument extends HunterUUIDModel {
	code: string;

	model: HunterDocumentModel;
	person: HunterPerson;
	parent: HunterDocument;
	user: HunterUser;

	items: HunterDocumentItem[];
	things: HunterDocumentThing[];
	fields: HunterField[];
	siblings: HunterDocument[];
	transports: HunterDocumentTransport[];

	props: any;

	constructor(init: any) {
		super(init);
		if (init != undefined) {
			this.code = init.code;

			if (init.model !== null)
				this.model = new HunterDocumentModel(init.model);
			if (init.person !== null)
				this.person = new HunterPerson(init.person);
			if (init.parent !== null)
				this.parent = new HunterDocument(init.parent);
			if (init.user !== null)
				this.user = new HunterUser(init.user);

			this.items = Array.from([]);
			if (init.items !== undefined)
				init.items.forEach(di => this.items.push(new HunterDocumentItem(di)));
			this.things = Array.from([]);
			if (init.things !== undefined)
				init.things.forEach(dt => this.things.push(new HunterDocumentThing(dt)));
			this.fields = Array.from([]);
			if (init.fields !== undefined)
				init.fields.forEach(df => this.fields.push(new HunterField(df)));
			this.siblings = Array.from([]);
			if (init.siblings !== undefined)
				init.siblings.forEach((d: HunterDocument) => this.siblings.push(new HunterDocument(d)));
			this.transports = Array.from([]);
			if (init.transports !== undefined)
				init.transports.forEach((dtr: HunterDocumentTransport) => this.transports.push(new HunterDocumentTransport(dtr)));

			this.props = Object.assign({});
			if (init.props !== undefined)
				for (let i in init.props)
					this.props[i] = init.props[i];
		}
	}
}

export class HunterDocumentModel extends HunterUUIDModel {
	fields: HunterModelField[];

	constructor(init: any) {
		super(init);
		if (init != undefined) {
			this.fields = Array.from([]);
			if (init.fields != undefined)
				init.fields.forEach((dmf: HunterModelField) => this.fields.push(new HunterModelField(dmf)));
		}
	}
}

export class HunterDocumentThing extends HunterUUIDModel {
	thing: HunterThing;

	constructor(init: any) {
		super(init);
		if (init != undefined) {
			this.thing = new HunterThing(init.thing);
		}
	}
}

export class HunterDocumentItem extends HunterUUIDModel {
	product: HunterProduct;
	qty: number;
	properties: any;
	measureUnit: string;

	constructor(init: any) {
		super(init);
		if (init != undefined) {
			this.product = new HunterProduct(init.product);
			this.qty = init.qty;
			this.properties = Object.assign({});
			if (init.properties != undefined)
				for (let i in init.properties)
					this.properties[i] = init.properties[i];
			this.measureUnit = init.measureUnit;
		}
	}
}

export class HunterDocumentTransport extends HunterUUIDModel {
	seq: number;
	thing: HunterThing;
	address: HunterAddress;
	origin: HunterAddress;
	thing_id: string;
	address_id: string;
	origin_id: string;

	constructor(init: any) {
		super(init);
		if (init != undefined) {
			this.seq = init.seq;
			this.thing_id = init.thing_id;
			this.address_id = init.address_id;
			this.origin_id = init.origin_id;
			if (init.thing != null)
				this.thing = new HunterThing(init.thing);
			if (init.address != null)
				this.address = new HunterAddress(init.address);
			if(init.origin !== null)
				this.origin = new HunterAddress(init.origin);
		}
	}
}