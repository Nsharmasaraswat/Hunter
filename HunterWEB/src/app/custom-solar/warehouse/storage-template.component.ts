import { HttpClient } from "@angular/common/http";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import 'rxjs/add/operator/catch';
import { environment } from '../../../environments/environment';
import { HunterAddress } from "../../shared/model/HunterAddress";

@Component({
    selector: 'storage-template',
    templateUrl: './storage-template.component.html'
})
export class StorageTemplateComponent implements OnInit, OnDestroy {
    routeSubscription: Subscription;
    restSubscription: Subscription;

    addressList: HunterAddress[];
    selAddressList: HunterAddress[];
    tplDesc: string;
    queries: string[];
    constructor(private route: ActivatedRoute, private http: HttpClient, private msgSvc: MessageService) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.addressList = Array.of(...[]);
            this.selAddressList = Array.of(...[]);
            this.loadAddresses('ROAD', routeParams.locId);
            this.loadAddresses('RACK', routeParams.locId);
            this.loadAddresses('DOCK', routeParams.locId);
            this.loadAddresses('BLOCK', routeParams.locId);
            this.loadAddresses('DRIVE-IN', routeParams.locId);
        });
    }

    ngOnDestroy(): void {
        this.unsubscribeAll();
    }

    unsubscribeAll() {
        if (this.routeSubscription !== null && this.routeSubscription !== undefined)
            this.routeSubscription.unsubscribe();
        if (this.restSubscription !== null && this.restSubscription !== undefined)
            this.restSubscription.unsubscribe();
    }

    loadAddresses(type: string, location: string) {
        this.http.get(environment.processserver + 'address/quickbytypeandlocation/' + type + '/' + location, { responseType: 'json' })
            .subscribe((addresses: HunterAddress[]) => {
                this.addressList = this.addressList.concat(addresses);
                if (type === 'ROAD')
                    this.orderSource();
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ENDEREÇOS', detail: error.statusText });
            },
                () => console.log("Lodaded Addresses"));
    }

    orderSource() {
        this.addressList.sort((a: HunterAddress, b: HunterAddress) => {
            if (a.metaname === null && b.metaname === null) return 0;
            if (b.metaname === null) return 1;
            if (a.metaname === null) return -1;
            if (a.metaname === b.metaname) return 0;
            return a.metaname > b.metaname ? 1 : -1;
        });
    }

    createTemplate() {
        let items: any[] = Array.of(...[]);

        this.queries = Array.of(...[]);
        this.queries.push("INSERT INTO `wms1_dev`.`tpl` (`tpl_descricao`, `tpl_itmserial`, `tpl_flagprg`) VALUES ('" + this.tplDesc + "', '1', '1');");
        this.queries.push("SET @lastId := LAST_INSERT_ID();");
        for (let i = 1; i <= this.selAddressList.length; i++) {
            let addr = this.selAddressList[i - 1].name
            items.push({
                'name': addr,
                'priority': i * 10
            });
            this.queries.push("INSERT INTO `wms1_dev`.`tplitm` (`tpl_id`, `tplitm_id`, `tplitm_descricao`, `tplitm_tblid`, `tplitm_tblattid`, `tplitm_operador`, `tplitm_tblcndid`, `tplitm_tblattcndid`, `tplitm_constante`, `tplitm_prior`, `tplitm_insdt`, `tplitm_udpdt`)VALUES(@lastId," + i + ",'" + addr + "',30,3,'=',1,1,'" + addr + "'," + (i * 10) + ",now(),now());");
        }
        let stub = {
            'name': this.tplDesc,
            'items': items
        }
        console.log(stub);
        this.queries.push("UPDATE wms1_dev.tpl INNER JOIN (SELECT tpl_id, COUNT(tplitm_id) AS serial FROM wms1.tplitm GROUP BY tpl_id) ser ON tpl.tpl_id = ser.tpl_id SET tpl.tpl_itmserial = ser.serial;");
    }
}