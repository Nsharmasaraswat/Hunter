import { HunterUUIDModel } from "./HunterUUIDModel";
import { HunterUser } from "./HunterUser";



export class HunterDashboard extends HunterUUIDModel {
  widgets: HunterDashboardWidget[] = [];
  user: HunterUser = new HunterUser({});

  constructor(init: any) {
    super(init);
  }
}

export class HunterDashboardWidget extends HunterUUIDModel {
  widget: HunterWidget = new HunterWidget({});

  constructor(init: any) {
    super(init);
  }
}


// Map<String, any>;
export class HunterWidget extends HunterUUIDModel {
  params: HunterWidgetModel = new HunterWidgetModel({});
  user: HunterUser = new HunterUser({});

  constructor(init: any) {
    super(init);
  }
}

export class HunterWidgetSearchParam {
  field: string;
  type: string;
  var: string;
  value: any;

  constructor(init: any) {
    if (this.field != undefined) {
      this.field = init.field;
    }
    if (this.type != undefined) {
      this.type = init.type;
    }
    if (this.var != undefined) {
      this.var = init.var;
    }
    if (this.value != undefined) {
      this.value = init.value;
    }
  }

}

export class Report {
  fileName: string;
  name: string;
}

export class Param {
  field: string;
  type: string;
  var: string;
  value: string = "";
}


export class Field {
  field: string;
  header: string;
  type: string;
  width: string;
  action: string;
  order: number;
}

export class Action {
  action: string;
  field: string;
  icon: string;
  name: string;
}

export class singleChartData {
  name: string;
  value: number;
  rawData: any;
  action: string;
}

export class multiChartData {
  name: any;
  series: singleChartData[];

  constructor() {
    this.name = "";
    this.series = [];
  }
}

export class HunterWidgetModel {
  width: number = 700;
  height: number = 400;
  top: string = "";
  left: string = "";
  min: number = null;
  max: number = null;
  maxRadius: number = null;
  minRadius: number = null;
  yScaleMin: number = null;
  yScaleMax: number = null;
  xScaleMin: number = null;
  xScaleMax: number = null;
  realTime: boolean = true;
  gradient: boolean = true;
  eixoX: boolean = true;
  eixoY: boolean = true;
  lengendas: boolean = true;
  showtitle: boolean = true;
  showlastupdate: boolean = true;
  legend: boolean = false;
  showLabels: boolean = true;
  animations: boolean = true;
  xAxis: boolean = true;
  yAxis: boolean = true;
  showYAxisLabel: boolean = false;
  showXAxisLabel: boolean = false;
  timeline: boolean = true;
  units: string = "";
  report: string = "";
  fields: Field[] = [];
  lengendAlignment: string = "center";
  widgetType: string = "chart";
  legendPosition: string = "right";
  charttype: string = "bar-horizontal";
  xAxisLabel: string = "x label";
  yAxisLabel: string = "y label";
  legendTitle: string = "Legenda";
  schemeType: string = "linear";
  colorScheme: string = "natural";
  dataType: string = "single";
  searchParams: HunterWidgetSearchParam[] = [];

  constructor(init: any) {
    if (init != undefined && init != null && init.lengthh > 0) {
      if (init.legendTitle != undefined) {
        this.legendTitle = init.legendTitle;
      }


      if (init.width != undefined) {
        this.width = init.width;
      }

      if (init.height != undefined) {
        this.height = init.height;
      }

      if (init.min != undefined) {
        this.min = init.min;
      }

      if (init.max != undefined) {
        this.max = init.max;
      }

      if (init.maxRadius != undefined) {
        this.maxRadius = init.maxRadius;
      }

      if (init.minRadius != undefined) {
        this.minRadius = init.minRadius;
      }

      if (init.yScaleMin != undefined) {
        this.yScaleMin = init.yScaleMin;
      }

      if (init.yScaleMax != undefined) {
        this.yScaleMax = init.yScaleMax;
      }

      if (init.xScaleMin != undefined) {
        this.xScaleMin = init.xScaleMin;
      }

      if (init.xScaleMax != undefined) {
        this.xScaleMax = init.xScaleMax;
      }

      if (init.realTime != undefined) {
        this.realTime = init.realTime;
      }

      if (init.gradient != undefined) {
        this.gradient = init.gradient;
      }

      if (init.eixoX != undefined) {
        this.eixoX = init.eixoX;
      }

      if (init.eixoY != undefined) {
        this.eixoY = init.eixoY;
      }

      if (init.lengendas != undefined) {
        this.lengendas = init.lengendas;
      }

      if (init.showtitle != undefined) {
        this.showtitle = init.showtitle;
      }

      if (init.showlastupdate != undefined) {
        this.showlastupdate = init.showlastupdate;
      }

      if (init.legend != undefined) {
        this.legend = init.legend;
      }

      if (init.showLabels != undefined) {
        this.showLabels = init.showLabels;
      }

      if (init.animations != undefined) {
        this.animations = init.animations;
      }

      if (init.xAxis != undefined) {
        this.xAxis = init.xAxis;
      }

      if (init.yAxis != undefined) {
        this.yAxis = init.yAxis;
      }

      if (init.showYAxisLabel != undefined) {
        this.showYAxisLabel = init.showYAxisLabel;
      }

      if (init.showXAxisLabel != undefined) {
        this.showXAxisLabel = init.showXAxisLabel;
      }

      if (init.timeline != undefined) {
        this.timeline = init.timeline;
      }

      if (init.units != undefined) {
        this.units = init.units;
      }

      if (init.report != undefined) {
        this.report = init.report;
      }

      if (init.fields != undefined) {
        this.fields = init.fields;
      }

      if (init.lengendAlignment != undefined) {
        this.lengendAlignment = init.lengendAlignment;
      }

      if (init.widgetType != undefined) {
        this.widgetType = init.widgetType;
      }

      if (init.legendPosition != undefined) {
        this.legendPosition = init.legendPosition;
      }

      if (init.charttype != undefined) {
        this.charttype = init.charttype;
      }

      if (init.xAxisLabel != undefined) {
        this.xAxisLabel = init.xAxisLabel;
      }

      if (init.yAxisLabel != undefined) {
        this.yAxisLabel = init.yAxisLabel;
      }

      if (init.schemeType != undefined) {
        this.schemeType = init.schemeType;
      }

      if (init.colorScheme != undefined) {
        this.colorScheme = init.colorScheme;
      }

      if (init.dataType != undefined) {
        this.dataType = init.dataType;
      }
    }
  }
}
