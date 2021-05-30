import { AfterViewInit, ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import 'rxjs/add/operator/catch';
import { ReportColumn } from "../../../report/interfaces/report.interface";
import { HunterField } from "../../model/HunterField";
import { HunterThing } from "../../model/HunterThing";

class ThingStub {
    constructor(public id: string, public addr_name: string, public sku, public name: string, public status: string, public lot: string, public quantity: number, public man: Date, public exp: Date, public cre: Date, public vol: number, public thing: HunterThing) {
        man.setHours(0, 0, 0, 0);
        exp.setHours(0, 0, 0, 0);
    }
}

@Component({
    selector: 'pallet-history',
    templateUrl: './pallet-history.component.html',
    styleUrls: ['../styles/modelcomponent.scss']
})
export class PalletHistoryComponent implements AfterViewInit, OnChanges {
    @Input("model") thing: HunterThing;
    @Input("mode") mode: string;

    pallets: ThingStub[];
    rowGroupMetadata: any;
    columns: ReportColumn[] = [
        {
            field: 'sku',
            header: 'CÓDIGO',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'name',
            header: 'PRODUTO',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'addr_name',
            header: 'LOCAL',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'quantity',
            header: 'QUANTIDADE',
            type: 'NUMBER',
            nullString: '',
            width: '10%'
        },
        {
            field: 'lot',
            header: 'LOTE',
            type: 'TEXT',
            nullString: '',
            width: '10%'
        },
        {
            field: 'man',
            header: 'FABRICAÇÃO',
            type: 'DATE',
            nullString: '',
            width: '10%'
        },
        {
            field: 'exp',
            header: 'VENCIMENTO',
            type: 'DATE',
            nullString: '',
            width: '10%'
        },
    ]



    constructor(private cdRef: ChangeDetectorRef) { }

    ngAfterViewInit(): void {
        let thArr: ThingStub[] = Array.of(...[]);
        if(this.thing){
            let id = this.thing.id;

            if (this.thing.siblings !== undefined) {
                if (this.thing.siblings.length > 0) {
                    thArr = thArr.concat(this.thing.siblings.map(th => this.getThingStub(id, th)));
                } else {
                    thArr.push(this.getThingStub(id, this.thing));
                }
            }
            if (this.mode === 'resumed') {//TODO: Sem Concentração... revisar e melhorar
                let tmpThArr: ThingStub[] = Array.of(...[]);
    
                thArr = thArr.map(thStub => {
                    thStub.addr_name = thStub.addr_name.indexOf('.') > 0 ? thStub.addr_name.substring(0, thStub.addr_name.indexOf('.')) : thStub.addr_name;
                    return thStub;
                });
                for (let ts of thArr) {
                    if (tmpThArr.find(th => th.id === ts.id) === undefined) {
                        let tmpArr: ThingStub[] = thArr.filter(thStub => {
                            let sameProduct = ts.sku === thStub.sku;
                            let sameLot = ts.lot === thStub.lot;
                            let sameExp = ts.exp.getTime() === thStub.exp.getTime();
                            let sameMan = ts.man.getTime() === thStub.man.getTime();
                            let sameQty = ts.quantity === thStub.quantity;
                            let sameAddress = ts.addr_name === thStub.addr_name;
    
                            if (sameProduct && sameLot && sameExp && sameMan && sameQty && sameAddress) {
                                thStub.id = thStub.sku + thStub.lot + thStub.exp.getTime() + thStub.man.getTime() + thStub.quantity + thStub.addr_name;
                                return true;
                            }
                            return false;
                        });
                        ts.vol = tmpArr.length;
                        tmpThArr.push(ts);
                    }
                }
                thArr = tmpThArr.sort((ts1: ThingStub, ts2: ThingStub) => {
                    if (ts1 === undefined && ts2 === undefined) return 0;
                    if (ts2 === undefined) return -1;
                    if (ts1 === undefined) return 1;
                    let sku1 = ts1.sku;
                    let sku2 = ts2.sku;
    
                    if (sku1 !== sku2) return sku1.localeCompare(sku2);
                    let man1 = ts1.man.getTime();
                    let man2 = ts2.man.getTime();
    
                    return man2 - man1;
                });
            }
        }
        this.pallets = Array.of(...thArr);
        this.updateRowGroupMetaData();
        this.cdRef.detectChanges();
    }

    groupBy = function (data, key) { // `data` is an array of objects, `key` is the key (or property accessor) to group by
        // reduce runs this anonymous function on each element of `data` (the `item` parameter,
        // returning the `storage` parameter at the end
        return data.reduce(function (storage, item) {
            // get the first instance of the key by which we're grouping
            var group = item[key];

            // set `storage` for this instance of group to the outer scope (if not empty) or initialize it
            storage[group] = storage[group] || [];

            // add this item to its group within `storage`
            storage[group].push(item);

            // return the updated storage to the reduce function, which will then loop through the next 
            return storage;
        }, {}); // {} is the initial value of the storage
    };

    ngOnChanges(changes: SimpleChanges) {
        //this.updateRowGroupMetaData();
    }

    getThingStub(id: string, th: HunterThing) {
        let lotPf: HunterField = th.properties.find(pr => pr.field.metaname === 'LOT_ID');
        let qtyPf: HunterField = th.properties.find(pr => pr.field.metaname === 'QUANTITY');
        let manPf: HunterField = th.properties.find(pr => pr.field.metaname === 'MANUFACTURING_BATCH');
        let expPf: HunterField = th.properties.find(pr => pr.field.metaname === 'LOT_EXPIRE');
        let addr_name: string = th.address === null || th.address === undefined ? 'EXPEDIDO' : th.address.name;
        let sku: string = th.product.sku;
        let name: string = th.product.name;
        let status: string = th.status;
        let lot: string = lotPf === undefined || lotPf.value.length === 36 ? '' : lotPf.value.substring(0, 4);
        let qty: string = qtyPf === undefined || th.id === id ? '0' : qtyPf.value;//th.id === id significa que o thing é do palete, coloca quantidade 0
        let man: string = manPf === undefined ? undefined : manPf.value;
        let exp: string = expPf === undefined ? undefined : expPf.value;
        let quantity: number = +qty;
        let manufacture: Date = man === undefined ? new Date(0) : this.getDate(man);
        let expiry: Date = man === undefined ? new Date(0) : this.getDate(exp);
        let creation: Date = th.createdAt;

        return new ThingStub(id, addr_name, sku, name, status, lot, quantity, manufacture, expiry, creation, 1, th);
    }

    getDate(val: string): Date {
        //2020-02-19T12:00:00
        if (val === '0000-00-00T00:00:00')
            return new Date(0);
        if (val.match(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/))
            return new Date(val.replace(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/, "$2/$1/$3 $4:$5:$6"));
        if (val.match(/(\d{2})\/(\d{2})\/(\d{4})/))
            return new Date(val.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$2/$1/$3"));
        if (val.match(/(\d{4})-(\d{2})-(\d{2})[T](\d{1,2}):(\d{1,2}):(\d{1,2})/))
            return new Date(val.replace("T", " "));
        if (val.match(/(\d{4})-(\d{2})-(\d{2})/))
            return new Date(val.replace(/(\d{4})-(\d{2})-(\d{2})/, "$2/$3/$1"));
        console.log('Invalid Date', val);
        return new Date(0);
    }

    onSort() {
        this.updateRowGroupMetaData();
    }

    updateRowGroupMetaData() {
        this.rowGroupMetadata = {};

        if (this.pallets) {
            for (let i = 0; i < this.pallets.length; i++) {
                let rowData = this.pallets[i];
                let id = rowData.id;

                if (i == 0)
                    this.rowGroupMetadata[id] = { index: 0, size: 1 };
                else {
                    let previousRowData = this.pallets[i - 1];
                    let previousRowGroup = previousRowData.id;

                    if (id === previousRowGroup)
                        this.rowGroupMetadata[id].size++;
                    else
                        this.rowGroupMetadata[id] = { index: i, size: 1 };
                }
            }
        }
    }
}