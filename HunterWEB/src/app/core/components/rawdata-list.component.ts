import { HttpClient } from "@angular/common/http";
import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { environment } from "../../../environments/environment";

@Component({
    selector: 'rawdata-list',
    templateUrl: 'rawdata-list.component.html',
    styleUrls: ['rawdata-list.component.scss']
})

export class RawdataListComponent {
    public complexDataList: any[] = [];
    public selectedRow: any;

    constructor(private route: ActivatedRoute, private http: HttpClient,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.complexDataList = [];
        // this.route.params.subscribe(lstPrm => {
        //     this.getDocs();
        // });
    }

    getComplexDataList(begin: Number, end: Number): void {
        console.log("Begin: " + begin + " End: " + end);
        this.http.get(environment.coreserver + 'rawdata/listInterval?begin=' + begin + '&end=' + end).subscribe((data: any[]) => {
            console.log(data);
            this.complexDataList = data;
        });
    }

    tsToDateString(ts: number): String {
        return new Date(ts).toUTCString();
    }

    onRowSelect(event) {
      //  this.msgSvc = [{severity:'info', summary:'Car Selected', detail:'Vin: ' + event.data.vin}];
    }
}
