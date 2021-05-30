import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './documentcontainer-create.component.html'
})
export class DocumentContainerCreateComponent implements OnInit {

    data: any = {};
    childrenData: any[] = [];
    selectedChildren: any[] = [];

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.loadChildrenData();
        this.refresh();
    }

    refresh() {
        this.http.get(environment.processserver + 'document/newDocument/OO')
            .subscribe((msg: any) => {
                console.log(msg);

                this.data = msg;

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadChildrenData() {
        this.http.get(environment.processserver + 'document/quickOrphanedByType/NFSAIDA')
            .subscribe((msg: any) => {
                console.log(msg);

                this.childrenData = msg.map(dm => {
                    return dm;
                });

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        // validate data
        if (this.validateBeforeSave()) {
            // save document 
            this.http.post(environment.processserver + 'document/saveChildren/' + this.data.id, this.selectedChildren)
                .subscribe((msg: any) => {
                    console.log(msg);
                    if (msg != null) {
                        this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                        this.router.navigate(['home/process/listDocumentContainer/OO/NOVO']);
                    }
                });
        } else {
            // report error
            this.msgSvc.add({ severity: 'error', summary: 'IMPOSSIBLE TO SAVE', detail: 'Please fill all fields' });
        }
    }

    onBack() {
        // back to route
        this.router.navigate(['home/process/listDocumentContainer/OO/NOVO']);
    }

    onDelete() {
        console.log('Delete clicked.');
        this.http.delete(environment.processserver + 'document/' + this.data.id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add(
                        {
                            severity: 'success',
                            summary: 'DOCUMENT DELETED',
                            detail: 'Document was deleted with success'
                        });
                    // back to route
                    this.router.navigate(['home/process/listDocumentContainer/OO/NOVO']);
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    validateBeforeSave() {
        // return false if any of the conditions below is satisfied
        return !(this.selectedChildren.length == 0);
    }
}
