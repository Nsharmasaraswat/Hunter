import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../entity/user';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';



@Component({
    templateUrl: 'login.component.html',
    styleUrls: ["./login.component.scss"],
    // animations: [
    //     trigger('reportedUser', [state('passwordTime', style({ opacity: 0 })),
    //     state('usernameTime', style({ opacity: 1 })), transition('* => *', animate('250ms 5s'))])]
})
export class LoginComponent implements OnInit {
    // formLoginState = 'usernameTime';
    showUsernameInLabel: boolean = false;
    user: User;
    username: string = "";
    pwd: string = "";
    showPass: boolean = false;
    loading = false;
    returnUrl: string;
    credentials: any;
    version: string = environment.version || 'unversioned';

    constructor(private route: ActivatedRoute, private router: Router, private authSvc: AuthService,
        private tokenSvc: TokenService, private msgSvc: MessageService) { }

    ngOnInit() {
        this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/home';
        if (this.authSvc.checkMobile()) this.router.navigate([this.returnUrl]);
        // let step1: string = this.authSvc.deriveAKey('daym', 'fb9a6127f15a0ab8c219eb9150d734c7e61e30e92c006f3515a097bf03bb7c74', 1000);
        // console.log('Iterations','1000');
        // console.log('Salt','fb9a6127f15a0ab8c219eb9150d734c7e61e30e92c006f3515a097bf03bb7c74');
        // console.log('Salt bytes', 'let byteSalt = forge.util.hexToBytes(salt)');
        // console.log('password','daym');
        // console.log('Step1 ', 'forge.util.bytesToHex(forge.pkcs5.pbkdf2(password, byteSalt, iterations, 64));');
        // console.log('Step1', step1);
    }

    reportedName() {
        this.getCredential();
    }

    backToLogin() {
        this.showUsernameInLabel = false;
        this.username = "";
    }

    login() {
        let cred: string = "";
        if (this.credentials.type == "CredentialPassword") {
            let step1: string = this.authSvc.deriveAKey(this.pwd, this.credentials.salt, 1000);
            // console.log('Iterations','1000');
            // console.log('Salt',this.credentials.salt);
            // console.log('Salt bytes', 'let byteSalt = forge.util.hexToBytes(salt)');
            // console.log('password',this.pwd);
            // console.log('Step1 ', 'forge.util.bytesToHex(forge.pkcs5.pbkdf2(password, byteSalt, iterations, 64));');
            // console.log('Step1', step1);
            // console.log('Session',this.credentials.session);
            // console.log('Step2 ', 'forge.util.bytesToHex(forge.pkcs5.pbkdf2(step1, credentials.session, iterations, 64));');
            cred = this.authSvc.deriveAKey(step1, this.credentials.session, 1000);
            // console.log('Step2',cred);
        } else {
            cred = this.pwd;
        }
        this.authSvc.login(this.username, cred).subscribe(data => {
            // console.log('Validate',data);
            if ("token" in data) {
                this.tokenSvc.setToken(data['token']);
                this.tokenSvc.setUid(data['userid']);
                this.router.navigate([this.returnUrl]);
            } else {
                this.msgSvc.add({ severity: 'error', summary: "Falha de Login", detail: "Favor verificar as credenciais informadas" });
            }
        }, error => {
            this.msgSvc.add({ severity: 'error', summary: "Falha de Login", detail: "Favor verificar as credenciais informadas" });
        });
    }

    getCredential() {
        this.authSvc.getCredential(this.username)
            .catch((err: HttpErrorResponse) => {
                return Observable.throw(err);
            })
            .subscribe(data => {
                // console.log('Pre Auth',data);
                if (data != null) {
                    this.credentials = data;
                    this.showUsernameInLabel = true;
                } else {
                    this.msgSvc.add({ severity: 'error', summary: "Falha de Login", detail: "Credenciais não encontradas" });
                    this.showUsernameInLabel = false;
                }
            }, error => {
                console.log(error);
                if (error.status == 404 || error.status == 0) {
                    this.msgSvc.add({ severity: 'error', summary: "Falha de Login", detail: 'Servidor Inalcançável' });
                } else
                    this.msgSvc.add({ severity: 'error', summary: "Falha de Login", detail: error.message });
            });
    }

    togglePassword(): void {
        this.showPass = !this.showPass;
    }
}