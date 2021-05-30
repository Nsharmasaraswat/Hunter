import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';


@Component({
    templateUrl: './change-password.component.html'
})

export class ChangePasswordComponent implements OnInit {

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
    }

    onSave() {
        this.http.post(environment.coreserver + 'auth/changePassword/' + this.id, this.data)
            .subscribe((msg: any) => {
                if (msg == null) {
                    this.msgSvc.add({ severity: 'error', summary: 'FAILURE', detail: 'Invalid Password' });
                    console.log(msg);
                } else if (msg.success === 'success') {
                    this.msgSvc.add({ severity: 'success', summary: 'SUCCESS', detail: 'Password Changed' });
                    this.onBack();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'FAILURE', detail: msg.message });
                    console.log(msg);
                }
            });
    }

    getHeader() {
        return "Change Password";
    }

    onBack() {
        this.router.navigate(['home']);
    }
}
