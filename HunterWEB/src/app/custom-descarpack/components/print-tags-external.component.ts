import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { environment } from "../../../environments/environment";

@Component({
    templateUrl: './print-tags-external.component.html'
})

export class PrintTagsExternalComponent implements OnInit {

    docid: string;
    doc: any[];
    device: string;
    prdItem: any;
    prdThings: any[] = [];
    all: any = {};
    qty: number = 0;
    metadata: any;
    dateMask: String = "99/99/9999";

    constructor(private route: ActivatedRoute, private http: HttpClient,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.all = {};
        this.route.params.subscribe(lstPrm => {
            console.log(lstPrm);
            this.device = lstPrm.device;
            this.getDoc(lstPrm.doc);
        });

    }

    getDoc(docid) {
        this.http.get(environment.customserver + 'task/tagsbydoc/' + docid).subscribe((data: any[]) => {
            this.docid = docid;
            this.doc = data;
            if (data.length > 0) {
                this.metadata = data[0].metadata;
                this.prdItem = null;
                this.prdThings = [];
            }
        });
    }

    onProdSelected(event) {
        console.log(this.prdItem);
        //this.prdItem.properties.slice();
        // if (this.prdItem.status == "NOVO" && this.prdThings.length==0) {
        //     //this.prdItem.properties = {};
        //     //console.log(this.prdThings);
        //     // for (var prop in this.prdItem.metadata) {
        //     //     this.prdItem.properties[prop] = '';
        //     // }
        // } else {
        //     this.prdItem = null;
        // }
    }

    enviaThings() {
        let envio: any = {};

        console.log(envio);

        for (var itms of this.prdThings) {
            itms['device'] = this.device;

            this.http.post(environment.customserver + 'task/printTags', itms).subscribe(data => {
                if (data['result']) {
                    this.msgSvc.add({ severity: "success", summary: "Print Tags", detail: "Tags successfully printed" });
                } else {
                    this.msgSvc.add({ severity: "error", summary: "Print Tags", detail: "Printing Problems. Please check." });
                }
                this.getDoc(this.docid);
            }, error => {
                this.msgSvc.add({ severity: "error", summary: "Print Tags", detail: "Printing Problems. Please check." });
                this.getDoc(this.docid);
            });

            this.msgSvc.add({ severity: "success", summary: "Print Tags", detail: "Tags sent to Printer" });

        }
    }

    onSetAll() {
        this.prdThings.push(this.clone(this.prdItem));
        this.prdItem = null;
        console.log(this.prdThings);
    }

    clone(obj) {
        if (obj == null || typeof (obj) != 'object')
            return obj;

        var temp = new obj.constructor();
        for (var key in obj)
            temp[key] = this.clone(obj[key]);

        return temp;
    }

    limpaThings() {
        this.prdThings = [];
    }

    undefChanged(checked){
        if(checked){
            this.dateMask="INDETERMINADA";
        } else {
            this.dateMask="99/99/9999";
        }
    }

}