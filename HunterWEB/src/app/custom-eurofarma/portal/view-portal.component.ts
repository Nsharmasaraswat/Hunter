import { ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Observable, Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { ProcessMessage } from "../../shared/classes/ProcessMessage";
import { MainScrollerDirective } from "../../shared/directive/mainscroller.directive";
import { HunterPermission, HunterPermissionCategory } from "../../shared/model/HunterPermission";
import { NavigationService } from "../../shared/services/navigation.service";
import { SocketService } from "../../shared/services/socket.service";

interface DisplayTag {
    tagid: string,
    error: boolean,
    message: string
}

@Component({
    templateUrl: 'view-portal.component.html',
    styleUrls: ['view-portal.component.scss']
})

export class ViewPortalComponent implements OnInit, OnDestroy {
    private navigationSubscription: Subscription;
    private socketSubscription: Subscription;
    private routeSubscription: Subscription;
    private stream: Observable<ProcessMessage>;

    permission: HunterPermission;
    tags: DisplayTag[];
    wrong: string[];
    right: string[];
    portalState: string = 'FECHADO';

    constructor(private msgSvc: MessageService, private socket: SocketService, private route: ActivatedRoute, private mainScrollerDirective: MainScrollerDirective,
        private navSvc: NavigationService, private router: Router, private changeDetector: ChangeDetectorRef) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(data => {
            if (this.socketSubscription !== undefined)
                this.socketSubscription.unsubscribe();
            if (this.navigationSubscription !== undefined)
                this.navigationSubscription.unsubscribe();
            this.navigationSubscription = this.navSvc.getItems().subscribe((pCat: HunterPermissionCategory[]) => {
                for (let category of pCat) {
                    for (let menu of category.permissions) {
                        if (menu.route === this.router.url) {
                            this.permission = new HunterPermission(menu);
                            this.changeDetector.detectChanges();
                            return;
                        }
                    }
                }
            });
            if (data.procId !== undefined) {
                this.initArrays();
                this.stream = this.socket.connect(environment.wsprocess + 'process/' + data.procId);
                this.socketSubscription = this.stream
                    .subscribe((msg: ProcessMessage) => {
                        let msgObj = JSON.parse(msg.data);

                        switch (msg.command.toUpperCase()) {
                            case "ALERT":
                                this.msgSvc.add({ severity: 'error', summary: msgObj.tag, detail: msgObj.message });
                                this.tags.push({ tagid: msgObj.tag, error: true, message: msgObj.message });
                                if (this.wrong.indexOf(msgObj.tag) < 0)
                                    this.wrong = [...this.wrong, msgObj.tag];
                                break;
                            case "NOTIFICATION":
                                this.msgSvc.add({ severity: 'success', summary: msgObj.tag, detail: 'Integração executada com sucesso' });
                                this.tags.push({ tagid: msgObj.tag, error: false, message: 'Integração executada com sucesso' });
                                if (this.right.indexOf(msgObj.tag) < 0)
                                    this.right = [...this.right, msgObj.tag];
                                break;
                            case "OPEN":
                                this.initArrays();
                                this.portalState = 'ABERTO';
                                this.msgSvc.add({ severity: 'info', summary: msgObj.tag, detail: 'Usuário ' + msgObj.user + ' Abriu o Portal' });
                                break;
                            case "CLOSE":
                                this.initArrays();
                                this.portalState = 'FECHADO';
                                this.msgSvc.add({ severity: 'info', summary: msgObj.tag, detail: 'Usuário ' + msgObj.user + ' Fechou o Portal' });
                                break;
                            default:
                                console.log('Unrecognized', msg);
                        }
                        this.mainScrollerDirective.scrollToBottom();
                    });
            }
        });
    }

    initArrays(): void {
        this.tags = Array.of(...[]);
        this.wrong = Array.of(...[]);
        this.right = Array.of(...[]);
    }

    ngOnDestroy(): void {
        this.unsubscribe();
    }

    @HostListener('window:beforeunload', ['$event'])
    beforeUnloadHander(event) {
        this.unsubscribe();
    }

    unsubscribe(): void {
        if (this.socketSubscription !== null)
            this.socketSubscription.unsubscribe();
        if (this.routeSubscription !== null)
            this.routeSubscription.unsubscribe();
        if (this.navigationSubscription !== null)
            this.navigationSubscription.unsubscribe();
    }
}