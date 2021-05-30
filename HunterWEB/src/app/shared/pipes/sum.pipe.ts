import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: 'sum' })

export class SumPipe implements PipeTransform {
    transform(array: any[]): number {
        if (array === null || array === undefined || array.length === 0) return 0;
        return array.reduce((p, c) => p + c);
    }
}