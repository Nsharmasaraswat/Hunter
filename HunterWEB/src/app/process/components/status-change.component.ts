import { HttpClient } from '@angular/common/http';
import { Component, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenService } from '../../security/services/token.service';
import { SocketService } from '../../shared/services/socket.service';

@Component({
    templateUrl: './status-change.component.html',
    styleUrls: ['./status-change.component.scss']
})

export class StatusChangeComponent {
    data: any[] = [];
    @Input() from: string[];
    @Input() to: string[];

    socketSubscription: Subscription;
    stream: any;
    action: any;

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            this.data = [];
            this.stream = this.socket.connect(environment.wsprocess + 'tasks/' + this.token.getToken() + "/" + data.taskdef);
            this.socketSubscription = this.stream.subscribe(
                (msg: any) => {
                    console.log(msg);
                    if (msg.remove === true) {
                        const newData: any[] = [];
                        for (const item of this.data) {
                            if (item.id !== msg.id) {
                                newData.push(item);
                            }
                        }
                        this.data = newData;
                    } else {
                        this.data.push(msg);
                        this.data = this.data.slice();
                    }
                }
            );
        });
    }

    runAction() {
        this.http.post(environment.processserver + 'task/actionStateChange/' + this.from + '/' + this.to, this.action,
            { responseType: 'text' }).subscribe((data: string) => {
                this.router.navigate([data]);
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }
}
