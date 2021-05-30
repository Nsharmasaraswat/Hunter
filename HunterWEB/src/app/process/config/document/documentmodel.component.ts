import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './documentmodel.component.html'
})
export class DocumentModelComponent implements OnInit {

    parents: any[] = [];
    selectedParent: any = null;

    data: any = {};
    metaname: string;

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.metaname = data.type;
            this.refresh();
        });
    }

    refresh() {
        this.data = {};
        this.selectedParent = null;
        this.loadData();
        this.loadParents();
    }

    loadData() {
        this.http.get(environment.processserver + 'documentmodel/' + this.metaname)
            .subscribe((msg: any) => {
                this.data = msg;
                if (this.data.parent != null)
                    this.selectedParent = this.data.parent;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadParents() {
        this.http.get(environment.processserver + 'documentmodel/all')
            .subscribe((msg: any) => {
                this.parents = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    getHeader() {
        return this.metaname === '0' ? 'New Document Model' : 'Editing Document Model';
    }

    onSave() {

        // check if direct self-reference
        if (this.selectedParent != null && this.selectedParent.id == this.data.id) {
            this.msgSvc.add({ severity: 'error', summary: 'IMPOSSIBLE TO SAVE', detail: 'You can not set Parent to self' });
            return;
        }

        this.http.post(environment.processserver + 'documentmodel', this.data, { responseType: 'text' })
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DOCUMENT MODEL SAVED', detail: 'DocumentModel saved to database' });
                    // reload route to enable further editing
                    if (this.metaname === '0') {
                        this.metaname = this.data.metaname;
                        this.router.navigate(['home/process/listDocuments/' + this.metaname + '/edit']);
                    }
                    else
                        this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DOCUMENT MODEL NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onDelete() {
        this.http.delete(environment.processserver + 'documentmodel/' + this.data.id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add(
                        {
                            severity: 'success',
                            summary: 'DOCUMENT MODEL DELETED',
                            detail: 'DocumentModel was deleted with success'
                        });
                    this.goBackRoute();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DOCUMENT MODEL NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onDiscard() {
        // reload page
        this.refresh();
        this.msgSvc.add({ severity: 'error', summary: 'CHANGES DISCARDED', detail: 'Changes were not applied' });
    }

    onBack() {
        // go back to previous route
        this.goBackRoute();
    }

    onParentChange() {
        this.data.parent = this.selectedParent;
    }

    onModelFields() {
        this.router.navigate(['home/process/listDocuments/' + this.metaname + '/edit/modelfields']);
    }

    goBackRoute() {
        this.router.navigate(['home/process/listDocuments']);
    }
}
