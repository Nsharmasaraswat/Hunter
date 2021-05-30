import { Component, Injectable, OnInit } from "@angular/core";
import { MessageService } from "primeng/components/common/messageservice";
import { HunterWidget, HunterWidgetModel } from "../../../shared/model/HunterWidget";
import { NavigationService } from "../../../shared/services/navigation.service";
import { WidgetService } from "../../services/widget.service";
import { options } from "../util/optionsData";


@Component({
  selector: "edit-chart",
  providers: [NavigationService],
  templateUrl: "./edit-chart.component.html",
  styleUrls: ["./edit-chart.component.scss"],
})
@Injectable()
export class EditChartComponent implements OnInit {
  public options2: any;
  public themeSubscription: any;
  public campos: any[];
  public camposSelecionados: any[];
  public fieldsShow: boolean[] = [];
  public selectedRow: any;
  public options: any = {};

  constructor(
    public widgetService: WidgetService,
    private msgSvc: MessageService
  ) {
    Object.assign(this, { options });
  }
  ngOnInit() {
    this.initialData();
  }

  onSaveWidget() {
    var rStat = this.widgetService.selectedWidget.params.report;
    var paramStat = this.widgetService.checkParamStatus();

    if (rStat === "" || rStat === undefined || rStat === null) {
      this.msgSvc.add({
        severity: "error",
        summary: "NENHUM RELATÓRIO SELECIONADO",
      });
      return;
    }

    if (paramStat) {
      this.msgSvc.add({
        severity: "error",
        summary: "PREENCHA OS PARÂMETROS DA BUSCA",
      });
      return;
    }

    if (this.widgetService.selectedWidget.name === null || this.widgetService.selectedWidget.name === undefined) {
      this.msgSvc.add({
        severity: "error",
        summary: "Digite o nome do Widget",
      });
      return;
    }

    if (this.widgetService.selectedWidget.id === undefined || this.widgetService.selectedWidget.id === null) {
      var status: boolean = this.widgetService.addWidget();
      this.widgetService.listWidgets()
    } else {
      // Widget already exists
      console.log("Widget id: " + this.widgetService.selectedWidget.id);
      var status: boolean = this.widgetService.updateWidget();
    }

    if (status === true) {
      this.initialData();
      this.msgSvc.add({
        severity: "success",
        summary: "WIDGET SALVO!",
      });
    } else {
      this.msgSvc.add({
        severity: "error",
        summary: "OCORREU ALGUM ERRO AO SALVAR WIDGET",
      });
    }

  }

  onButtonnSelectWidget() {
    if (!this.widgetService.listWidgets()) {
      this.msgSvc.add({
        severity: "error",
        summary: "OCORREU ALGUM ERRO AO BUSCAR WIDGETS",
      });
    }
  }

  onSelectWidget(widget: HunterWidget) {
    console.log(widget)
    widget.status = "ATIVO";
    this.widgetService.mydata = [];
    this.widgetService.processedData = [];
    this.widgetService.selectedWidget = widget;
    this.widgetService.setLoadValues();
  }

  onTableWidgetSelection() {
    console.log("I was clicked!");
  }

  onDeactivateWidget() {
    this.widgetService.selectedWidget.status = "INATIVO";
    if (this.widgetService.updateWidget() === true) {
      this.msgSvc.add({
        severity: "success",
        summary: "WIDGET DESATIVADO!",
      });
    } else {
      this.msgSvc.add({
        severity: "error",
        summary: "ERRO AO DESATIVAR WIDGET",
      });
    }
  }

  onActivateWidget() {
    this.widgetService.selectedWidget.status = "ATIVO";
    if (this.widgetService.updateWidget() === true) {
      this.msgSvc.add({
        severity: "success",
        summary: "WIDGET ATIVADO!",
      });
    } else {
      this.msgSvc.add({
        severity: "error",
        summary: "ERRO AO ATIVAR WIDGET",
      });
    }
  }

  onClearWidget() {
    
    this.widgetService.clearAllData();
    this.clearAllData();
    this.widgetService.listWidgets()
    this.msgSvc.add({
      severity: "success",
      summary: "DADOS LIMPOS!",
    });
  }

