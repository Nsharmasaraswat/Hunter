import { HttpClient } from '@angular/common/http';
import { Component, Inject, LOCALE_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import 'rxjs/add/operator/catch';
import { NavigationService } from '../../shared/services/navigation.service';
import { FixedTableComponent } from "../fixed/fixed-table";
import { ReportColumn } from '../interfaces/report.interface';

@Component({
    templateUrl: './group-table.html'
})

export class GroupTableComponent extends FixedTableComponent {

    rowGroupMetadata: any;
    dataProcessed: boolean;
    columnsFiltered: ReportColumn[];

    constructor(protected msgSvc: MessageService, protected http: HttpClient, protected route: ActivatedRoute,
        protected navSvc: NavigationService, protected router: Router, @Inject(LOCALE_ID) protected locale: string) {
        super(msgSvc, http, route, navSvc, router, locale);
    }

    onSort() {
        this.updateRowGroupMetaData();
    }

    notifyDataLoaded(): void {
        this.updateRowGroupMetaData();
    };

    updateRowGroupMetaData() {
        this.rowGroupMetadata = {};
        if (this.data) {
            for (let i = 0; i < this.data.length; i++) {
                let rowData = this.data[i];
                let local = rowData.LOCAL;

                if (i == 0) {
                    this.rowGroupMetadata[local] = { index: 0, size: 1 };
                } else {
                    let previousRowData = this.data[i - 1];
                    let previousRowGroup = previousRowData.LOCAL;
                    if (local === previousRowGroup)
                        this.rowGroupMetadata[local].size++;
                    else
                        this.rowGroupMetadata[local] = { index: i, size: 1 };
                }
            }
            this.columnsFiltered = this.selectedReport.columns.filter(rc=>rc.field !== 'LOCAL');
            this.dataProcessed = true;
            console.log(this.rowGroupMetadata);
        }
    }
}