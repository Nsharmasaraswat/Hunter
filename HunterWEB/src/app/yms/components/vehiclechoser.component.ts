import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from "@angular/core";
import { MessageService } from "primeng/components/common/messageservice";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { ReportColumn } from "../../report/interfaces/report.interface";
import { HunterUnitType } from "../../shared/model/enum/HunterUnitType";
import { HunterPropertyModel } from "../../shared/model/HunterPropertyModel";
import { HunterThing } from "../../shared/model/HunterThing";
import { HunterUnit } from "../../shared/model/HunterUnit";

class Vehicle {
    constructor(public thing: HunterThing, public tracker: string, public plates: string) {

    }
}

@Component({
    selector: 'vehiclechooser',
    templateUrl: './vehiclechoser.component.html',
    styleUrls: ['../process/yms-process.scss']
})
export class VehicleChoserComponent implements OnInit {
    selectedVehicle: Vehicle;
    @Output("chosen") onChosen: EventEmitter<HunterThing> = new EventEmitter();
    @Output("closed") onClosed: EventEmitter<void> = new EventEmitter();
    @Input("display") displayDialog: boolean;
    things: Vehicle[];
    rowCount: number = 10;

    columns: ReportColumn[] = [
        {
            field: 'plates',
            header: 'PLACA',
            type: '',
            nullString: '-',
            width: '20vw'
        },
        {
            field: 'thing.name',
            header: 'DESCRIÇÃO',
            type: '',
            nullString: '-',
            width: '50vw'
        },
        {
            field: 'tracker',
            header: 'RASTREADOR',
            type: '',
            nullString: '-',
            width: '20vw'
        }
    ];

    constructor(private http: HttpClient, private msgSvc: MessageService) {
    }

    ngOnInit() {
        if (window.innerHeight >= 800)
            this.rowCount = 18;
        else if (window.innerHeight >= 600)
            this.rowCount = 13;
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.displayDialog.currentValue)
            this.loadVehicles();
    }

    vehicleChosen(): void {
        this.onChosen.emit(this.selectedVehicle.thing);
        this.displayDialog = false;
        this.selectedVehicle = undefined;
    }

    close() {
        this.displayDialog = false;
        this.onClosed.emit();
    }

    loadVehicles() {
        Observable.of(environment.processserver + 'propertymodel/metaname/TRUCK')
            // first request
            .concatMap(url => {
                return this.http.get(url);
            })
            // .do((propModel: HunterPropertyModel) => {
            //     if (propModel === undefined || propModel.id === null)
            //         return Observable.throw(new Error("Invalid Property Model"));
            // })
            // second request
            .concatMap((propModel: HunterPropertyModel) => {
                return this.http.get(environment.processserver + 'thing/listByModelAndFieldValue/' + propModel.id + '/SERVICE_TYPE/ROTA');
            })
            .subscribe((trucks: HunterThing[]) => {
                this.things = trucks.map(tr => {
                    let tracker: HunterUnit = tr.unitModel.find(un => un.type === HunterUnitType.RTLS);
                    let plates: HunterUnit = tr.unitModel.find(un => un.type === HunterUnitType.LICENSEPLATES);
                    let vehicle = new Vehicle(tr, tracker === undefined ? '' : tracker.tagId, plates === undefined ? '' : plates.tagId);

                    return vehicle;
                });
            }, (errmsg: HttpErrorResponse) => {
                console.log(errmsg);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR VEÍCULOS', detail: errmsg.error });
            }, () => console.log('Complete'));
    }
}
