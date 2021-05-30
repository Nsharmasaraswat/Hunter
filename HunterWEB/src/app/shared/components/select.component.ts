import { Component, Input, Output } from '@angular/core';

@Component({
  selector: 'hunter-select',
  templateUrl: 'select.component.html'
})
export class SelectComponent {
  @Input() items: any = {};
  @Output() selected: any

  select() {
    console.log('é bonita é bonita é bonita')
  }
}