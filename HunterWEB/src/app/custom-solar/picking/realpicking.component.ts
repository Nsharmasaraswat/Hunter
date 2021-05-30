import { DatePipe } from "@angular/common";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Component, HostListener, Inject, LOCALE_ID, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { environment } from "../../../environments/environment";
import { HunterPermission, HunterPermissionCategory } from "../../shared/model/HunterPermission";
import { NavigationService } from "../../shared/services/navigation.service";

@Component({
    selector: 'realpicking',
    templateUrl: 'realpicking.component.html'
})
export class RealPickingComponent implements OnInit, OnDestroy {

    private routeSubscription: Subscription;
    private navigationSubscription: Subscription;
    private dPipe: DatePipe;
    permission: HunterPermission;

    constructor(private router: Router, private route: ActivatedRoute, private navSvc: NavigationService, private http: HttpClient, private msgSvc: MessageService,
        @Inject(LOCALE_ID) protected locale: string) {
        this.dPipe = new DatePipe(locale);
    }

    ngOnInit(): void {
        console.log("NgOnInit");
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            console.log("RouteSubscription");
            this.navigationSubscription = this.navSvc.getItems().subscribe((pCat: HunterPermissionCategory[]) => {
                for (let category of pCat) {
                    for (let menu of category.permissions) {
                        if (menu.route === this.router.url) {
                            this.permission = new HunterPermission(menu);
                            return;
                        }
                    }
                }
            });
        });
    }

    ngOnDestroy(): void {
        console.log("NgOnDestroy");
        this.unsubscribeObservables();
    }

    @HostListener('window:beforeunload', ['$event'])
    beforeUnloadHander(event) {
        console.log("beforeUnload");
        this.unsubscribeObservables();
    }

    unsubscribeObservables(): void {
        if (this.routeSubscription !== null)
            this.routeSubscription.unsubscribe();
        if (this.navigationSubscription !== null)
            this.navigationSubscription.unsubscribe();
    }

    loadDeliveries(dt: Date): void {
        let dateStr: string = this.dPipe.transform(dt, 'yyyyMMdd');
        let headers = new HttpHeaders({
            'Content-Type': 'application/pdf',
            responseType: 'blob',
            Accept: 'application/pdf',
            observe: 'response'
        })

        this.http.post(environment.customserver + "realPicking/loadtrips/" + dateStr, {}, { headers: headers, responseType: 'blob' })
            .subscribe(
                (res: any) => {
                    const file = res;
                    const url = window.URL.createObjectURL(new Blob([res as Blob], { type: 'application/pdf' }));

                    var link = document.createElement('a');
                    document.body.appendChild(link);
                    link.setAttribute('style', 'display: none');
                    link.href = url;
                    link.download = 'Montagens_' + this.dPipe.transform(dt, 'dd_MM_yyyy') + '.pdf';
                    link.click();
                    this.msgSvc.add({ severity: 'success', summary: 'Dados Carregados', detail: 'Entregas importadas para o hunter' });
                },
                (error: Error) => {
                    this.msgSvc.add({ severity: 'error', summary: 'Não foi possível carregar documento', detail: error.message });
                },
                () => { }
            );
    }
}