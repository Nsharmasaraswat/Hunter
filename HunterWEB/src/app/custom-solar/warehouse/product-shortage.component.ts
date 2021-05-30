import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenService } from '../../security/services/token.service';
import { HunterDocument, HunterDocumentItem } from '../../shared/model/HunterDocument';
import { HunterField } from '../../shared/model/HunterField';
import { HunterPerson } from '../../shared/model/HunterPerson';
import { NavigationService } from '../../shared/services/navigation.service';
import { SocketService } from '../../shared/services/socket.service';
import CPFCNPJ from '../../shared/utils/cpfcnpjutils';
import RestStatus from '../../shared/utils/restStatus';

interface WmsRule {
    id: number,
    name: string,
    conds: string[]
}

@Component({
    selector: 'product-shortage',
    templateUrl: 'product-shortage.component.html',
    styleUrls: ['product-shortage.component.scss'],
    providers: [DatePipe]
})
export class ProductShortageComponent {
    routeSubscription: Subscription;
    transport: HunterDocument;
    prdshrt: HunterDocument;
    customer: HunterPerson;
    ruleLoaded: boolean;
    selectedRule: WmsRule;
    ruleList: WmsRule[];
    ruleChanged: boolean;

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute,
        private navSvc: NavigationService, private datePipe: DatePipe) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.http.get(environment.processserver + 'document/parent/' + routeParams.docId, { responseType: 'json' })
                .subscribe((doc: HunterDocument) => {
                    this.transport = new HunterDocument(doc);
                    console.log(this.transport);
                    this.prdshrt = this.transport.siblings.find(d => d.metaname === 'PRDSHORTAGE' && d.status === 'NOVO');
                    if (this.prdshrt !== undefined) {
                        //listar regras
                        this.customer = this.prdshrt.person;
                        let rule = +this.prdshrt.props['template_id'];
                        if (rule > 0)
                            this.loadRule(rule);
                        this.customer.fields = this.customer.fields.sort((f1: HunterField, f2: HunterField) => f1.field.ordem - f2.field.ordem);
                        this.prdshrt.items = this.prdshrt.items.sort((di1: HunterDocumentItem, di2: HunterDocumentItem) => di1.product.sku.localeCompare(di2.product.sku));
                    }
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR DOCUMENTO', detail: error });
                });
        });
    }

    getCustomerName(): string {
        let ret: string = this.customer === undefined ? 'NÃO ENCONTRADO' : this.customer.name;
        let fieldCNPJ: HunterField = this.customer.fields.find(hf => hf.field.metaname === 'CNPJ');

        if (fieldCNPJ !== undefined && fieldCNPJ.value !== '')
            ret = CPFCNPJ.format(fieldCNPJ.value) + ' - ' + ret;
        else {
            let fieldCPF: HunterField = this.customer.fields.find(hf => hf.field.metaname === 'CPF');
            if (fieldCPF !== undefined && fieldCPF.value !== '')
                ret = CPFCNPJ.format(fieldCPF.value) + ' - ' + ret;
        }

        return ret;
    }

    loadRule(tplId: number) {
        this.http.get(environment.customserver + 'wms/listRules/2', { responseType: 'json' })
            .subscribe((ruleList: WmsRule[]) => {
                this.ruleList = ruleList;
                this.selectedRule = ruleList.find(r => r.id == tplId);
                this.ruleLoaded = true;
                if (this.prdshrt.items.length === 0 && this.selectedRule !== undefined)
                    this.changeRule();
            }, error => {
                console.log(error);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR DOCUMENTO', detail: error });
            });
    }

    ruleChange(ev) {
        let sel: WmsRule = ev.value;
        let orRuleId = +this.prdshrt.props['template_id'];

        this.ruleChanged = sel.id !== orRuleId;
    }

    changeRule() {
        this.http.put(environment.customserver + 'wms/changetransportrule/' + this.transport.id + '/' + this.selectedRule.id, this.prdshrt)
            .subscribe((res: RestStatus) => {
                if (res.result)
                    this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: "Regra Alterada" });
                this.router.navigate(['home']);
            }, (errmsg: HttpErrorResponse) => {
                console.log(errmsg);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO ALTERAR DOCUMENTO', detail: errmsg.error });
            });
    }

    fixRule() {
        this.http.put(environment.customserver + 'wms/buildnewrule/' + this.transport.id, null, { responseType: 'json' })
            .subscribe((res: RestStatus) => {
                if (res.result) {
                    this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: "Documento Reavaliado" });
                    this.ngOnInit();
                } else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO', detail: res.message });
            }, (errmsg: HttpErrorResponse) => {
                console.log(errmsg);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO REAVALIAR DOCUMENTO', detail: errmsg.error });
            });
    }

    resendDocument() {
        if (this.transport.status === "CAMINHAO NA PORTARIA" || this.transport.status === "CAMINHAO NA ENTRADA" || this.transport.status === "CAMINHAO NO PATIO") {
            this.http.put(environment.customserver + 'wms/reevaluatetransportrule/' + this.transport.id, '', { responseType: 'json' })
                .subscribe((res: RestStatus) => {
                    if (res.result) {
                        this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: "Documento Reavaliado" });
                        this.ngOnInit();
                    } else
                        this.msgSvc.add({ severity: 'error', summary: 'ERRO', detail: res.message });
                }, (errmsg: HttpErrorResponse) => {
                    console.log(errmsg);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO REAVALIAR DOCUMENTO', detail: errmsg.error });
                });
        } else {
            this.msgSvc.add({ severity: 'error', summary: 'ERRO AO REAVALIAR DOCUMENTO', detail: 'Status não permite reavaliação (' + this.transport.status + '). Usar Gerenciamento de Transporte' });
            this.router.navigate(['home', 'yms', 'manageTransport', this.transport.id]);
        }
    }
}
