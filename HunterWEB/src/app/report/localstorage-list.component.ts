import { HttpClient } from '@angular/common/http';
import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Calendar } from 'primeng/components/calendar/calendar';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../environments/environment';
import { TokenService } from '../security/services/token.service';
import { LocalStorageService } from '../shared/services/localstorage.service';
import { SocketService } from '../shared/services/socket.service';
import RestResponse from '../shared/utils/restResponse';
import { Report, ReportColumn } from './interfaces/report.interface';

@Component({
    templateUrl: 'localstorage-list.component.html',
    styleUrls: ['localstorage-list.component.scss']
})

export class LocalStorageReportList implements OnInit {

    /** Get handle on cmp tags in the template */
    @ViewChildren('txtVar') variablesValues: QueryList<any>;
    lineCount: number = 5;
    dataLoaded: boolean;
    reportList: Report[];
    selectedReport: Report;
    data: any[];
    columns: ReportColumn[];

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute,
        private localStorage: LocalStorageService) {
    }

    ngOnInit(): void {
        this.reportList = this.localStorage.listFromLocalStorage("REPORTS");
    }

    loadReport(ev) {
        this.dataLoaded = false;
        let params: string = this.selectedReport.file;
        if(this.variablesValues != null && this.variablesValues != undefined){
            this.variablesValues.forEach(f => {
                if (params === this.selectedReport.file) {
                    params += "?";
                } else {
                    params += "&";
                }
                if(f instanceof Calendar){
                    params += f.el.nativeElement.id + "=" + f.value.toISOString().replace(/T.+/, '');
                } else {
                    params += f.nativeElement.id + "=" + f.nativeElement.value;
                }
            });
        }
        if (params == '?') {
            params = '';
        }
        this.http.get(environment.reportserver + 'query/byFile/' + params).subscribe((resp: RestResponse) => {
            if (resp.status.result) {
                this.columns = this.selectedReport.columns;
                this.data = resp.data;
                this.dataLoaded = true;
            } else {
                console.log(resp.status.message);
            }
        });
    }
}