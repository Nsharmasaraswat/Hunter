import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { environment } from "../../../environments/environment";
import { SocketService } from "../../shared/services/socket.service";

@Component({
    templateUrl: './status-origins.component.html'
})
export class StatusOriginsComponent implements OnInit {

    origins: any[];
    processes: any[];
    allocations: any[];
    table: any[] = [];

    constructor(private http: HttpClient, private socket: SocketService) {

    }
    ngOnInit(): void {
        this.http.get(environment.processserver + "origin").subscribe((data: any[]) => {
            console.log(data);
            this.origins = data;
            this.http.get(environment.processserver + "process/all").subscribe((procs: any[]) => {
                console.log(procs);
                this.processes = procs;
                this.http.get(environment.processserver + "process/allocation").subscribe((allocs: any[]) => {
                    console.log(allocs);
                    this.allocations = allocs;
                    for(let ori in this.origins) {
                        let obj = {};
                        obj['nome'] = this.origins[ori];
                        if(ori in this.allocations) {
                            for(let proc of this.processes) {
                                if(this.allocations[ori]===proc['id']) {
                                    obj['processo'] = proc['name'];
                                }
                            }
                        } else {
                            obj['processo'] = "NO RUNNING PROCESS";
                        }
                        this.table.push(obj);
                    }
                });
            });
        });
    }
}