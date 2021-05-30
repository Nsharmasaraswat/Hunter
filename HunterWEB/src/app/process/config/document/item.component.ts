import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs/Subscription';

import { DataTableModule, SharedModule } from 'primeng/primeng';
import { MessageService } from 'primeng/components/common/messageservice';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { environment } from '../../../../environments/environment';

@Component({
    templateUrl: './item.component.html'
})
export class ItemComponent implements OnInit {

    tempId: number = 0;

    selectedItem: any = null;
    documentId: string;
    type: string;

    itemList: any[] = [];
    productList: any[] = [];
    qty: number = 0;
    selectedProduct: any = {};

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.documentId = data.id;
            this.type = data.type;
            this.refresh();
        });
    }

    refresh() {
        this.tempId = 0;
        this.selectedItem = null;
        this.loadProductList();
        this.loadData();
        this.qty = 0;
        this.selectedProduct = {};
    }

    loadData() {
        this.http.get(environment.processserver + 'documentitem/' + this.documentId)
            .subscribe((msg: any) => {
                console.log(msg);
                this.itemList = msg.map(i => {
                    let obj = {
                        id: i.id,
                        docId: this.documentId,
                        prodId: i.product.id,
                        name: i.product.name,
                        metaname: i.product.metaname,
                        sku: i.product.sku,
                        qty: i.qty
                    };
                    console.log(obj);
                    return obj;
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadProductList() {
        this.http.get(environment.processserver + 'product/all')
            .subscribe((msg: any) => {
                this.productList = msg.map(o => {
                    let obj = {
                        id: o.id,
                        metaname: o.metaname,
                        name: o.name,
                        sku: o.sku
                    };
                    console.log(obj);
                    return obj;
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.itemList);
        this.http.post(environment.processserver + 'documentitem/list', this.itemList, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    // go back to previous route
                    this.router.navigate(['home/process/listDocuments/' + this.type + '/', this.documentId]);
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onDiscard() {
        // go back to previous route
        this.router.navigate(['home/process/listDocuments/' + this.type + '/', this.documentId]);
        this.msgSvc.add({ severity: 'error', summary: 'CHANGES DISCARDED', detail: 'Changes were not applied' });
    }

    onAdd() {
        if (this.qty < 1) {
            this.msgSvc.add({ severity: 'error', summary: 'INVALID QUANTITY', detail: 'Quantity must be greater than 0' });
            return;
        }

        let o = {
            id: String(this.tempId),
            docId: this.documentId,
            prodId: this.selectedProduct.id,
            name: this.selectedProduct.name,
            metaname: this.selectedProduct.metaname,
            sku: this.selectedProduct.sku,
            qty: Number(this.qty)
        };

        this.itemList.push(o);
        this.itemList = this.itemList.slice();
        this.tempId++;

        console.log(o);
    }

    onDelete() {

        if (this.selectedItem == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        let size = this.itemList.length;
        let id = this.selectedItem.id;

        for (let i = this.itemList.length - 1; i >= 0; i--) {
            let item = this.itemList[i];
            if (item.id == this.selectedItem.id) {
                this.itemList.splice(i, 1);
            }
        }

        this.itemList = this.itemList.slice();
    }

    onBack() {
        this.backRoute();
    }

    backRoute() {
        // back to route
        this.router.navigate(['home/process/listDocuments/' + this.type]);
    }

    onDocument() {
        // go to document route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.documentId]);
    }

    onItem() {
        // go to item route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.documentId + '/item']);
    }

    onThing() {
        // go to thing route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.documentId + '/thing']);
    }

    onField() {
        // go to field route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.documentId + '/field']);
    }
}