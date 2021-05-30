import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './documentmodel-list.component.html'
})
export class DocumentModelListComponent implements OnInit {

    data: any[] = [];
    selected: any = null;

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
        this.http.get(environment.processserver + 'documentmodel/all')
            .subscribe((msg: any) => {
                console.log(msg);

                this.data = msg.map(dm => {
                    dm.parent = dm.parent == null ? { name: 'None' } : dm.parent;
                    dm.createdAtText = this.formatDate(new Date(dm.createdAt));
                    return dm;
                });

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onDelete() {

        if (this.selected == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        this.http.delete(environment.processserver + 'documentmodel/' + this.selected.id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add(
                        {
                            severity: 'success',
                            summary: 'DOCUMENT MODEL DELETED',
                            detail: 'DocumentModel was deleted with success'
                        });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DOCUMENT MODEL NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onNewDocumentModel() {
        // go to edit page with id metaname 0
        this.router.navigate(['home/process/listDocuments/0/edit']);
    }

    onEdit() {

        if (this.selected == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        // go to edit page
        this.router.navigate(['home/process/listDocuments/' + this.selected.metaname + '/edit']);
    }

    onUnselect(e) {
        this.selected = e.data;
        if (this.selected == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'An selection is necessary to perform the action' });
            return;
        }
        // get metaname and redirect route
        this.router.navigate(['home/process/listDocuments/' + this.selected.metaname]);
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

        return [day, month, year].join('/');
    }

}
