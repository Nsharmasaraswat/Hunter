import { LabelData } from "../../custom-solar/classes/LabelData";

export class PrintTagOrder {
    docname: string;
    prodname: string;
    sku: string;
    properties: LabelData;
    metadata: Map<string, string>;
    qty: number;
    printed: number;
    status: string;
    thing: string;

    constructor(public device: string, public document: string, public product: string) {
    }

    // parseLabelData(labelData: LabelData) {
    //     this.properties = new Map();
    //     for (let key in labelData) {
    //         if (labelData.hasOwnProperty(key)) {
    //             this.properties.set(key, labelData[key]);
    //         }
    //     }
    // }
}