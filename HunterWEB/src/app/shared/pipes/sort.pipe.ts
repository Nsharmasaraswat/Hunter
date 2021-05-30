import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "sort" })
export class ArraySortPipe implements PipeTransform {
  transform(array: any[], field: string, direction?: number): any[] {
    if (direction == null) direction = 1;
    if (array != null) {
      array.sort((a: any, b: any) => {
        const propertyA: number | string = this.getProperty(a, field);
        const propertyB: number | string = this.getProperty(b, field);

        if (propertyA < propertyB) {
          return -1 * direction;
        } else if (propertyA > propertyB) {
          return 1 * direction;
        } else {
          return 0;
        }
      });
    }
    return array;
  }

  private getProperty(value: { [key: string]: any }, key: string): number | string {
    if (value == null || typeof value !== 'object') {
      return undefined;
    }

    const keys: string[] = key.split('.');
    let result: any = value[keys.shift()];

    for (const key of keys) {
      if (result == null) { // check null or undefined
        return undefined;
      }

      result = result[key];
    }

    return result;
  }
}
