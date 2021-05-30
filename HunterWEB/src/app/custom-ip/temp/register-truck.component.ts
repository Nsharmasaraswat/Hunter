import { Component, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';
import { MessageService } from 'primeng/components/common/messageservice';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs/Subscription';

@Component({
    selector: 'register-truck',
    templateUrl: 'register-truck.component.html',
    styleUrls: ['register-truck.component.scss']
})

export class RegisterTruckComponent implements OnInit {
    data: any = {};
    products: any[] = [];
    selectedProduct: any = null;

    productModel: String;
    routeSubscription: Subscription;

    constructor(private msgSvc: MessageService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(data => {
            console.log(data);
            this.productModel = data.productModel;
            this.refresh();
        });
    }

    refresh() {
        this.selectedProduct = null;
        this.loadProducts();
    }

    loadProducts() {
        this.http.get(environment.processserver + 'product/byType/PMPRIMEMAT')
            .subscribe((msg: any) => {
                console.log(msg);
                this.products = msg;
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
            });
    }

    onSave() {
        console.log(this.data);

        if (this.selectedProduct == null) {
            this.msgSvc.add({ severity: 'error', summary: 'POR FAVOR SELECIONE O PRODUTO', detail: 'Necessário selecionar um produto' });
            return;
        }

        this.http.post(environment.processserver + 'thing/tempTruck', this.data)
            .subscribe((msg: any) => {
                if (msg != null) {
                    this.msgSvc.add({ severity: 'success', summary: 'DADOS SALVOS', detail: 'ados salvos no banco de dados' });
                    this.refresh();
                } else {
                    this.msgSvc.add({ severity: 'error', summary: 'DADOS NÃO SALVOS', detail: 'Checar o console para detalhes' });
                    console.log(msg);
                }
            }, error => {
                this.msgSvc.add({ severity: 'error', summary: 'DADOS NÃO SALVOS', detail: 'Checar o console para detalhes' });
            });
    }

    onBack() {
        this.backRoute();
    }

    onDiscard() {
        // refresh
        this.refresh();
    }

    onProductChange() {
        this.data.product = this.selectedProduct;
        console.log(this.data);
    }

    getHeader() {
        return 'Novo Caminhão';
    }

    backRoute() {
        // go to taskdef list page
        this.router.navigate(['home/']);
    }

}
