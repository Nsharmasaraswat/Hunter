import { HttpClient } from "@angular/common/http";
import { AfterViewInit, Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterAddress } from "../../shared/model/HunterAddress";
import { SocketService } from '../../shared/services/socket.service';
import RestStatus from '../../shared/utils/restStatus';

const ELEM_HEIGHT: number = 37;

class AddressStub {
    constructor(public id: string, public name: string, public status: string, public capacity: number, public address: HunterAddress) {
    }
}

@Component({
    selector: 'address-block',
    templateUrl: 'address-block.component.html'
})

export class AddressBlockComponent implements OnInit, OnDestroy, AfterViewInit {
    private routeSubscription: Subscription;

    selAddress: AddressStub[] = [];
    addressList: AddressStub[] = [];
    colsAddress: ReportColumn[] = [
        {
            field: 'name',
            header: 'NOME',
            type: 'TEXT',
            nullString: '',
            width: '70%'
        },
        {
            field: 'capacity',
            header: 'CAPACIDADE',
            type: 'NUMBER',
            nullString: '',
            width: '10%'
        },
        {
            field: 'status',
            header: 'STATUS',
            type: 'TEXT',
            nullString: '',
            width: '20%'
        }
    ];

    rows: number = 15;
    rowOptions: number[] = [5, 10, 15, 25, 50];
    disableSave: boolean = false;

    statusOptions: any[] = [
        { label: 'ATIVO', value: 'ATIVO' },
        { label: 'BLOQUEADO', value: 'BLOQUEADO' }
    ]
    selStatus: any = 'ATIVO';

    constructor(private route: ActivatedRoute, private http: HttpClient, private socket: SocketService,
        private msgSvc: MessageService, private router: Router) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            console.time('BuildList');
            this.http.get(environment.processserver + 'address/bytype/ROAD')
                .subscribe((addresses: HunterAddress[]) => {
                    this.addressList = addresses
                        .filter(addr => addr.model.fields.find(amf => amf.metaname === 'CAPACITY') !== undefined)
                        .map(addr => {
                            let capField = addr.model.fields.find(amf => amf.metaname === 'CAPACITY');
                            let cap = addr.fields.find(af => af.modelfield_id === capField.id);

                            if (cap !== undefined)
                                return new AddressStub(addr.id, addr.name, addr.status, +cap.value, addr);
                            else
                                console.log('Fix ' + addr.name + ' Fields')
                        });
                    console.timeEnd('BuildList');
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR ENDEREÇOS', detail: error });
                },
                    () => console.log("Lodaded Addresses"));
        });
    }

    ngAfterViewInit() {
        this.rows = Math.floor(window.innerHeight / ELEM_HEIGHT);
        this.rowOptions = [Math.floor(this.rows / 3), Math.floor(this.rows / 2), this.rows, this.rows * 2, this.rows * 3];
    }

    ngOnDestroy(): void {
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
    }

    changeComplete(): void {
        for (let selAddr of this.selAddress) {
            this.http.put(environment.customserver + 'address/changeStatus/' + selAddr.id + '/' + this.selStatus, '').catch(error => {
                console.error("error catched", error);
                return Observable.of({ result: false, message: "Error Value Emitted" });
            }).subscribe((resp: RestStatus) => {
                if (resp.result) {
                    selAddr.status = this.selStatus;
                    this.checkState();
                } else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ALTERAR ENDEREÇOS', detail: resp.message });
            }, (error: RestStatus) => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ALTERAR ENDEREÇOS', detail: error.message });
            });
        }
    }

    checkState() {
        let hasSame = false;

        for (let selAddr of this.selAddress) {
            if (selAddr.status === this.selStatus) {
                hasSame = true;
                break;
            }
        }
        this.disableSave = hasSame;
    }
}
