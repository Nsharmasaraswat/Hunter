import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Component, HostListener, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { Observable } from 'rxjs/Observable';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterDocument } from "../../shared/model/HunterDocument";
import { HunterUser } from "../../shared/model/HunterUser";

interface LockedTask {
    document: HunterDocument;
    user: HunterUser;
}

class LockedTaskItem {
    constructor(public id: string, public model: string, public code: string, public status: string, public creation: Date, public user: string) {

    }
}

@Component({
    templateUrl: './locked-tasks.component.html'
})
export class LockedTasksComponent implements OnInit, OnDestroy {

    private routeSubscription: Subscription;
    private navigationSubscription: Subscription;
    lockedTasks: LockedTaskItem[];
    columns: ReportColumn[] = [
        {
            field: 'model',
            header: 'TAREFA',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        },
        {
            field: 'code',
            header: 'CÓDIGO',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        },
        {
            field: 'creation',
            header: 'DATA',
            type: 'TIMESTAMP',
            nullString: '',
            width: '15em'
        },
        {
            field: 'user',
            header: 'USUÁRIO',
            type: 'TEXT',
            nullString: '',
            width: '15em'
        }
    ]

    constructor(private http: HttpClient, private msgSvc: MessageService, private route: ActivatedRoute) {

    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(data => {
            this.loadLockedTasks();
        });
    }

    ngOnDestroy(): void {
        this.unsubscribeObservables();
    }

    @HostListener('window:beforeunload', ['$event'])
    beforeUnloadHander(event) {
        console.log("beforeUnload", event);
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
    }

    loadLockedTasks() {
        this.lockedTasks = Array.of(...[]);
        this.navigationSubscription = this.http.get(environment.processserver + 'task/inprogress')
            .catch((err: HttpErrorResponse) => {
                this.msgSvc.add({ severity: 'error', summary: "Falha ao Listar Tarefas", detail: err.error });
                return Observable.empty();
            })
            .subscribe((pCat: LockedTask[]) => this.lockedTasks = pCat.map(e => new LockedTaskItem(e.document.id, e.document.model.name, e.document.code, e.document.status, e.document.createdAt, e.user.name)));
    }

    unlockTasks(): void {
        this.http.post(environment.processserver + 'task/releaseAll', {})
            .catch((err: HttpErrorResponse) => {
                this.msgSvc.add({ severity: 'error', summary: "Falha ao Desbloquear Tarefas", detail: err.error });
                return Observable.empty();
            }).subscribe(() => {
                this.msgSvc.add({ severity: 'success', summary: "Sucesso", detail: "Tarefas Desbloqueadas" });
                this.lockedTasks = Array.of(...[]);
            });
    }

    unlockTask(id: string): void {
        this.http.post(environment.processserver + 'task/release/' + id, {})
            .catch((err: HttpErrorResponse) => {
                this.msgSvc.add({ severity: 'error', summary: "Falha ao Desbloquear Tarefa", detail: err.error });
                return Observable.empty();
            }).subscribe(() => {
                this.lockedTasks = this.lockedTasks.filter(lt => lt.id !== id);
                this.msgSvc.add({ severity: 'success', summary: "Sucesso", detail: "Tarefa Desbloqueada" });
            });
    }
}