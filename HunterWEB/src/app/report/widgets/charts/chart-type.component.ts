import { Component, Input, OnInit } from "@angular/core";
import { MessageService } from "primeng/components/common/messageservice";
import { AuthService } from "../../../security/services/auth.service";
import { TokenService } from "../../../security/services/token.service";
import { HunterWidget } from "../../../shared/model/HunterWidget";
import { WidgetService } from "../../services/widget.service";
import { options } from "../util/optionsData";


import {
  Field
} from "../../../shared/model/HunterWidget";

@Component({
  selector: "charttype",
  templateUrl: "./chart-type.component.html",
  styleUrls: ["./chart-type.component.scss"],
})
export class ChartTypeComponent implements OnInit {
  public widgetdata: any;
  public modalId = 'selectedData';
  public data: any = [];
  public colorScheme: any;
  public selectedData: any;
  public selectedRawData: any = [];
  public selectedRow: any;
  public fieldsList: Field[] = [];

  constructor(
    private widgetService: WidgetService,
    private messageService: MessageService,
    private tokenSvc: TokenService,
    private authSvc: AuthService
  ) { }

  ngOnInit() { }

  @Input()
  set _widgetdata(widget: HunterWidget) {
    this.widgetdata = widget.params;

    options.colorScheme.forEach((element) => {
      if (widget.params.colorScheme === element.scheme.name) {
        this.colorScheme = element.scheme;
      }
    });
  }

  @Input()
  set _data(value: any) {
    this.data = value;
  }

  @Input()
  set _columns(value: any) {
    this.fieldsList = value;
  }

  @Input()
  set _colorScheme(c: string) {
    options.colorScheme.forEach((element) => {
      if (c === element.scheme.name) {
        this.colorScheme = element.scheme;
      }
    });
  }

  RowSelected(u: any) {
    console.log("Modal table row clicked");
    console.log(u);
  }


  onSelect(selectedValues): void {
    this.selectedRawData = [];
    this.selectedData = {};
    this.selectedRow = {};
    let r = Math.random().toString(36).substring(7);
    this.modalId = r;

    var type = this.widgetdata.charttype;

    if (
      type === "bar-horizontal" ||
      type === "bar-vertical" ||
      type === "pie" ||
      type === "pie-grid" ||
      type === "pie-advanced" ||
      type === "tree-map" ||
      type === "cards" ||
      type === "gauge"
    ) {
      this.data.forEach((element) => {
        if (element.name === selectedValues.name) {
          this.selectedData = element;
          this.selectedRawData = element.rawData;
        }
      });
    } else if (
      type === "bar-vertical-stacked" ||
      type === "bar-vertical-grouped" ||
      type === "bar-horizontal-normalized" ||
      type === "bar-horizontal-stacked" ||
      type === "bar-horizontal-grouped" ||
      type === "bar-vertical-normalized"
    ) {
      if (selectedValues.rawData != undefined) {
        this.selectedRawData = selectedValues.rawData;
      }
    } else if (
      type === "line" ||
      type === "area" ||
      type === "heat-map" ||
      type === "area-normalized"
    ) {
      this.data.forEach((element) => {
        if (element.name === selectedValues.series) {
          element.series.forEach((el) => {
            if (el.name === selectedValues.name) {
              this.selectedData = el;
              this.selectedRawData = el.rawData;
            }

          });
        }
      });
    }
  }

  onActivate(data): void {
    //console.log("Activate", JSON.parse(JSON.stringify(data)));
  }

  onDeactivate(data): void {
   
  }

  onDeselect() {
    this.selectedRawData = [];
    this.selectedData = {};
    this.selectedRow = {};
  }
}
