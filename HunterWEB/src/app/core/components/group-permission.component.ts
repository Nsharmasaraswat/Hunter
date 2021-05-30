import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';
import { TokenService } from '../../security/services/token.service';
import { HunterPermission } from '../../shared/model/HunterPermission';


@Component({
    templateUrl: './group-permission.component.html'
})
export class GroupPermissionComponent implements OnInit {
    available: HunterPermission[];
    selected: HunterPermission[];
    user: HunterPermission[];

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute, private tokenSvc: TokenService) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.http.get(environment.coreserver + 'permission/all')
                .subscribe((permissions: HunterPermission[]) => {
                    console.log(permissions);
                    this.available = Array.from(permissions.filter(perm => perm.app === 'HunterWEB'));
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
                });
            this.http.get(environment.coreserver + 'permission/user/' + this.tokenSvc.getUid())
                .subscribe((permissions: HunterPermission[]) => {
                    console.log(permissions);
                    this.selected = permissions.length > 0 ? Array.from(permissions.filter(perm => perm.app === 'HunterWEB')) : [];
                    this.available = this.available.filter(per => this.selected.find(sel => sel.id === per.id) === undefined);
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
                });
        });
    }
}
