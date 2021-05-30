import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';
import { TokenService } from '../../security/services/token.service';
import { LocalStorageService } from '../../shared/services/localstorage.service';
import { SocketService } from '../../shared/services/socket.service';
import RestResponse from '../../shared/utils/restResponse';
import { Report } from '../interfaces/report.interface';

@Component({
    selector: 'report-builder',
    templateUrl: 'report-builder.component.html',
    styleUrls: ['report-builder.component.scss']
})

export class ReportBuilderComponent implements OnInit {

    data: Report[] = [];
    selectedReport: Report;

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute,
        private localStorage: LocalStorageService) {

    }

    ngOnInit(): void {
        this.route.params.subscribe(d => {
            this.http.get(environment.reportserver + 'query/listFiles').subscribe((resp: RestResponse) => {
                if (resp.status.result) {
                    this.data = resp.data;
                } else {
                    console.log(resp.status.message);
                }
            });
        });
    }

    handleSave(ev) {
        this.localStorage.storeOnLocalStorage("REPORTS", this.selectedReport);
        this.msgSvc.add({ severity: 'success', summary: 'Report Saved', detail: 'Report ' + this.selectedReport.name + ' saved on local storage' });
    }

    handleRemove(ev) {
        this.localStorage.removeFromLocalStorage("REPORTS", "name", this.selectedReport.name);
        this.msgSvc.add({ severity: 'success', summary: 'Report Removed', detail: 'Report ' + this.selectedReport.name + ' removed from local storage' });
    }

    handleClear(ev) {
        this.localStorage.removeAllFromLocalStorage("REPORTS");
        this.msgSvc.add({ severity: 'success', summary: 'Local Storage Cleared', detail: 'Reports removed from local storage' });
    }
}
