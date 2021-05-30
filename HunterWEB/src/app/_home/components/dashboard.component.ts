import { Component, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { HunterPermissionCategory } from '../../shared/model/HunterPermission';
import { NavigationService } from '../../shared/services/navigation.service';
import RestStatus from '../../shared/utils/restStatus';
@Component({
  selector: 'app-dashboard',
  providers: [NavigationService],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  items: MenuItem[];
  activeItem: MenuItem;
  subitems: any[];

  constructor(private navSvc: NavigationService) { }

  ngOnInit() {
    this.items = Array.from([]);
    this.navSvc.getItems().subscribe((categories: HunterPermissionCategory[]) => {
      categories.sort((a, b) => {
        if (a.name < b.name) return -1;
        if (a.name > b.name) return 1;
        return 0;
      });
      for (let c of categories) {
        this.items.push({
          label: c.name,
          icon: 'fa-' + c.icon,
          command: () => { this.subitems = c.permissions }
        });
      }
      this.subitems = categories[0].permissions;
      this.activeItem = this.items[0];
    }, (error: RestStatus) => {
      console.log(error);
    });
  }
}