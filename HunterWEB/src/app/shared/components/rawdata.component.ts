import { Component, Input } from "@angular/core";

@Component({
  selector: 'rawdata',
  templateUrl: './rawdata.component.html',
  styleUrls:['./rawdata.component.scss']
})
export class RawDataComponent {
  @Input() lstRawData: any = [];
  cols: any[];

  constructor() {
    this.cols = [
      { field: 'id', header: '#' },
      { field: 'tagId', header: 'Tag' },
      { field: 'source', header: 'Source' },
      { field: 'device', header: 'Device' },
      { field: 'port', header: 'Port' },
      { field: 'ts', header: 'Timestamp' },
      { field: 'type', header: 'Type' },
      { field: 'payload', header: 'Payload' }
    ];
  }
}
