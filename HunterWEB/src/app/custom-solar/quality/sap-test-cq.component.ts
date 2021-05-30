import { HttpClient } from "@angular/common/http";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Subscription } from 'rxjs';
import { environment } from "../../../environments/environment";
import { AuthService } from "../../security/services/auth.service";
import { HunterUser } from "../../shared/model/HunterUser";

interface Z_HW_QUALIDADE {
    MANDT: string,
    PLANT: string,
    CODE: string,
    IDENT: string,
    CONTROLE: string,
    REF_DOC_NO: string,
    MATERIAL: string,
    MOVE_TYPE: string,
    MOVE_COD: string,
    BATCH: string,
    STGE_LOC: string,
    MOVE_STLOC: string,
    ENTRY_QNT: string,
    UNID_MED: string,
    MOVE_PLANT: string,
    MOVE_BATCH: string,
    TIPO_NRHUNTER: string,
    DOCUMENTO: string
}

interface Z_HW_TRANSFERENCIA_MP {
    MANDT: string,
    CODE: string,
    IDENT: string,
    CENTRO: string,
    MATERIAL: string,
    DOCUMENTO: string,
    ANO: string,
    TIPOMOV: string,
    DEPORIGEM: string,
    DEPDESTINO: string,
    QUANTIDADE: string,
    UNID_MED: string,
    DEBCRED: string,
    TIPO_NRHUNTER: string
}

interface Z_HW_CONFERENCIA {
    NUMERO_NF: string,
    SERIE_NF: string,
    DATA_NF: string,
    EBELN: string,
    MATNR: string,
    QTDE_CONTADA: string,
    LGORT: string,
    CENTRO: string
}

interface Z_HW_PSC {
    I_PLANT: string,
    I_ORDER_NUMBER: string,
    I_QTD_PRODUCED: string
}

interface Z_HW_PROCESSO_NOTA_INDIVIDUAL {
    I_CHAVE: string
}

interface Z_HW_DELETE_START_CONF_CEGA {
    I_CHAVE: string
}

interface Z_HW_CHECKINCHECKOUT {
    MANDT: string;
    TKNUM: string;
    FUNCAO: string;
    ID_CONF: string;
    MATNR: string;
    LFIMG: string;
    FINAL: string;
    I_PALLET: string;
    I_LACRE: string;
    I_EUCATEX: string;
}

interface Z_HW_CHECKINCHECKOUT_PORTARIA {
    MANDT: string;
    TKNUM: string;
    SAIENT: string;
    CARRINHOS: string;
    CONES: string;
    KMSAIENT: string;
    OBSERVACAO: string;
    ITENSEGUR: string;
    EXTINTOR: string;
}

interface Z_HW_VEICULOS {
    I_TKNUM: string;
}

interface Z_HW_RECUSA_NF {
    I_DATA: string;
    I_WERKS: string;
}

interface Z_HW_CONFERENCIA_TRANSPORTE {
    NUMERO_NF: string,
    SERIE_NF: string,
    DATA_NF: string,
    EBELN: string,
    MATNR: string,
    QTDE_CONTADA: string,
    LGORT: string,
    CENTRO: string
}

interface Z_HW_PRONTA_ENTREGA {
    I_TKNUM: string
}

interface Z_HW_CHECKOUT_FATURADO {
    I_TKNUM: string
}

@Component({
    selector: 'sap-test-cq',
    templateUrl: 'sap-test-cq.component.html',
    styleUrls: ['quality-control.component.scss']
})

export class SapTest implements OnInit, OnDestroy {
    modelCQ: Z_HW_QUALIDADE = {
        MANDT: '120',
        PLANT: 'CNAT',
        CODE: '5',
        IDENT: '',
        CONTROLE: '',
        REF_DOC_NO: '',
        MATERIAL: '',
        MOVE_TYPE: '',
        MOVE_COD: '',
        BATCH: '',
        STGE_LOC: '',
        MOVE_STLOC: '',
        ENTRY_QNT: '',
        UNID_MED: '',
        MOVE_PLANT: '',
        MOVE_BATCH: '',
        TIPO_NRHUNTER: '',
        DOCUMENTO: ''
    }

