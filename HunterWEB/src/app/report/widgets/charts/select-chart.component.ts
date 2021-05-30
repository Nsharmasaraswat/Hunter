import { HttpClient } from "@angular/common/http";
import { Component, Injectable, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { AuthService } from "../../../security/services/auth.service";
import { TokenService } from "../../../security/services/token.service";
import { HunterWidget } from "../../../shared/model/HunterWidget";
import { NavigationService } from "../../../shared/services/navigation.service";
import { SocketService } from "../../../shared/services/socket.service";
import { WidgetService } from "../../services/widget.service";

import { options } from "../../widgets/util/optionsData";

@Component({
  selector: "select-chart",
  providers: [NavigationService],
  templateUrl: "./select-chart.component.html",
  styleUrls: ["./select-chart.component.scss"],
})
@Injectable()
export class SelectChartComponent implements OnInit {
  public widgetList: HunterWidget[] = [];
  public selectedWgt: HunterWidget = new HunterWidget({});
  public options: any = {};

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private socket: SocketService,
    private tokenSvc: TokenService,
    private authSvc: AuthService,
    private msgSvc: MessageService,
    public wgtService: WidgetService,
  ) {
    Object.assign(this, { options });
  }

  getReportName(reportName: string) {
    for (var i = 0; i < this.wgtService.allReports.length; i++) {
      if (this.wgtService.allReports[i].fileName === reportName)
        return this.wgtService.allReports[i].name;
    }
  }

  getWidgetTypeName(name: string) {
    for (var i = 0; i < this.options.widgetType.length; i++) {
      if (this.options.widgetType[i].name === name)
        return this.options.widgetType[i].display;
    }
  }

  getWidgetChartTypeName(name: string) {
    for (var i = 0; i < this.options.chartTypes.length; i++) {
      for (var j = 0; j < this.options.chartTypes[i].chartTypes.length; j++) {
        if (this.options.chartTypes[i].chartTypes[j].name === name)
          return this.options.chartTypes[i].display + " - " + this.options.chartTypes[i].chartTypes[j].display;
      }
    }
  }

  ngOnInit() {
    this.wgtService.listWidgets();
    this.wgtService.findUserDashboard();
    // this.wgtService.selectedWgt = this.selectedWgt;
  }

  addWidget() {
    if (this.wgtService.userDashboard.id === undefined || this.wgtService.userDashboard.id === null) {
      if (this.wgtService.addUserDashboard(this.selectedWgt)) {
        this.unselectWidget();
        this.msgSvc.add({
          severity: "success",
          summary: "WIDGET ADICIONADO!",
        });
      } else {
        this.msgSvc.add({
          severity: "error",
          summary: "ERRO AO ADICIONAR WIDGET",
        });
      }
    } else {
      if (!this.wgtService.widgetIsInDashborad(this.selectedWgt.id)) {
        if (this.wgtService.updateUserDashboard(this.selectedWgt)) {
          this.unselectWidget();
          this.msgSvc.add({
            severity: "success",
            summary: "WIDGET ADICIONADO!",
          });
        } else {
          this.msgSvc.add({
            severity: "error",
            summary: "ERRO AO ADICIONAR WIDGET",
          });
        }
      } else {
        this.msgSvc.add({
          severity: "warn",
          summary: "WIDGET JÁ ESTÁ NA LISTA",
        });
      }

    }
  }

  unselectWidget() {
    this.selectedWgt = new HunterWidget({});
  }

  removeWidget() {
    console.log(event)
    if (this.selectedWgt.id != undefined && this.selectedWgt.id != null) {
      this.wgtService.removeWidgetUserDashboard(this.selectedWgt);
      this.unselectWidget();
      this.msgSvc.add({
        severity: "success",
        summary: "WIDGET REMOVIDO!",
      });
    }
  }

  setWidget(event: HunterWidget) {
    this.selectedWgt = event;
  }

  userHasWidget(event: HunterWidget): string {
    if (this.wgtService.widgetIsInDashborad(event.id)) {
      return 'true';
    } else {
      return 'false';
    }
  }

}
