import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { AuthService } from '../../security/services/auth.service';
import { HunterPermission, HunterPermissionCategory } from '../model/HunterPermission';

@Injectable()
export class NavigationService {

  constructor(private authSvc: AuthService) { }


  getItems(): Observable<HunterPermissionCategory[]> {
    return this.authSvc.getPermissions().map((perms: HunterPermission[]) => {
      let categories: HunterPermissionCategory[] = Array.from([]);

      if (perms !== null && perms !== undefined) {
        for (let p of perms) {
          let cat = categories.find(c => c.id === p.category.id);

          if (cat === undefined) {
            let c = p.category;
            c.permissions = Array.from([]);
            c.permissions.push(p);
            categories.push(c);
          } else
            cat.permissions.push(p);
        }
      }
      return categories;
    });
  }
}