    modelTR: Z_HW_TRANSFERENCIA_MP = {
        MANDT: '120',
        CODE: '3',
        IDENT: '0',
        CENTRO: 'CNAT',
        MATERIAL: '',
        DOCUMENTO: '',
        ANO: '',
        TIPOMOV: '',
        DEPORIGEM: '',
        DEPDESTINO: '',
        QUANTIDADE: '',
        UNID_MED: '',
        DEBCRED: '',
        TIPO_NRHUNTER: ''
    }

    modelCC: Z_HW_CONFERENCIA = {
        NUMERO_NF: '',
        SERIE_NF: '',
        DATA_NF: '',
        EBELN: '',
        MATNR: '',
        QTDE_CONTADA: '',
        LGORT: '',
        CENTRO: ''
    }

    modelPR: Z_HW_PSC = {
        I_PLANT: 'CNAT',
        I_ORDER_NUMBER: '',
        I_QTD_PRODUCED: ''
    }

    modelRN: Z_HW_PROCESSO_NOTA_INDIVIDUAL = {
        I_CHAVE: ''
    }

    modelDC: Z_HW_DELETE_START_CONF_CEGA = {
        I_CHAVE: ''
    }

    modelCIO: Z_HW_CHECKINCHECKOUT = {
        MANDT: '120',
        TKNUM: '',
        FUNCAO: '',
        ID_CONF: '',
        MATNR: '',
        LFIMG: '',
        FINAL: '',
        I_PALLET: '',
        I_LACRE: '',
        I_EUCATEX: ''
    }

    modelCIOP: Z_HW_CHECKINCHECKOUT_PORTARIA = {
        MANDT: '120',
        TKNUM: '',
        SAIENT: '',
        CARRINHOS: '',
        CONES: '',
        KMSAIENT: '',
        OBSERVACAO: '',
        ITENSEGUR: '',
        EXTINTOR: ''
    }

    modelVE: Z_HW_VEICULOS = {
        I_TKNUM: ''
    }

    modelCT: Z_HW_CONFERENCIA_TRANSPORTE = {
        NUMERO_NF: '',
        SERIE_NF: '',
        DATA_NF: '',
        EBELN: '',
        MATNR: '',
        QTDE_CONTADA: '',
        LGORT: '',
        CENTRO: ''
    }

    modelRNF: Z_HW_RECUSA_NF = {
        I_DATA: '',
        I_WERKS: 'CNAT'
    }

    modelPRE: Z_HW_PRONTA_ENTREGA = {
        I_TKNUM: ''
    }

    modelCHF: Z_HW_CHECKOUT_FATURADO = {
        I_TKNUM: ''
    }

    showQuality: boolean;
    showTransfer: boolean;
    showConference: boolean;
    showProduction: boolean;
    showProcNF: boolean;
    showDelConf: boolean;
    showCheckinout: boolean;
    showCheckinoutPort: boolean;
    showVehicle: boolean;
    showRecusaNF: boolean;
    showProntaEntrega: boolean;
    showCheckoutFaturado: boolean;
    integrationType: string = 'RN';
    result: string;
    sent: string;
    routeSubscription: Subscription;
    authSubscription: Subscription;

    constructor(private authSvc: AuthService, private route: ActivatedRoute,
        private http: HttpClient, private msgSvc: MessageService) {
    }

    ngOnInit(): void {
        this.routeSubscription = this.route.params.subscribe(data => {
            this.authSubscription = this.authSvc.getUser().subscribe((us: HunterUser) => {
                this.showQuality = us.properties['SAP_Quality'] !== undefined && us.properties['SAP_Quality'] === "true";
                this.showTransfer = us.properties['SAP_Transfer'] !== undefined && us.properties['SAP_Transfer'] === "true";
                this.showConference = us.properties['SAP_Conference'] !== undefined && us.properties['SAP_Conference'] === "true";
                this.showProduction = us.properties['SAP_Production'] !== undefined && us.properties['SAP_Production'] === "true";
                this.showProcNF = us.properties['SAP_ProcNF'] !== undefined && us.properties['SAP_ProcNF'] === "true";
                this.showDelConf = us.properties['SAP_DelConf'] !== undefined && us.properties['SAP_DelConf'] === "true";
                this.showCheckinout = us.properties['SAP_CheckInOut'] !== undefined && us.properties['SAP_CheckInOut'] === "true";
                this.showCheckinoutPort = us.properties['SAP_CheckInOutPort'] !== undefined && us.properties['SAP_CheckInOutPort'] === "true";
                this.showVehicle = us.properties['SAP_Vehicle'] !== undefined && us.properties['SAP_Vehicle'] === "true";
                this.showRecusaNF = us.properties['SAP_RecusaNF'] !== undefined && us.properties['SAP_RecusaNF'] === 'true';
                this.showProntaEntrega = us.properties['SAP_ProntaEntrega'] !== undefined && us.properties['SAP_ProntaEntrega'] === 'true';
                this.showCheckoutFaturado = us.properties['SAP_CheckoutFaturado'] !== undefined && us.properties['SAP_CheckoutFaturado'] === 'true';
            });
        });
    }

