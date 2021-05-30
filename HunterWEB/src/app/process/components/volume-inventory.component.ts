import { Component } from '@angular/core';
import { Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { SocketService } from '../../shared/services/socket.service';
import { environment } from '../../../environments/environment';
import { ProductModelComponent } from '../config/productmodel/productmodel.component';


@Component({
    selector: 'volume-inventory',
    templateUrl: 'volume-inventory.html',
    styleUrls: ['volume-inventory.scss']
})
export class VolumeInventory {
  private socketSubscription: Subscription;
  items: any = [];
  msgs: any[] = [];
  product: any;
  width: number;
  length: number;
  uheight: number;
  fheight: number;
  volume: number;
  colorCode: string;
  paint: string;
  motorType: string;
  code:string;
  t5Number: string;
  inventory: any = [];
  columns: any[] = [{ "field": "sku", "header": "SKU", "type": "java.lang.Integer", "null-string": "-" }, { "field": "rawCount", "header": "RAW COUNT", "type": "java.lang.Integer", "null-string": "-" }, { "field": "count", "header": "COUNT", "type": "java.lang.Integer", "null-string": "-" }];
  private stream: any;

  constructor(private http: HttpClient, private socket: SocketService) {

  }

  ngOnInit(): void {
    this.http.get(environment.processserver + "product/all").subscribe(data => {
      console.log(data);
      this.items = data;
      this.items.sort((n1,n2) => {
        if(n1.sku.length === n2.sku.length) {
          if(n1.sku > n2.sku)
            return 1;
          else if (n1.sku < n2.sku)
            return -1;
        } else if (n1.sku.length > n2.sku.length)
          return 1;
        else if (n1.sku.length < n2.sku.length)
          return -1;
        return 0;
      });
    });
  }

  calculate(event: KeyboardEvent){
    var lcCount = this.volume / ((this.width / 1000) * (this.fheight / 1000) * (this.length / 1000));
    this.inventory.push({sku: this.product.sku, rawCount: lcCount.toFixed(2), count: Math.round(lcCount)});
  }

  fillData(event: KeyboardEvent){
    this.width = eval(this.product.fields.filter(f => f.metaname == 'WIDTH')[0].value);
    this.length = eval(this.product.fields.filter(f => f.metaname == 'LENGTH')[0].value);
    this.fheight = eval(this.product.fields.filter(f => f.metaname == 'FOLDEDHEIGHT')[0].value);
    this.uheight = eval(this.product.fields.filter(f => f.metaname == 'UNFOLDEDHEIGHT')[0].value);
    this.colorCode = this.product.fields.filter(f => f.metaname == 'COLORCODE')[0].value;
    this.paint = this.product.fields.filter(f => f.metaname == 'PAINT')[0].value;
    this.motorType = this.product.fields.filter(f => f.metaname == 'MOTORTYPE')[0].value;
    this.code = this.product.fields.filter(f => f.metaname == 'CODE')[0].value;
    this.t5Number = this.product.fields.filter(f => f.metaname == 'T5NUMBER')[0].value;
    console.log('Length: ' + this.length + ' Width: ' + this.width + ' Unfolded Height: ' + this.uheight + ' Folded Height: ' + this.fheight);
  }

  clearInventory(event: KeyboardEvent){
    this.inventory.length = 0;
  }
}
