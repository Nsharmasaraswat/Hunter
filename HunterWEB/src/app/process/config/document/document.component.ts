import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';


@Component({
    templateUrl: './document.component.html'
})
export class DocumentComponent implements OnInit {

    data: any = {};

    id: string;
    type: string;

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.type = data.type;
            this.id = data.id;
            this.refresh();
        });
    }

    refresh() {
        this.data = {};
        this.loadData();
    }

    loadData() {
        this.http.get(environment.processserver + 'document/' + this.id)
            .subscribe((msg: any) => {

                this.data = {
                    id: this.id,
                    metaname: msg.metaname,
                    name: msg.name,
                    code: msg.code,
                    status: msg.status,
                    model: this.type
                }

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERROR WHILE TRYING TO LOAD DATA', detail: 'Check console for more details' });
            });
    }

    getHeader() {
        return this.id === '0' ? 'New Document' : 'Editing Document';
    }

    onSave() {
        // validate data
        if (this.validateBeforeSave()) {
            // save document 
            this.http.post(environment.processserver + 'document', this.data)
                .subscribe((msg: any) => {
                    console.log(msg);
                    if (msg != null) {
                        this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                        this.router.navigate(['home/process/listDocuments/' + this.type + '/', msg.id]);
                    }
                });
        } else {
            // report error
            this.msgSvc.add({ severity: 'error', summary: 'IMPOSSIBLE TO SAVE', detail: 'Please fill all fields' });
        }
    }

    onDiscard() {
        // reload page
        this.refresh();
        this.msgSvc.add({ severity: 'error', summary: 'CHANGES DISCARDED', detail: 'Changes were not applied' });
    }

    onBack() {
        // back to route
        this.router.navigate(['home/process/listDocuments/' + this.type]);
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
                    this.router.navigate(['home/process/listDocuments/' + this.type]);
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    validateBeforeSave() {
        console.log(this.data);
        // return false if any of the conditions below is satisfied
        return !(this.data.name == null || this.data.metaname == null || this.data.code == null);
    }

    onDocument() {
        // go to document route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.id]);
    }

    onItem() {
        // go to item route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.id + '/item']);
    }

    onThing() {
        // go to thing route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.id + '/thing']);
    }

    onField() {
        // go to field route passing data id
        this.router.navigate(['home/process/listDocuments/' + this.type + '/' + this.id + '/field']);
    }
}
