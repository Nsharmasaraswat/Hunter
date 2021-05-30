import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../security/services/auth.service';
import { TokenService } from '../../security/services/token.service';
import { HunterUser } from '../../shared/model/HunterUser';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { MessageService } from 'primeng/components/common/messageservice';

@Component({
    selector: 'user',
    templateUrl: 'user.component.html'
})

export class UserComponent implements OnInit {

    user: HunterUser;

    constructor(private tokenSvc: TokenService, private authSvc: AuthService, private router: Router) {

    }

    ngOnInit(): void {
        this.authSvc.getUser().subscribe((data: HunterUser) => {
            this.user = data;
        })
    }

    changePassword() {
        this.router.navigate(['/home/core/changePassword/' + this.tokenSvc.getUid()]);
    }
}