import { Component, Input, OnChanges, SimpleChanges, OnInit, TemplateRef } from '@angular/core';
import { LocationData } from '../../shared/classes/LocationData';

@Component({
  selector: 'location-data',
  templateUrl: 'location-data.html',
  styleUrls: ['location-data.scss']
})
export class LocationDataComponent implements OnChanges, OnInit {
  @Input("locationData") locData: LocationData;

  ngOnInit() {
    console.log(typeof this.locData);
  }

  ngOnChanges(changes: SimpleChanges) {
    // console.log(changes.locData.currentValue);
    // console.log(changes.locData.previousValue);
    // You can also use categoryId.firstChange for comparing old and new values
  }
}
