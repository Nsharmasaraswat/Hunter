import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';


@Component({
    templateUrl: './source.component.html'
})
export class SourceComponent implements OnInit {

    id: string;
    data: any = {};

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.id = data.id;
            this.refresh();
        });
    }

    refresh() {
        this.loadData();
    }

    loadData() {
        this.http.get(environment.coreserver + 'source/' + this.id)
            .subscribe((msg: any) => {
                console.log(msg);
                this.data = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        this.http.post(environment.coreserver + 'source', this.data)
            .subscribe((msg: any) => {
                console.log(msg);
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    this.router.navigate(['home/core/source/' + msg.id]);
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onDiscard() {
        // refreshing page to discard changes
        this.refresh();
    }

    onBack() {
        this.backRoute();
    }

    getHeader() {
        return this.id === '0' ? 'New Source' : 'Editing Source';
    }

    backRoute() {
        // go to source list page
        this.router.navigate(['home/core/sourceList/']);
    }
}
