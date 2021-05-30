import { HttpClient } from '@angular/common/http';
import { Component, HostListener, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Rx';
import { environment } from '../../../environments/environment';
import { TokenService } from '../../security/services/token.service';
import { HunterAction, HunterTask } from '../../shared/model/HunterAction';
import { HunterPermission, HunterPermissionCategory } from '../../shared/model/HunterPermission';
import { NavigationService } from '../../shared/services/navigation.service';
import { SocketService } from '../../shared/services/socket.service';
import RestStatus from '../../shared/utils/restStatus';



@Component({
    templateUrl: './view-tasks.component.html',
    styleUrls: ['./view-tasks.component.scss']//,encapsulation: ViewEncapsulation.None //https://stackoverflow.com/a/50159982 - Access child component within scss
})
export class ViewTasksComponent implements OnInit, OnDestroy {
    private navigationSubscription: Subscription;
    private routeSubscription: Subscription;
    private socketSubscription: Subscription;

    permission: HunterPermission;
    data: HunterTask[];

    stream: Observable<any>;

    requiresConfirmation: boolean = false;
    disableAction: boolean = false;
    prioritizable: boolean = false;

    action: any;

    socketTimeout;
    updateTimeout;

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute,
        private navSvc: NavigationService, private confirmationService: ConfirmationService) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(data => {
            if (this.socketSubscription && !this.socketSubscription.closed) {
                this.socketSubscription.unsubscribe();
            }
            if (this.navigationSubscription && !this.navigationSubscription.closed) {
                this.navigationSubscription.unsubscribe();
            }
            this.navigationSubscription = this.navSvc.getItems().subscribe((pCat: HunterPermissionCategory[]) => {
                for (let category of pCat) {
                    for (let menu of category.permissions) {
                        if (menu.route === this.router.url) {
                            this.permission = new HunterPermission(menu);
                            if (this.permission.params !== null) {
                                this.requiresConfirmation = this.permission.params['task_requires_confirmation'] !== undefined && this.permission.params['task_requires_confirmation'] === 'true';
                                this.prioritizable = this.permission.params['task_prioritizable'] !== undefined && this.permission.params['task_prioritizable'] === 'true';
                            } else {
                                this.requiresConfirmation = false;
                                this.prioritizable = false;
                            }
                            if (this.permission.properties['datelabel'] === undefined) this.permission.properties.datelabel = 'Data/Hora';
                            if (this.permission.properties['docnamelabel'] === undefined) this.permission.properties.docnamelabel = 'Documento';
                            if (this.permission.properties['contentslabel'] === undefined) this.permission.properties.contentslabel = 'Conteúdo';
                            return;
                        }
                    }
                }
            });
            this.data = Array.of(...[]);
            this.stream = this.socket.connect(environment.wsprocess + 'tasks/' + this.token.getToken() + "/" + data.taskdef);
            this.socketTimeout = setTimeout(() => {
                this.msgSvc.add({ severity: 'success', summary: 'NENHUMA TAREFA DISPONÍVEL', detail: "Monitorando tarefas em segundo plano" });
                this.socket.hideLoadIndicator();
            }, 5000);
            this.socketSubscription = this.stream.subscribe(
                (msg: any) => {
                    clearTimeout(this.socketTimeout);
                    if (msg.constructor === Array) {
                        msg = msg.sort(this.taskSorter);
                        msg.forEach(item => {
                            this.addTaskToList(new HunterTask(item));
                        });
                    } else {
                        if (!msg.result) {
                            // console.log(msg.message);
                            this.msgSvc.add({ severity: 'error', summary: 'ERRO DE CONFIGURAÇÃO', detail: "Tarefa não cadastrada no sistema" });
                            this.router.navigate(['home']);
                        } else {
                            this.addTaskToList(new HunterTask(msg));
                        }
                    }
                },
                (error: any) => {
                    console.log(error);
                },
                () => {
                    clearTimeout(this.socketTimeout);
                }
            );
        });
    }

    taskSorter = (a, b) => {
        if (a.priority === undefined || b.priority === undefined) return a.created_at.localeCompare(b.created_at);
        if (a.priority === b.priority) return a.created_at.localeCompare(b.created_at);
        return a.priority - b.priority;
    }

    ngOnDestroy(): void {
        console.log("onNgDestroy");
        this.unsubscribeObservables();
    }

    @HostListener('window:beforeunload', ['$event'])
    beforeUnloadHander(event) {
        console.log("beforeUnload");
        this.unsubscribeObservables();
    }

    unsubscribeObservables() {
        if (this.routeSubscription && !this.routeSubscription.closed) {
            this.routeSubscription.unsubscribe();
            console.log("Unsubscribed Route");
        }
        if (this.navigationSubscription && !this.navigationSubscription.closed) {
            this.navigationSubscription.unsubscribe();
            console.log("Unsubscribed Navigation");
        }
        if (this.socketSubscription && !this.socketSubscription.closed) {
            this.socketSubscription.unsubscribe();
            console.log("Unsubscribed Sockets");
        }
        this.data = null;
        this.prioritizable = false;
    }

    onChange(events): void {
        console.log("AEHHHOOOO");
    }

    addTaskToList(item: HunterTask): void {
        let tmp: HunterTask[] = Array.of(...this.data);
        let found = this.data.find(it => it.id === item.id);
        // console.log(item);
        if (!item.cancel && found === undefined)
            tmp.push(item);
        else if (item.cancel && found !== undefined)
            tmp.splice(tmp.indexOf(found), 1);
        this.data = Array.of(...tmp);
    }

    confirmTask(docname: string, action: HunterAction) {
        if (this.requiresConfirmation)
            this.confirmationService.confirm({
                //key: null,
                //icon: null,
                //header: null,
                //acceptLabel: null,
                //rejectLabel: null,
                //acceptVisible: true,
                //rejectVisible: true,
                message: 'Deseja executar a tarefa "' + action.name + ' ' + docname + '"?',
                accept: () => {
                    this.runAction(action);
                },
                reject: () => {
                    this.msgSvc.add({ severity: 'info', summary: 'Cancelada', detail: 'Ação cancelada pelo usuário' });
                }
            });
        else
            this.runAction(action);
    }

    updatePriority(id: string) {
        clearTimeout(this.updateTimeout);
        this.updateTimeout = setTimeout(() => {
            let task = this.data.find(tsk => tsk.id === id);

            this.http.put(environment.processserver + 'ui/priority/' + id + '/', '' + task.priority)
                .catch(error => {
                    console.error("error catched", error);
                    return Observable.of({ result: false, message: "Error Value Emitted" });
                }).subscribe((resp: RestStatus) => {
                    if (resp.result) {
                        this.data = Array.of(...this.data.sort(this.taskSorter));
                        this.msgSvc.add({ severity: 'success', summary: "TAREFA REPRIORIZADA", detail: 'A tarefa agora tem prioridade ' + task.priority });
                    } else
                        this.msgSvc.add({ severity: 'error', summary: 'ERRO AO REPRIORIZAR TAREFA', detail: resp.message });
                }, error => {
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO REPRIORIZAR TAREFA', detail: error });
                });
        }, 1000);
    }

    private runAction(action: HunterAction) {
        if (!this.disableAction) {
            let actParams = action.params.indexOf("{") === 0 ? JSON.parse(action.params) : { 'document-id': action.params };
            let actIndex = this.data.findIndex(task => task.id === actParams['document-id']);

            this.disableAction = true;
            action.createdAt = null;
            action.updatedAt = null;
            if (actIndex > -1)
                this.data.splice(actIndex, 1);
            this.http.post(environment.processserver + 'task/action', action, { responseType: 'text' })
                .subscribe((data: string) => {
                    // console.log(data);
                    if ((data != null) && (data.indexOf("/") > -1)) {
                        setTimeout(() => this.msgSvc.add({ severity: 'success', summary: action.name, detail: "Executado com sucesso!" }), 600);
                        this.router.navigate([data]);
                    } else {
                        this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: "Documento Bloqueado" });
                    }
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: error });
                }, () => {
                    this.disableAction = false;
                });
        }
    }
}
