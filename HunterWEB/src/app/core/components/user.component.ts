import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';


@Component({
    templateUrl: './user.component.html'
})
export class UserComponent implements OnInit {

    id: string;
    groups: any[] = [];
    data: any = {};
    password: string;

    selectedGroup: any = null;
    selectedPerson: any = null;

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
        this.selectedGroup = null;
        this.selectedPerson = null;
        this.loadGroups();
        this.loadData();
        // this.loadPeople();
    }

    loadGroups() {
        this.http.get(environment.coreserver + 'group/all')
            .subscribe((msg: any) => {
                console.log(msg);
                this.groups = msg;
                if(this.data != null && this.data.grpId != null && this.selectedGroup == null) {
                    for(let grp of this.groups) {
                        if(grp.id === this.data.grpId) {
                            this.selectedGroup = grp;
                            break;
                        }
                    }
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    loadData() {
        this.http.get(environment.coreserver + 'user/' + this.id)
            .subscribe((msg: any) => {
                this.data = msg;
                if(msg.grpId != null) {
                    console.log(" USR TEM GRP ");
                    for(let grp of this.groups) {
                        if(grp.id === msg.grpId) {
                            this.selectedGroup = grp;
                            break;
                        }
                    }
                }
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        this.http.post(environment.coreserver + 'user', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DATA SAVED', detail: 'Data saved to database' });
                    if (this.id === '0')
                        this.router.navigate(['home/core/user/' + msg.id]);
                    else
                        this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DATA NOT SAVED', detail: 'Check console for more details' });
                    console.log(msg);
                }
            });
    }

    onBack() {
        this.backRoute();
    }

    onDiscard() {
        this.refresh();
    }

    onGroupChange() {
        this.data.grpId = this.selectedGroup.id;
        console.log(this.data);
    }

    onPersonChange() {
        this.data.person = this.selectedPerson;
        console.log(this.data);
    }

    getHeader() {
        return this.id === '0' ? 'New User' : 'Editing User';
    }

    backRoute() {
        // go to list page
        this.router.navigate(['home/core/userList/']);
    }

}
