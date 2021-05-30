import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    selector: 'document-container',
    templateUrl: 'documentcontainer-list.component.html',
    styleUrls: ['documentcontainer-list.component.scss']
})

export class DocumentContainerListComponent {

    data: any[] = [];
    childrenData: any[] = [];
    containerType: String;
    containerStatus: String;
    selected: any = null;

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.containerType = data.containertype;
            this.containerStatus = data.containerstatus;
            this.refresh();
        });
    }

    refresh() {
        this.selected = null;
        this.loadData();
    }

    loadData() {
        this.http.get(environment.processserver + 'document/quickByTypeStatus/' + this.containerType + '/' + this.containerStatus)
            .subscribe((msg: any) => {
                console.log(msg);

                this.data = msg.map(dm => {
                    dm.parent = dm.parent == null ? { name: 'None' } : dm.parent;
                    return dm;
                });

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onRowSelect(e) {
        this.loadChildrenData();
    }

    loadChildrenData() {
        this.http.get(environment.processserver + 'document/quickChildrenByType/' + this.selected.id + '/' + 'NFENTRADA')
            .subscribe((msg: any) => {
                console.log(msg);

                this.childrenData = msg.map(dm => {
                    dm.parent = this.selected;
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

        this.http.delete(environment.processserver + 'document/' + this.selected.id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add(
                        {
                            severity: 'success',
                            summary: 'DOCUMENT MODEL DELETED',
                            detail: 'Document Container was deleted with success'
                        });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DOCUMENT NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onNewDocument() {
        // go to edit page with id metaname 0
        this.router.navigate(['home/process/createDocumentContainer/']);
    }

    onEdit() {

        if (this.selected == null) {
            this.msgSvc.add({ severity: 'error', summary: 'PLEASE SELECT AN IEM', detail: 'An selection is necessary to perform the action' });
            return;
        }

        // go to edit page
        this.router.navigate(['home/process/listDocuments/' + this.selected.metaname + '/edit']);
    }
}
