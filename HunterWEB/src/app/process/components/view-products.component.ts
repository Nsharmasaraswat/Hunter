import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../environments/environment';
import { TokenService } from '../../security/services/token.service';

@Component({
    templateUrl: './view-products.component.html'
})
export class ViewProductsComponent implements OnInit {

    data: any[] = [];
    first: number = 0;
    innerHeight: any;
    innerWidth: any;
    stream: any;

    constructor(private msgSvc: MessageService, private token: TokenService, private http: HttpClient, private router: Router, private route: ActivatedRoute) {
        this.innerHeight = (window.screen.height - window.screenTop) + "px";
        this.innerWidth = window.screen.availWidth;
    }

    ngOnInit(): void {
        this.route.params.subscribe(data => {
            console.log(data);
            this.http.get(environment.processserver + 'product/bytype/' + data.type).subscribe((lst: any[]) => {
                console.log(lst);
                this.data = lst;
            });
        });
    }

}