  initialData() {
    if (!this.widgetService.listWidgets()) {
      this.msgSvc.add({
        severity: "error",
        summary: "OCORREU ALGUM ERRO AO BUSCAR WIDGETS",
      });
    }
  }

  clearAllData() {
    this.clearPartialData();
    Object.assign(this, { options });
    this.initialData();
    
  }

  clearPartialData() {
    this.widgetService.processedData = [];
    this.widgetService.selectedWidget = new HunterWidget({});
    this.widgetService.selectedWidget.params = new HunterWidgetModel({});
    this.widgetService.selectedWidget.params.searchParams = [];
  }

  onSelect(data): void {
    // console.log("Item clicked", JSON.parse(JSON.stringify(data)));
  }

  RowSelected(u: any) {
    // console.log(u);
  }

  checkBoxChange(values: any, i: any): void {
    console.log(values);
    console.log(i);
    let index = this.widgetService.fieldsList.findIndex(
      (val) => val.field === i.field
    );
    this.fieldsShow[index] = values.target.checked;
    console.log(this.fieldsShow);
  }

  gerarGrafico() {
    var rStat = this.widgetService.selectedWidget.params.report;
    var paramStat = this.widgetService.checkParamStatus();

    if (rStat === "" || rStat === undefined || rStat === null || rStat.length < 1) {
      this.msgSvc.add({ severity: "error", summary: "NENHUM RELATÓRIO SELECIONADO" });
    } else if (paramStat) {
      this.msgSvc.add({
        severity: "error", summary: "PREENCHA OS PARÂMETROS DA BUSCA",
      });
    } else if (this.widgetService.checkFieldsSettings() > 0 && this.widgetService.isGroupingSelected() == 0) {
      this.msgSvc.add({
        severity: "error", summary: "UM CAMPO DEVE SER AGRUPADO POR OCORRÊNCIA",
      });
    } else if (this.widgetService.isGroupingSelected() > 1) {
      this.msgSvc.add({
        severity: "error", summary: "APENAS UM CAMPO DEVE SER AGRUPADO POR OCORRÊNCIA",
      });
    }
    else {
      this.widgetService.mydata = [];
      this.widgetService.processedData = [];
      this.widgetService.getWebSocketCode2();
    }
  }

  // Com os parâmetros fornecidos, busca os dados no WebSocket
  buscarDados() {
    var rStat = this.widgetService.selectedWidget.params.report;
    var paramStat = this.widgetService.checkParamStatus();
    // this.widgetService.mydata = [];
    if (rStat === "" || rStat === undefined || rStat === null) {
      this.msgSvc.add({
        severity: "error",
        summary: "NENHUM RELATÓRIO SELECIONADO",
      });
    } else if (paramStat) {
      this.msgSvc.add({
        severity: "error",
        summary: "PREENCHA OS PARÂMETROS DA BUSCA",
      });
    } else {
      this.widgetService.getWebSocketCode();
      this.widgetService.selectedWidget.params.widgetType = "table";
    }
  }

  // Busca parametros para cada report, os quais serão preenchidos pelo usuário
  onReportChange(event: any) {
    if (event != "" && event != undefined) {
      this.widgetService.selectedWidget.params.fields = [];
      this.widgetService.selectedWidget.params.searchParams = [];
      this.widgetService.getReportInfoParamsActions();
    }
  }

  onChangChartType() {
    console.log("ChartType Changed to: " + this.widgetService.selectedWidget.params.charttype)
  }


  checkFields(event: any, event2: any): boolean {
    var status: boolean = false;

    if (event != undefined && event2 != undefined) {
      if (event.fieldType.indexOf("any") > -1 ||
        event.fieldType.indexOf(event2.type) > -1) {
          status = true;
      }
      // if (event.name === 'group' && this.widgetService.isGroupingSelected() && this.widgetService.isGroupingSelectedField() != event2.field) {
      //   status = false;
      // } else {
      //   status = true;
      // }
    }
    return status;
  }

}