    ngOnDestroy(): void {
        console.log("onNgDestroy");
        this.unsubscribeObservables();
    }

    send() {
        let method: string;
        let model: any;
        let resultTable: string;
        let sentTable: string;
        switch (this.integrationType) {
            case "CC":
                method = "testConf";
                model = this.modelCC;
                resultTable = 'T_ZWH_CONFCEGALOG';
                sentTable = 'T_ZWH_CONFCEGA';
                break;
            case "CQ":
                method = "testCQ";
                model = this.modelCQ;
                resultTable = 'T_RETURN';
                sentTable = 'T_ZWH_CTLQA';
                break;
            case "TR":
                method = "transfMP";
                model = this.modelTR;
                resultTable = 'T_RETURN';
                sentTable = 'T_ZWH_DEVMP';
                break;
            case "PR":
                method = "testAPOPrd";
                model = this.modelPR;
                resultTable = 'T_RETURN';
                sentTable = 'E_RETURN';
                break;
            case "RN":
                method = "restartNF";
                model = this.modelRN;
                resultTable = 'T_RETURN';
                sentTable = 'I_CHAVE';
                break;
            case "DC":
                method = "deleteConf";
                model = this.modelDC;
                resultTable = 'T_RETURN';
                sentTable = 'I_CHAVE';
                break;
            case "CIO":
                method = "checkinout";
                model = this.modelCIO;
                resultTable = 'T_ZWH_CONFCEGALOG';
                sentTable = 'T_ZWH_CHECK_IN_OUT';
                break;
            case "CIOP":
                method = "checkinoutportaria";
                model = this.modelCIOP;
                resultTable = 'T_ZWH_CHECK_PORTA';
                sentTable = 'T_ZWH_CHECK_PORTA';
                break;
            case "VE":
                method = "veiculo";
                model = this.modelVE;
                resultTable = 'T_RETURN';
                sentTable = 'I_LISTA_VEICULOS';
                break;
            case "CT":
                method = "testConfTransp";
                model = this.modelCT;
                resultTable = 'T_ZWH_CONFCEGALOG';
                sentTable = 'T_ZWH_CONFCEGA';
                break;
            case "RNF":
                method = "recusaNF";
                model = this.modelRNF;
                resultTable = 'E_MENSAGEM';
                sentTable = 'T_ZWH_RECUSA_DOC';
                break;
            case "PRE":
                method = "prontaEntrega";
                model = this.modelPRE;
                resultTable = 'E_MENSAGEM';
                sentTable = 'T_ZWH_PENT';
                break;
            case "CHF":
                method = "checkoutFaturado";
                model = this.modelCHF;
                resultTable = 'E_MENSAGEM';
                sentTable = 'T_ZWH_CHECKOUT_TOT';
                break;
        }

        this.http.post(environment.customserver + 'testSAP/' + method, model).subscribe((msg: string) => {
            this.result = JSON.stringify(msg[resultTable], undefined, 6);
            this.sent = JSON.stringify(msg[sentTable], undefined, 6);
            const isErr = (element) => element['TYPE'] === 'E';

            if (msg[resultTable].some(isErr))
                this.msgSvc.add({ severity: 'error', summary: 'SAP', detail: msg[resultTable][0]['MESSAGE'] });
            else
                this.msgSvc.add({ severity: 'info', summary: 'Enviado', detail: 'SUCESSO' });
        }, (error: any) => {
            this.msgSvc.add({ severity: 'error', summary: 'SAP', detail: error.error.text });
        });
    }

    unsubscribeObservables() {
        if (this.routeSubscription && !this.routeSubscription.closed) {
            console.log("Unsubscribed Route");
            this.routeSubscription.unsubscribe();
        }
        if (this.authSubscription && !this.authSubscription.closed) {
            console.log("Unsubscribed Auth");
            this.authSubscription.unsubscribe();
        }
    }
}
