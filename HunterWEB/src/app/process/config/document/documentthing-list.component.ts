import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './documentthing-list.component.html'
})
export class DocumentThingListComponent implements OnInit {

    data: any[] = [];
    documentModels: any[] = [];
    selected: any = null;
    documentType: any;
    lstDi: any[] = [];
    things: any[] = [];

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.refresh();
        });
    }

    refresh() {
        this.documentType = null;
        this.selected = null;
        this.loadDocumentModels();
    }

    loadDocumentModels() {
        this.http.get(environment.processserver + 'documentmodel/all')
            .subscribe((msg: any) => {
                console.log(msg);

                this.documentModels = msg.map(dm => {
                    dm.parent = dm.parent == null ? { name: 'None' } : dm.parent;
                    dm.createdAtText = this.formatDate(dm.createdAt);
                    dm.label = dm.name;
                    dm.value = dm.metaname;
                    return dm;
                });

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData(documentType) {
        this.http.get(environment.processserver + 'document/bytype/' + documentType)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg.map(dt => {
                    dt.parent = dt.parent == null ? { name: 'None' } : dt.parent;
                    dt.createdAtText = this.formatDate(dt.createdAt);
                    return dt;
                });

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onDocumentModelChange(e) {
        if (this.documentType != null && this.documentType != undefined)
            this.loadData(this.documentType);
    }

    onDocumentSelected(doc) {
        this.http.get(environment.processserver + 'document/quickByTypeAndCode/' + this.documentType + "/" + doc.code)
            .subscribe((msg: any) => {
                console.log(msg);
                this.lstDi = msg.items;
                this.things = msg.things.map(t => {
                    t.parent = t.parent == null ? { name: 'None' } : t.parent;
                    t.desc = t.name.substring(0,30);
                    // t.batch = t.properties.find(p => p.metaname === 'BATCH').value;
                    //t.unit = t.unitModel[0].tagId;
                    t.createdAtText = this.formatDate(t.createdAt);
                    for(let itm of this.lstDi) {
                        if(t.sku === itm.product.sku) {
                            itm.things.push(t);
                            break;
                        }
                    }
                    return t;
                });

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    formatDate(date) {
        const d = new Date(date);
        let month = '' + (d.getMonth() + 1);
        let day = '' + d.getDate();
        const year = d.getFullYear();

        if (month.length < 2) {
            month = '0' + month;
        }

        if (day.length < 2) {
            day = '0' + day;
        }

        return [year, month, day].join('-');
    }
}