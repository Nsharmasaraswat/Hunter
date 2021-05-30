import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: 'find', pure: false })

export class FindPipe implements PipeTransform {
    transform(array: any[], field: string, value: string): any[] {
        return array.find(v => v[field] === value);
    }
}