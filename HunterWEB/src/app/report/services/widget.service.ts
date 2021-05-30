import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import * as _ from "lodash";
import * as Rx from "rxjs/Rx";
import { Observable } from "rxjs/Rx";
import { environment } from "../../../environments/environment";
import { AuthService } from "../../security/services/auth.service";
import { TokenService } from "../../security/services/token.service";
import { HunterUser } from "../../shared/model/HunterUser";
import {
  Action,
  Field,
  HunterDashboard, HunterDashboardWidget, HunterWidget,
  HunterWidgetModel,
  multiChartData, Param,
  Report,
  singleChartData
} from "../../shared/model/HunterWidget";
// import { WebsocketService } from "./websocket.service";
import { SocketService } from "../../shared/services/socket.service";
import RestStatus from "../../shared/utils/restStatus";

let NONE_SELECTED: number = 0;
let FIRST_SELECTED: number = 1;
let SECOND_SELECTED: number = 2;
let BOTH_SELECTED: number = 3;

@Injectable()
export class WidgetService implements OnInit, OnDestroy {
  public allReports: Report[] = [];
  public fieldsList: Field[] = [];
  public actionsList: Action[] = [];
  public searchParams: Action[] = [];
  public mydata: any[] = [];
  public processedData: any = [];
  private stream: Observable<any>;
  public lastupdate: string = "";
  public selectedWidget: HunterWidget = new HunterWidget({});
  public widgetList: HunterWidget[] = [];
  public userDashboard: HunterDashboard = new HunterDashboard({});

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private socket: SocketService,
    private tokenSvc: TokenService,
    private authSvc: AuthService
  ) {
    this.getReportsType();
    this.selectedWidget.params = new HunterWidgetModel({});
    this.selectedWidget.status = 'ATIVO';
    this.userDashboard = new HunterDashboard({});
  }

  ngOnInit(): void {
    this.getReportsType();
  }

  clearAllData() {
    this.widgetList = [];
    this.fieldsList = [];
    this.actionsList = [];
    this.mydata = [];
    this.processedData = [];
    this.lastupdate = "";
    this.selectedWidget = new HunterWidget({});
    this.selectedWidget.status = 'ATIVO';
  }

  ngOnDestroy() {
    if (this.socket != null) {
      this.socket.disconnect();
    }
  }

  setLoadValues() {
    console.log("Cheguei aqui")
    this.getReportInfoParamsActions();
    this.getWebSocketCode();
  }

  getReportsType() {
    this.reachReportsType().then(
      (reports: any) => {
        let sortedReports: Report[] = reports.sort((obj1, obj2) => {
          if (obj1.name >= obj2.name) {
            return 1;
          }

          if (obj1.name < obj2.name) {
            return -1;
          }

          return 0;
        });
        this.allReports = sortedReports;
      }
    );
  }

  async reachReportsType(): Promise<Object | any> {
    var url = environment.dashboard;
    return await this.http.get(url)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }

  isGroupingSelected(): number {
    let status: number = 0;

    this.selectedWidget.params.fields.forEach(elem => {
      if (elem.action === 'group') {
        status = status + 1;
      }
    });

    return status;
  }

  isGroupingSelectedField(): string {
    let status: string = "";

    this.selectedWidget.params.fields.forEach(elem => {
      if (elem.action === 'group') {
        status = elem.field;
      }
    });

    return status;
  }

  getReportInfoParamsActions() {
    this.fieldsList = [];
    this.actionsList = [];

    if (this.selectedWidget.params.report.length > 0) {
      this.reachUrl("variables").then(
        (params: Param[]) => {
          if (params != undefined && params.length > 0 && this.selectedWidget.params.searchParams.length < 1) {
            params.forEach((element) => {
              element.value = "";
            });
            this.selectedWidget.params.searchParams = params;
          }
        },
        (error: RestStatus) => {
          this.selectedWidget.params.searchParams = [];
          console.log(error);
        }
      );

      this.reachUrl("columns").then(
        (fields: Field[]) => {
          this.fieldsList = fields;
          if (fields != undefined && fields.length > 0 && this.selectedWidget.params.fields.length < 1) {
            fields.forEach((element) => {
              element.action = "";
            });
            this.selectedWidget.params.fields = fields;
          }
        },
        (error: RestStatus) => {
          this.selectedWidget.params.fields = [];
          this.fieldsList = [];
          console.log(error);
        }
      );

      this.reachUrl("actions").then(
        (actions: Action[]) => {
          this.actionsList = actions;
        },
        (error: RestStatus) => {
          this.selectedWidget.params.fields = [];
          this.actionsList = [];
          console.log(error);
        }
      );
    }
  }

  async reachUrl(name: string): Promise<Object | any> {
    var baseUrl = environment.dashboard + this.selectedWidget.params.report;
    var url = baseUrl + '/' + name;
    return await this.http.get(url)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }


  checkParamStatus(): boolean {
    let temp: boolean = false;
    this.selectedWidget.params.searchParams.forEach((element) => {
      var uv = element.value;
      if (uv == undefined || uv === null || uv === "") {
        temp = true;
        return temp;
      }
    });

    return temp;
  }

  getWebSocketCode2() {
    var qtFields: number = this.checkFieldsSettings();
    if (qtFields === 0) {
      this.selectedWidget.params.widgetType = "table";
      this.selectedWidget.params.width = 1000;
    } else if (qtFields === 1) {
      this.selectedWidget.params.charttype = "bar-horizontal";
      this.selectedWidget.params.widgetType = "chart";
      this.selectedWidget.params.width = 700;
    } else if (qtFields === 2) {
      if (this.checkFieldsSettingsDataSingle()) {
        this.selectedWidget.params.charttype = "bar-vertical";
      } else {
        this.selectedWidget.params.charttype = "bar-vertical-grouped";
      }
    } else {
      this.selectedWidget.params.charttype = "bar-vertical-grouped";
    }
    this.getWebSocketCode();
  }

  getWebSocketCode() {
    if (this.selectedWidget.params.report.length > 0) {
      this.reachWSCode().then(
        (wscode: string) => {
          // console.log("WS Code: " + wscode);
          if (wscode != undefined && wscode.length > 0) {
            this.getDataFromWS(wscode);
            this.lastupdate = "Atualizado em " + new Date().toLocaleString();
          } else {
            console.log("Código não foi gerado.");
          }
        },
        (error: RestStatus) => {
          console.log(error);
        }
      );
    } else {
      console.log("Nenhum relatório Selecionado");
    }
  }


  async reachWSCode(): Promise<Object | string> {
    var url: string =
      environment.dashboard + this.selectedWidget.params.report;
    let body: string = "{ ";

    if (this.selectedWidget.params.searchParams.length > 0) {
      for (
        var i = 0;
        i < this.selectedWidget.params.searchParams.length - 1;
        i++
      ) {
        body =
          body +
          ' "' +
          this.selectedWidget.params.searchParams[i].var +
          '" : "' + "'" +
          this.selectedWidget.params.searchParams[i].value + "'" +
          '", ';
      }

      body =
        body +
        ' "' +
        this.selectedWidget.params.searchParams[
          this.selectedWidget.params.searchParams.length - 1
        ].var +
        '" : "' + "'" +
        this.selectedWidget.params.searchParams[
          this.selectedWidget.params.searchParams.length - 1
        ].value + "'" +
        '" }';
    } else {
      body = "{}";
    }

    return await this.http.post<any>(url, JSON.parse(body), {
      headers: {
        "Content-Type": "application/json",
      },
      responseType: "text" as "json",
    })
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error: ', err);
        return Observable.empty();
      }).toPromise();
  }

  private getDataFromWS(wscode: string) {
    var url = environment.dashboardws + wscode;
    this.stream = this.socket.connect(url);

    this.stream.subscribe((message: MessageEvent) => {
      var jsonData = JSON.parse(JSON.stringify(message));
      this.mydata.push(jsonData);
      this.processDataFromWS();
      return true;
    });
  }

  public processDataFromWS() {
    var qtFields: number = this.checkFieldsSettings();
    if (qtFields === 1) {
      this.prepareDataSingle();
    } else if (qtFields === 2 && this.checkFieldsSettingsDataSingle()) {
      this.prepareDataSingleFromMultiple();
    } else if (qtFields > 2) {
      this.prepareDataMulti();
    }
  }

  private prepareDataSingleFromMultiple() {
    if (this.mydata.length > 1) {
      this.selectedWidget.params.dataType = "single";
      let field1: string = '';
      let action1: string = '';
      let field2: string = '';
      let action2: string = '';

      this.selectedWidget.params.fields.forEach(el => {
        if (el.action === 'group') {
          field1 = el.field;
          action1 = el.action;
        }
        if (el.action === 'sum' || el.action === 'average') {
          field2 = el.field;
          action2 = el.action;
        }
      });


      // default vai ser barra
      let single: singleChartData[] = [];
      this.mydata.forEach((ele) => {
        var a: singleChartData = { name: "", value: 0, rawData: ele, action: "" };
        ele.forEach((el) => {
          if (el.field === field1) {
            a.action = action1;
            if (action1 === "group" || action1 === "count") {
              a.name = el.value;
            } else if (action1 === "group-year") {
              a.name = new Date(el.value).getFullYear() + "";
            } else if (action1 === "group-month") {
              a.name = new Date(el.value).getMonth() + "";
            } else if (action1 === "group-day") {
              a.name = new Date(el.value).getDay() + "";
            } else if (action1 === "group-hour") {
              a.name = new Date(el.value).getHours() + "";
            } else if (action1 === "sum") {
              a.name = el.value;
            } else if (action1 === "average") {
              a.name = el.value;
            }
          }
          if (el.field === field2) {
            a.value = el.value;
          }
        });
        single.push(a);
      });

      Rx.Observable.from(single)
        .groupBy((x) => x.name)
        .flatMap((group) => group.toArray())
        .map((g) => {
          var name = "nulo";
          if (g[0].name != null || g[0].name != undefined) {
            name = g[0].name.trim();
          }
          var myrawData: any = [];
          var valores: any = [];
          g.forEach((r) => {
            myrawData.push(r.rawData);
            var y: number = +r.value;
            valores.push(y);
          });
          if (action2 === "sum") {
            return {
              name: name,
              value: this.sum(valores),
              rawData: myrawData,
            };
          } else if (action2 === "average") {
            return {
              name: name,
              value: this.average(valores),
              rawData: myrawData,
            };
          }
        })
        .toArray().subscribe((d) => {
          this.processedData = d;
          // console.log(d);
        });
    }
  }

  average(nums) {
    return nums.reduce((a, b) => (a + b)) / nums.length;
  }

  sum(nums) {
    return nums.reduce((a, b) => (a + b));
  }

  private prepareDataSingle() {
    if (this.mydata.length > 1) {
      this.selectedWidget.params.dataType = "single";
      let field: string = '';
      let action: string = '';

      this.selectedWidget.params.fields.forEach(el => {
        if (el.action != '') {
          field = el.field;
          action = el.action;
        }
      });


      // default vai ser barra
      let single: singleChartData[] = [];
      this.mydata.forEach((ele) => {
        var a: singleChartData = { name: "", value: 1, rawData: ele, action: "" };
        ele.forEach((el) => {
          if (el.field === field) {
            a; action = action;
            if (action === "group" || action === "count") {
              a.name = el.value.trim();
            } else if (action === "group-year") {
              a.name = new Date(el.value).getFullYear() + "";
            } else if (action === "group-month") {
              a.name = new Date(el.value).getMonth() + "";
            } else if (action === "group-day") {
              a.name = new Date(el.value).getDay() + "";
            } else if (action === "group-hour") {
              a.name = new Date(el.value).getHours() + "";
            } else if (action === "sum") {
              a.name = el.value;
            } else if (action === "average") {
              a.name = el.value;
            }
          }
        });
        single.push(a);
      });


      Rx.Observable.from(single)
        .groupBy((x) => x.name)
        .flatMap((group) => group.toArray())
        .map((g) => {
          var name = "nulo";
          if (g[0].name != null || g[0].name != undefined) {
            name = g[0].name.trim();
          }
          var myrawData: any = [];
          var valores: any = [];
          g.forEach((r) => {
            myrawData.push(r.rawData);
            var y: number = +r.value;
            valores.push(y);
          });
          return {
            name: name,
            value: this.sum(valores),
            rawData: myrawData,
          };
        })
        .toArray().subscribe((d) => {
          this.processedData = d;
          // console.log(d)
        });
    }
  }

  private prepareDataMulti() {

    if (this.mydata.length > 1) {
      this.selectedWidget.params.dataType = "multi";
      let multi: multiChartData[] = [];

      let field: string = '';
      let action: string = '';
      // Acha qual será o campo principal
      this.selectedWidget.params.fields.forEach(el => {
        if (el.action === 'group') {
          field = el.field;
          action = el.action;
        }
      });


      this.mydata.forEach((ele) => {
        var a: multiChartData = new multiChartData();
        a.series = [];

        ele.forEach((el) => {
          if (el.field === field) {
              a.name = el.value;
          }
          this.selectedWidget.params.fields.forEach(el2 => {
            if (el2.action != '' && el2.field === el.field && el2.field != field) {
              let serie = { name: "", value: 1, rawData: ele, action: el2.action }
              if (el2.action === "group" || action === "count") {
                serie.name = el.value;
              } else if (el2.action === "group-year") {
                serie.name = new Date(el.value).getFullYear() + "";
              } else if (el2.action === "group-month") {
                serie.name = new Date(el.value).getMonth() + "";
              } else if (el2.action === "group-day") {
                serie.name = new Date(el.value).getDay() + "";
              } else if (el2.action === "group-hour") {
                serie.name = new Date(el.value).getHours() + "";
              } else if (el2.action === "sum") {
                let valor: number = +el.value;
                serie.name = el2.header;
                serie.value = valor;
              } else if (el2.action === "average") {
                let valor: number = +el.value;
                serie.name = el2.header;
                serie.value = valor;
              }
              a.series.push(serie);
            }
          });
        });
        multi.push(a);
      });

      Rx.Observable.from(multi)
        .groupBy((x) => x.name)
        .flatMap((group) => group.toArray())
        .map((g) => {
          var name = "nulo";
          var a: multiChartData = {
            name: g[0].name.trim(),
            series: [],
          };

          for (var i = 0; i < g.length; i++) {
            for (var j= 0; j < g[i].series.length; j++) {
                a.series.push(g[i].series[j]);
            }
          }
          var tempSeries;
          Rx.Observable.from(a.series)
            .groupBy((x2) => x2.name)
            .flatMap((group2) => group2.toArray())
            .map((g2) => {
              // console.log(g2)
              var name = "nulo";
              var val: number = 0;
              if (g2[0].name != null && g2[0].name != undefined) {
                name = g2[0].name;
              }
              var myrawData: any = [];
              var valores: number[] = [];
              g2.forEach((r) => {
                myrawData.push(r.rawData);
                var y: number = +r.value;
                valores.push(y);
              });

              let action = g2[0].action;
              let soma: number = _.sum(valores);
              let media: number = _.mean(valores);

              if (action === "average") {
                return {
                  name: name,
                  value: media,
                  rawData: myrawData,
                };
              } else {
                return {
                  name: name,
                  value: soma,
                  rawData: myrawData,
                };
              }
            })
            .toArray().subscribe((d2) => {
              tempSeries = d2;
              // console.log(d2)
            });
          a.series = tempSeries;
          return a;
        })
        .toArray().subscribe((d) => {
          this.processedData = d;
          // console.log("d:", d);
        });
      return
    }
  }


  listWidgets(): boolean {
    var status: boolean = true;
    this.reachListWidgets().then(
      (widgets: HunterWidget[]) => {
        this.widgetList = widgets;
      },
      (error: RestStatus) => {
        console.log(error);
        status = false;
      }
    );
    return status;
  }

  async reachListWidgets(): Promise<Object | any> {
    var url: string = environment.coreserver + "widget/all";
    return await this.http.get(url)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }

  findUserDashboard() {
    this.authSvc.getUser().subscribe((us: HunterUser) => {
      this.reachUserDashboard().then(
        (dashboard: HunterDashboard) => {
          console.log('Dashboard', dashboard)
          if (dashboard != null && dashboard != undefined) {
            this.userDashboard = dashboard;
          }
        },
        (error: RestStatus) => {
          console.log(error);
        }
      );
    });
  }

  async reachUserDashboard(): Promise<Object | any> {
    var url: string = environment.coreserver + "dashboard";
    return await this.http.get(url)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }

  updateWidgetParam(top: any, left: any): boolean {
    var status: boolean = true;
    if (this.selectedWidget.id != undefined && this.selectedWidget.id != null) {
      this.selectedWidget.params.top = top;
      this.selectedWidget.params.left = left;
      this.reachUpdateWidget().then(
        (result) => {
          // console.log(result)
          status = true;
        },
        (error: RestStatus) => {
          console.log(error);
          status = false;
        }
      );
    }
    return status;
  }


  updateWidget(): boolean {
    var status: boolean = true;
    this.reachUpdateWidget().then(
      (result) => {
        // console.log(result)
        status = true;
      },
      (error: RestStatus) => {
        console.log(error);
        status = false;
      }
    );

    return status;
  }

  async reachUpdateWidget(): Promise<Object | any> {
    var url: string =
      environment.tempreportserver + "widget/" + this.selectedWidget.id;
    return await this.http.put(url, this.selectedWidget)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }

  widgetIsInDashborad(id: string): boolean {
    let status: boolean = false;
    this.userDashboard.widgets.forEach(element => {
      if (element.widget.id === id) {
        status = true;
      }
    });
    return status;
  }


  addUserDashboard(wgt: HunterWidget): boolean {
    var status: boolean = true;
    this.authSvc.getUser().subscribe((us: HunterUser) => {
      this.reachAddUserDashboard(us, wgt).then(
        (result: any) => {
          // console.log(result)
          this.userDashboard = result;
          status = true;
        },
        (error: RestStatus) => {
          console.log(error);
          status = false;
        }
      );
    });
    return status;
  }

  async reachAddUserDashboard(us: HunterUser, wgt: HunterWidget): Promise<Object | any> {
    var url: string = environment.tempreportserver + "dashboard/";
    var body: string = "";
    var metaname = "DSB " + us.name;
    metaname = metaname.replace(/\s/g, "").toUpperCase();
    this.userDashboard.user = us;
    this.userDashboard.metaname = metaname;
    this.userDashboard.name = us.name;
    let dw: HunterDashboardWidget = new HunterDashboardWidget({})
    dw.widget = wgt;
    this.userDashboard.widgets.push(dw);

    return await this.http.post<any>(url, this.userDashboard)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }

  addWidget(): boolean {
    let status: boolean = true;
    this.authSvc.getUser().subscribe((us: HunterUser) => {
      this.reachAddWidget(us).then(
        (result: any) => {
          console.log(result);
          status = true;
        },
        (error: RestStatus) => {
          status = false;
          console.log(error);
        }
      );
    });
    return status;
  }

  async reachAddWidget(us: HunterUser): Promise<Object | any> {
    var url: string = environment.tempreportserver + "widget/";
    this.selectedWidget.metaname = "WDGT" + this.selectedWidget.name.replace(/\s/g, "").toUpperCase();
    this.selectedWidget.status = "ATIVO";
    this.selectedWidget.user.id = us.id;

    return await this.http.post<any>(url, this.selectedWidget)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }

  updateUserDashboard(wgt: HunterWidget): boolean {
    var status: boolean = true;
    this.reachUpdateUserDashboard(wgt).then(
      (result: any) => {
        // console.log(result);
        status = true;
      },
      (error: RestStatus) => {
        status = false;
        console.log(error);
      });
    return status;
  }

  async reachUpdateUserDashboard(w: HunterWidget): Promise<Object | HunterWidget> {
    var url: string = "";
    url = environment.tempreportserver + "dashboard/" + this.userDashboard.id;
    let widgets: HunterDashboardWidget[] = this.userDashboard.widgets;
    this.userDashboard.widgets = [];
    let dw: HunterDashboardWidget = new HunterDashboardWidget({})
    dw.widget = w;
    widgets.push(dw);
    this.userDashboard.widgets = widgets;

    return await this.http.put(url, this.userDashboard)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }

  removeWidgetUserDashboard(wgt: HunterWidget): boolean {
    var status: boolean = true;
    this.reachRemoveWidgetUserDashboard(wgt).then(
      (result) => {
        // console.log(result)
        status = true;
      },
      (error: RestStatus) => {
        console.log(error);
        status = false;
      }
    );

    return status;
  }

  async reachRemoveWidgetUserDashboard(wgt: HunterWidget): Promise<Object | HunterWidget> {
    var url =
      environment.tempreportserver + "dashboard/" + this.userDashboard.id;

    this.userDashboard.widgets.forEach((element, index) => {
      if (element.widget.id === wgt.id) {
        this.userDashboard.widgets.splice(index, 1);
      }
    });

    return await this.http.put(url, this.userDashboard)
      .timeout(36000000)
      .retry(0)
      .catch((err: HttpErrorResponse) => {
        console.log('Error', err);
        return Observable.empty();
      }).toPromise();
  }

  checkFieldsSettings(): number {
    let status: number = 0;
    if (this.selectedWidget.params.fields.length > 0) {
      this.selectedWidget.params.fields.forEach(el => {
        if (el.action != '') {
          status = status + 1;
        }
      });
    }

    return status;
  }

  checkFieldsSettingsDataSingle(): boolean {
    let status: boolean = false;
    if (this.selectedWidget.params.fields.length > 0 && this.checkFieldsSettings() === 2) {
      let actions: string[] = [];
      this.selectedWidget.params.fields.forEach(el => {
        actions.push(el.action);
      })
      if (actions.indexOf('group') > -1 && (actions.indexOf('sum') > -1 || actions.indexOf('average') > -1)) {
        status = true;
      }
    }

    return status;
  }
}
