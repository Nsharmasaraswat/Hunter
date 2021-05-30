import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { Observable } from 'rxjs/Rx';
import { environment } from "../../../environments/environment";
import { HunterDocument } from "../../shared/model/HunterDocument";
import CPFCNPJ from "../../shared/utils/cpfcnpjutils";

interface NotaFiscal {
    numero: string;
    serie: string;
    data: string;
    person: string;
    doc: string;
}

interface RecebimentoModel {
    nome: string;
    cnh: string;
    rg: string;
    rastreador: string;
    placa: string;
    marca: string;
    modelo: string;
    posesq: number;
    posdir: number;
    cnpj: string;
    nfs: NotaFiscal[];
    tNome: string;
    obs: string;
    custcode: string;
}

@Component({
    templateUrl: './lobby.component.html',
    styleUrls: ['yms-process.scss']
})

export class LobbyComponent implements OnInit {

    public model: RecebimentoModel = { nome: null, cnh: null, rg: null, rastreador: null, placa: null, marca: null, modelo: null, cnpj: null, tNome: "", posesq: 14, posdir: 14, obs: "", nfs: [], custcode: "" };

    @ViewChild('txtCustCode') txtCustCode: ElementRef;

    displayDialog: boolean;
    enableEditTransp: boolean;
    enableEditPerson: boolean;
    enableEditThing: boolean;
    btnNFEEnabled: boolean = false;
    btnNFSEnabled: boolean = false;
    emptyNFs: boolean = false;
    romaneioPendente: boolean = false;
    transportePendente: boolean = false;

    documents: any[] = [];
    columns: any[] = [
        { field: 'code', header: 'CÓDIGO NF' },
        { field: 'props.serie_nf', header: 'SÉRIE NF' },
        { field: 'props.data_nf', header: 'DATA NF' },
        { field: 'props.ticket | props.transporte_sap', header: 'TRANSPORTE SAP', }
    ];

    constructor(private msgSvc: MessageService, private http: HttpClient, private router: Router) {

    }

    ngOnInit(): void {

    }

    cancelSelect() {
        this.model.cnpj = undefined;
        this.model.custcode = undefined;
        this.checkCNPJ(undefined);
        this.checkCustCode(undefined);
    }

    fetchPerson(text: string, txtName) {
        if (text.length > 3) {
            this.http.get(environment.processserver + 'person/bytypecode/DRIVER/' + text)
                .catch((err: HttpErrorResponse) => {
                    this.enableEditThing = true;
                    txtName.focus();
                    return Observable.empty();
                }).subscribe((person: any) => {
                    if (person === null) {
                        this.enableEditPerson = true;
                        setTimeout(() => {
                            txtName.focus();
                        }, 300);
                    } else {
                        this.model.nome = person.name;
                        this.model.rg = person.fields.find(f => f.metaname === 'RG').value;
                    }
                });
        }
    }

    fetchThing(txtBrand) {
        this.http.get(environment.processserver + 'thing/bytagid/' + this.model.placa).catch((err: HttpErrorResponse) => {
            this.enableEditThing = true;
            return Observable.empty();
        }).subscribe((thing: any) => {
            if (thing === null) {
                this.enableEditThing = true;
                txtBrand.focus();
            } else {
                console.log(thing);
                this.model.marca = thing.properties.find(p => p.field.metaname === 'BRAND').value;
                this.model.modelo = thing.properties.find(p => p.field.metaname === 'MODEL').value;
                this.model.tNome = thing.properties.find(p => p.field.metaname === 'CARRIER').value;
            }
        });
    }

    selectNFs(ev: HunterDocument[]) {
        if (ev.length === 0) {
            this.model.nfs = Array.from([]);
        } else {
            ev.forEach(d => {
                let exist = this.model.nfs.find(nf => nf.doc === d.id);

                if (!exist) {
                    this.model.nfs.push({
                        numero: d.code,
                        serie: d.props.serie_nf,
                        data: d.props.data_nf,
                        person: d.person.id,
                        doc: d.id
                    });
                }
            });
        }
        this.model.custcode = undefined;
        this.model.cnpj = undefined;
    }

    enviar(): void {
        //#^&^% hack mf
        this.model.posesq = +this.model.posesq;
        this.model.posdir = +this.model.posdir;
        if (this.romaneioPendente) {
            if (this.model.obs.length > 0) this.model.obs += "\r\n";
            this.model.obs += "***ROMANEIO PENDENTE***"
        }

        if (this.transportePendente) {
            if (this.model.obs.length > 0) this.model.obs += "\r\n";
            this.model.obs += "***TRANSPORTE PENDENTE***"
        }
        this.http.post(environment.customserver + 'yms/lobby', this.model)
            .catch((err: HttpErrorResponse) => {
                return Observable.empty();
            }).subscribe((data: any) => {
                if (data.result) {
                    this.msgSvc.add({ severity: 'info', summary: "Entrada Registrada", detail: "Aguardar processo de chamada" });
                    this.router.navigate(['home', 'report', 'fixed', 'transportstatus']);
                } else {
                    this.msgSvc.add({ severity: 'error', summary: "Falha de Cadastramento", detail: data.message });
                }
            }, error => {
                this.msgSvc.add({ severity: 'error', summary: "Falha de Cadastramento", detail: error.message });
            });
    }

    checkCNPJ(complete: boolean) {
        this.btnNFEEnabled = complete !== undefined && CPFCNPJ.validateCNPJ(this.model.cnpj);
        if (complete && !this.btnNFEEnabled)
            this.msgSvc.add({ severity: 'error', summary: 'CNPJ INVÁLIDO', detail: 'Digite um CNPJ' });
    }

    checkCustCode(code) {
        if (code !== undefined && code.length > 0) {
            if (code.length < 10) {
                for (let i = code.length; i < 10; i++) {
                    this.txtCustCode.nativeElement.value = '0' + this.txtCustCode.nativeElement.value;
                }
            }
            this.btnNFSEnabled = true;
        } else
            this.btnNFSEnabled = false
    }

    trimCustCode(code) {
        if (code.length > 0) {
            while (code.indexOf('0') === 0)
                code = code.substring(1);
            this.btnNFSEnabled = false;
        }
    }

    custCodeChanged() {
        if (this.txtCustCode.nativeElement.value === '')
            this.model.custcode = '';
        else if (this.txtCustCode.nativeElement.value.length === 10)
            this.checkCustCode(this.txtCustCode.nativeElement.value);
    }
}

