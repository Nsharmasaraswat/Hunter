import { HttpClient } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';


@Component({
    selector: 'app-manage-fields',
    templateUrl: './manage-fields.component.html'
})
export class ManageFieldsComponent implements OnInit {

    @Input() option: string = '';
    @Input() model: any = {};

    newField = false;

    field: any = {};
    fieldType: any = {};

    fieldTypeList: any[] = [];
    fieldList: any[] = [];

    constructor(private msgSvc: MessageService, private http: HttpClient) {
    }

    ngOnChanges() {
        this.refresh();
    }

    ngOnInit(): void {
        this.refresh();
    }

    refresh() {
        this.newField = false;
        this.field = {};
        this.loadModelFieldList();
        this.loadTypeList();
    }

    onNewField() {
        this.newField = true;
        this.field.model = this.model.id;
    }

    onDiscardField() {
        this.newField = false;
        this.field = {};
    }

    onEditField(data) {
        console.log(data);
        this.field.id = data.id;
        this.field.name = data.name;
        this.field.metaname = data.metaname;
        this.field.status = data.status;
        this.field.type = data.type;
        this.field.model = this.model.id;
        this.fieldType = { name: data.type };
        this.newField = true;
    }

    onFieldTypeChange() {
        this.field.type = this.fieldType.name;
    }

    onDeleteField(id) {
        this.http.delete(environment.processserver + 'modelfield/' + id, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add(
                        {
                            severity: 'success',
                            summary: 'FIELD DELETED',
                            detail: 'Field was deleted with success'
                        });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'FIELD NOT DELETED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onSaveField() {
        this.http.post(environment.processserver + 'modelfield/', this.field, { responseType: 'text' })
            .subscribe(msg => {
                if (msg === 'Ok') {
                    this.msgSvc.add({ severity: 'success', summary: 'FIELD SAVED', detail: 'Document Model Field saved to database' });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'FIELD NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    loadModelFieldList() {
        this.http.get(environment.processserver  + 'modelfield/' + this.model.id)
            .subscribe((msg: any) => {

                this.fieldList = msg.map(f => {
                    f.createdAtText = this.formatDate(new Date(f.createdAt));
                    return f;
                });

                console.log(this.fieldList);

            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadTypeList() {
        this.http.get(environment.processserver + 'modelfield/type')
            .subscribe((msg: any) => {

                this.fieldTypeList = msg.map(t => {
                    return { name: t };
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
