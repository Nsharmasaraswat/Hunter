import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from "rxjs";
import { environment } from '../../../../environments/environment';
import { HunterLocation } from '../../../shared/model/HunterLocation';
import { SocketService } from "../../../shared/services/socket.service";

declare var require: any;

@Component({
    selector: 'location-choser',
    templateUrl: 'location-choser.component.html'
})

export class LocationChoserComponent implements OnInit, OnDestroy {
    routeSubscription: Subscription;
    locationLoaded: Boolean;
    location: HunterLocation;


    constructor(private msgSvc: MessageService, private http: HttpClient, private socket: SocketService, private route: ActivatedRoute) {

    }

    ngOnDestroy(): void {
        console.log('CustomIpLocationDestroy');
        if (this.routeSubscription != null)
            this.routeSubscription.unsubscribe();
    }


    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(params => {
            console.log('Custom-IP Route Subscription');
            this.http.get(environment.processserver + 'location/byMetaname/' + params.location).subscribe((resp: HunterLocation) => {
                var parse = require('wellknown');
                this.location = resp;
                this.location.center = parse(resp.wkt).coordinates;
                this.locationLoaded = true;
            });
        });
    }
}
