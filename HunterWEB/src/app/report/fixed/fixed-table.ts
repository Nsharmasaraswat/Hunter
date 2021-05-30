import { DatePipe } from "@angular/common";
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, Inject, LOCALE_ID, OnDestroy, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Calendar } from 'primeng/components/calendar/calendar';
import { MessageService } from 'primeng/components/common/messageservice';
import { Dropdown } from 'primeng/components/dropdown/dropdown';
import { Accordion } from "primeng/primeng";
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Rx';
import { environment } from '../../../environments/environment';
import { HunterPermissionCategory } from "../../shared/model/HunterPermission";
import { NavigationService } from '../../shared/services/navigation.service';
import RestResponse from '../../shared/utils/restResponse';
import { DynamicReportTableComponent } from "../components/dynamic-table.component";
import { Report, ReportColumn, ReportVariable } from '../interfaces/report.interface';

@Component({
    templateUrl: './fixed-table.html'
})

export class FixedTableComponent implements OnInit, OnDestroy {

    @ViewChild('tbl') dynTable: DynamicReportTableComponent;
    @ViewChild('accFilter') accordionFilters: Accordion;
    /** Get handle on cmp tags in the template */
    @ViewChildren('txtVar') variablesValues: QueryList<any>;
    protected dPipe: DatePipe;
    protected rowCount: number = 10;
    protected autoLoad: boolean;
    protected data: any[];
    protected columns: ReportColumn[];
    private updateTimer: any;
    expanded: boolean;
    dataLoaded: boolean;
    selectedReport: Report;

    private routeSubscription: Subscription;
    private navigationSubscription: Subscription;

    constructor(protected msgSvc: MessageService, protected http: HttpClient, protected route: ActivatedRoute,
        protected navSvc: NavigationService, protected router: Router, @Inject(LOCALE_ID) protected locale: string) {
        this.dPipe = new DatePipe(locale);
    }

    ngOnInit() {
        this.routeSubscription = this.route.params.subscribe((params: Params) => {
            this.dataLoaded = false;
            if (window.innerHeight > 720)
                this.rowCount = 20;
            else if (window.innerHeight > 640)
                this.rowCount = 10;
            if (params.fileName != undefined) {
                this.http.get(environment.reportserver + 'query/loadFile/' + params.fileName + '.json')
                    .catch((err: HttpErrorResponse) => {
                        return Observable.of<RestResponse>({ status: { result: false, message: err.error }, data: [] });
                    }).subscribe((resp: RestResponse) => {
                        if (resp.status.result) {
                            let variables: ReportVariable[] = resp.data['variables'];

                            this.selectedReport = {
                                file: params.fileName,
                                name: resp.data['name'],
                                query: resp.data['query'],
                                variables: variables.sort((a1: ReportVariable, a2: ReportVariable) => a1.type.localeCompare(a2.type)),
                                columns: resp.data['columns'],
                                actions: resp.data['actions']
                            };
                            this.expanded = variables !== undefined && variables.length > 0;
                            setTimeout(() => {
                                this.variablesValues.forEach(f => {
                                    if (f instanceof Dropdown) {
                                        let dd: Dropdown = f;
                                        dd.updateDimensions();
                                    }
                                });
                            }, 200);
                            this.checkMenuOptions();
                        } else {
                            console.log(resp.status.message);
                            this.msgSvc.add({ severity: 'error', summary: "Falha arquivo Json", detail: "Problema na consulta" })
                        }
                    });

            } else {
                console.log('CHECK PERMISSION (missing filename at route?)');
                this.msgSvc.add({ severity: 'error', summary: "Falha de Cadastramento", detail: "Sem arquivo na rota" })
            }
        });
    }

    checkMenuOptions() {
        this.navigationSubscription = this.navSvc.getItems().subscribe((perms: HunterPermissionCategory[]) => {
            perms.forEach(category => {
                category.permissions.forEach(menu => {
                    if (menu.route === this.router.url) {
                        if ('autoload' in menu.properties) {
                            this.loadProperties(menu.properties, this.selectedReport);
                        } else {
                            if (this.selectedReport.variables.length == 0) {
                                this.loadReport();
                            }
                        }
                    }
                    this.navigationSubscription.unsubscribe();
                });
            });
        });
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
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
        if (this.navigationSubscription != null)
            this.navigationSubscription.unsubscribe();
    }

    protected loadProperties(properties: any, lodadedReport: Report): void {
        this.autoLoad = properties.autoload || false;
        this.selectedReport = lodadedReport;
        if ('rowcount' in properties)
            this.rowCount = +properties.rowcount;
        this.loadReport();
    }

    protected loadReport(ev?) {
        this.dataLoaded = false;
        let params: string = this.selectedReport.file;
        let addParams = this.autoLoad;
        let dateNow = this.dPipe.transform(new Date(), 'yyyy-MM-dd');

        if (this.variablesValues != null && this.variablesValues != undefined) {
            this.variablesValues.forEach(f => {
                if (params === this.selectedReport.file) {
                    params += "?";
                } else {
                    params += "&";
                }

                if (f instanceof Calendar) {
                    params += f.el.nativeElement.id + "=" + this.dPipe.transform(f.value == null ? dateNow : f.value, 'yyyy-MM-dd');
                } else if (f instanceof Dropdown) {
                    params += f.el.nativeElement.id + "=" + f.selectedOption.value;
                } else {
                    params += f.nativeElement.id + "=" + f.nativeElement.value;
                }
                addParams = false;
            });
        } else if (addParams) //TODO: Acochambramento Solar
            params += (params.indexOf('?') > -1 ? "&" : "?") + "beginDate=" + dateNow + "&endDate=" + dateNow;
        if (params == '?') {
            params = '';
        }
        this.http.get(environment.reportserver + 'query/byFile/' + params)
            .catch((err: HttpErrorResponse) => {
                return Observable.of<RestResponse>({ status: { result: false, message: err.error }, data: [] });
            }).subscribe((resp: RestResponse) => {
                if (resp.status.result) {
                    this.selectedReport.columns = resp['columns'].map(col => {
                        if (col.width === undefined || col.width === '')
                            col.width = (col.header.length + 7) + 'ch';//Accounts for the sortable icon and space between
                        if (col.min_width === undefined || col.min_width === '')
                            col.min_width = (col.header.length + 7) + 'ch';
                        return col;
                    });
                    this.selectedReport.query = resp['query'];
                    this.selectedReport.name = resp['name'];
                    this.data = resp.data;
                    this.dataLoaded = true;
                    this.expanded = false;
                    this.updateTable()
                    this.notifyDataLoaded();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO NA CONSULTA', detail: resp.status.message });
                }
            });
    }

    protected notifyDataLoaded(): void { };
    
    updateTable(): void {
        clearTimeout(this.updateTimer);
        this.updateTimer = setTimeout(() => {
            if (this.dynTable !== undefined) {
                this.dynTable.fixTableSize(window.innerHeight);
            }
        }, 500);
    }
}