import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SelectItem } from 'primeng/api';
import { MessageService } from 'primeng/components/common/messageservice';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenService } from '../../security/services/token.service';
import { HunterDocument } from '../../shared/model/HunterDocument';
import { NavigationService } from '../../shared/services/navigation.service';
import { SocketService } from '../../shared/services/socket.service';
import RestStatus from '../../shared/utils/restStatus';
import { ConferenceProduct } from '../../shared/classes/ConferenceProduct';

// const APOQC_MODEL_ID: string = 'e661ca59-8d59-11e9-815b-005056a19775';
// const INTERNAL_LOT_PMF_ID: string = '526367e8-79ac-11e9-a9ec-005056a19775';
// const LABEL_OBS_PMF_ID: string = '52661afe-79ac-11e9-a9ec-005056a19775';
// const QCR_PMF_ID: string = '235926e8-8945-11e9-815b-005056a19775';

@Component({
    selector: 'quality-control',
    templateUrl: 'quality-control.component.html',
    styleUrls: ['quality-control.component.scss'],
    providers: [DatePipe]
})

export class QualityControlComponent {
    routeSubscription: Subscription;
    transport: HunterDocument;
    ordconf: HunterDocument;
    qualityDoc: HunterDocument;
    prdConf: ConferenceProduct[];
    allSelected: boolean = false;
    qualityStatus: SelectItem[] = [
        { label: 'Selecione', value: '' },
        { label: 'BLOQUEADO', value: 'BLOQUEADO' },
        { label: 'REPROVADO', value: 'REPROVADO' },
        { label: 'APROVADO', value: 'APROVADO' }
    ]


    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute,
        private navSvc: NavigationService, private datePipe: DatePipe) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(routeParams => {
            this.http.get(environment.customserver + 'document/grandparent/' + routeParams.docId, { responseType: 'json' })
                .subscribe((doc: HunterDocument) => {
                    this.transport = new HunterDocument(doc);
                    this.ordconf = new HunterDocument(doc.siblings.find(d => d.metaname === 'ORDCONF' && (d.status === 'CONFERIDO' || d.status === 'SUCESSO')));
                    this.qualityDoc = new HunterDocument(this.ordconf.siblings.find(s => s.metaname === 'RETORDCONF' && s.status === 'SUCESSO'));
                    if (this.qualityDoc !== undefined) {
                        this.prdConf = ConferenceProduct.getPrdQCArray(this.qualityDoc);
                        this.itemChanged();
                    } else
                        this.msgSvc.add({ severity: 'error', summary: 'ERRO NO DOCUMENTO', detail: 'SEM CONFERENCIA' });
                }, error => {
                    console.log(error);
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO CARREGAR DOCUMENTO', detail: error });
                });
        });
    }

    sendQuality() {
        this.http.post(environment.customserver + 'document/createquality/' + this.transport.id, this.prdConf)
            .subscribe((res: RestStatus) => {
                if (res.result) {
                    this.msgSvc.add({ severity: 'success', summary: 'SUCESSO', detail: "Controle de Qualidade " + this.transport.code + " Salvo" });
                    this.router.navigate(['home']);
                } else
                    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO SALVAR DOCUMENTO', detail: res.message });
            }, (errmsg: HttpErrorResponse) => {
                console.log(errmsg);
                this.msgSvc.add({ severity: 'error', summary: 'ERRO AO SALVAR DOCUMENTO', detail: errmsg.message });
            });
    }

    itemChanged() {
        console.log("Array Length", this.prdConf.filter(p => p.rodape === '').length);
        this.allSelected = this.prdConf.filter(p => p.rodape === '').length == 0;
    }
}
