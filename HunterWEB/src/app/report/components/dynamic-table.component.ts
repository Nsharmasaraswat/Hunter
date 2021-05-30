import { DatePipe } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, HostListener, Input, ViewChild, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { SortEvent } from 'primeng/primeng';
import { Table } from 'primeng/table';
import { Action, ReportColumn } from '../interfaces/report.interface';

@Component({
    selector: 'dynamic-table',
    templateUrl: './dynamic-table.component.html',
    styleUrls: ['./dynamic-table.component.scss'],
    providers: [DatePipe],
    encapsulation: ViewEncapsulation.None //https://stackoverflow.com/a/50159982 - Access child component within scss
})
export class DynamicReportTableComponent implements AfterViewInit {

    @Input("action-buttons") action_buttons: Action[];
    @Input("action-title") action_title: string = 'ACTIONS';
    @Input("tableData") data: any[];
    @Input("tableColumns") columns: ReportColumn[];
    @Input("rowCount") rowCount: number;
    @Input("rowNumberVisible") rowNumberVisible: boolean;
    @Input("showFooter") showFooter: boolean;
    @Input("showFilters") showFilters: boolean = true;
    @Input("showCSV") showCSV: boolean = true;

    @ViewChild('tbl') table: Table;

    scHeight: number = 100;
    rowOptions: number[];
    hasActions: boolean;

    constructor(private router: Router, private cdRef: ChangeDetectorRef, private datePipe: DatePipe) { }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        setTimeout(() => {
            this.fixTableSize(event.target.innerHeight);
        }, 100);
    }

    fixTableSize(scrHeight: number): void {
        let pTable = this.table.el.nativeElement as HTMLElement;
        let body = pTable.getElementsByClassName('ui-table-scrollable-body').item(0) as HTMLElement;
        let tTop = this.getOffset(body).top;
        let tHeight = scrHeight - tTop - 50;//TODO: Calc Footer And Paginator

        if (this.rowCount === undefined)
            this.rowCount = tHeight / 30;//~35px each cell
        document.documentElement.style.setProperty('--tableHeight', `${tHeight}px`);
        this.cdRef.detectChanges();
    }

    ngAfterViewInit() {
        if (this.data.length > 100)
            this.rowOptions = [10, 25, 50, 75, 100];
        else if (this.data.length > 50)
            this.rowOptions = [10, 15, 20, 25, 30, 50];
        else if (this.data.length > 25)
            this.rowOptions = [5, 10, 15, 20, 25];
        else if (this.data.length > 15)
            this.rowOptions = [1, 5, 10, 15];
        else if (this.data.length > 10)
            this.rowOptions = [1, 3, 5, 10];
        else if (this.data.length > 5)
            this.rowOptions = [1, 3, 5];
        else
            this.rowOptions = [1];
        if (this.data.length > 1 && this.data.length < 300)
            this.rowOptions.push(this.data.length);
        this.rowCount = Math.min(Math.max.apply(null, this.rowOptions), this.rowCount);
        if (!this.rowOptions.includes(this.rowCount))
            this.rowCount = this.rowOptions[Math.floor(this.rowOptions.length / 2)];;
        this.hasActions = this.action_buttons !== null && this.action_buttons !== undefined && this.action_buttons.length > 0;
        this.fixTableSize(window.innerHeight);
    }

    getOffset(el) {
        var _x = 0;
        var _y = 0;
        while (el && !isNaN(el.offsetLeft) && !isNaN(el.offsetTop)) {
            _x += el.offsetLeft - el.scrollLeft;
            _y += el.offsetTop - el.scrollTop;
            el = el.offsetParent;
        }
        return { top: _y, left: _x };
    }

    //TODO: Check other dates also
    customSort(event: SortEvent) {
        event.data.sort((data1, data2) => {
            let value1 = data1[event.field];
            let value2 = data2[event.field];
            let result = null;

            if (!isNaN(value1) && !isNaN(value2)) {
                result = (value1 - value2);
            } else {
                if (value1.match(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/))
                    value1 = new Date(value1.replace(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/, "$2/$1/$3 $4:$5:$6"));
                else if (value1.match(/(\d{2})\/(\d{2})\/(\d{4})/))
                    value1 = new Date(value1.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$2/$1/$3"));
                else if (value1.match(/(\d{4})-(\d{2})-(\d{2})/))
                    value1 = new Date(value1.replace(/(\d{4})-(\d{2})-(\d{2})/, "$2/$3/$1"));

                if (value2.match(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/))
                    value2 = new Date(value2.replace(/(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{1,2}):(\d{1,2})/, "$2/$1/$3 $4:$5:$6"));
                else if (value2.match(/(\d{2})\/(\d{2})\/(\d{4})/))
                    value2 = new Date(value2.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$2/$1/$3"));
                else if (value2.match(/(\d{4})-(\d{2})-(\d{2})/))
                    value2 = new Date(value1.replace(/(\d{4})-(\d{2})-(\d{2})/, "$2/$3/$1"));


                if (value1 != null && value2 != null) {
                    if (typeof value1 === 'string' && typeof value2 === 'string')
                        result = value1.localeCompare(value2);
                    else if (typeof value1 === 'string' && value2 instanceof Date)
                        result = value1.localeCompare(this.datePipe.transform(value2, 'yyyy-MM-dd 00:00:00'));
                    else if (value1 instanceof Date && typeof value2 === 'string')
                        result = this.datePipe.transform(value1, 'yyyy-MM-dd 00:00:00').localeCompare(value2);
                    else {
                        result = value1 < value2 ? -1 : (value1 > value2 ? 1 : 0);
                    }
                } else if (value1 == null && value2 == null)
                    result = 0;
                else if (value1 != null)
                    result = -1;
                else if (value2 != null)
                    result = 1;
            }

            return (event.order * result);
        });
    }

    runAction(route: string) {
        let route_path: string[] = route.split('/');

        this.router.navigate(route_path);
    }
}
