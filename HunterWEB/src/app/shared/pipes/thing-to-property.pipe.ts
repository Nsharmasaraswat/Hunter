import { Pipe, PipeTransform } from "@angular/core";
import { HunterThing } from "../model/HunterThing";

@Pipe({ name: 'ttop', pure: false })

export class ThingToPropertyPipe implements PipeTransform {
    transform(ts: HunterThing[], propName: string): number[] {
        if (ts === null || ts === undefined || ts.length === 0) return Array.from([]);
        let properties = Array.prototype.concat(...ts.map(t => t.properties));

        return properties.filter(pr => pr.field.metaname === propName).map(pr => +pr.value);

    }
}