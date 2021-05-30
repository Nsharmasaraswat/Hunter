import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './productmodel-list.component.html'
})
export class ProductModelListComponent implements OnInit {

    selected: any = null;
    data: any[] = [];

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.refresh();
    }

    refresh() {
        this.selected = null;
        this.loadData();
    }

    loadData() {
        this.http.get(environment.processserver + 'productmodel/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg.map(d => {
                    d.createdAtText = this.formatDate(new Date(d.createdAt));
                    return d;
                });
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onDelete() {
        if (this.selected == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'A selection is necessary to perform the productmodel' });
            return;
        }

        this.http.delete(environment.processserver + 'productmodel/' + this.selected.id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA DELETED', detail: 'Data was deleted with success' });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onNew() {
        // go to create/editing page with id = 0
        this.router.navigate(['home/process/productmodel/0']);
    }

    onEdit() {
        // go to create/editing page 
        this.router.navigate(['home/process/productmodel/' + this.selected.id]);
    }

    onUnselect(e) {
        this.selected = e.data;
        this.onEdit();
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