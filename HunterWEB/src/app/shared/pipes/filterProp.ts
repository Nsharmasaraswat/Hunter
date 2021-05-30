import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'filterProp',
    pure: false
})
export class FilterPropPipe implements PipeTransform {
    transform(items: any[], prop: string, value:string): any {
        if (!items || !prop) {
            return items;
        }
        // filter items array, items which match and return true will be
        // kept, false will be filtered out
        return items.filter(item => item[prop] === value);
    }
}