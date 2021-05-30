import { AfterViewInit, Component, ElementRef, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { environment } from "../../../environments/environment";
import { HunterPermissionCategory } from '../../shared/model/HunterPermission';
import { NavigationService } from '../../shared/services/navigation.service';
import RestStatus from '../../shared/utils/restStatus';

@Component({
    selector: 'permission',
    providers: [NavigationService],
    templateUrl: 'menu.component.html'
})

export class MenuComponent implements OnInit, AfterViewInit {
    items: MenuItem[];
    categories: HunterPermissionCategory[];
    envColor: string;
    mult: boolean;
    filter: string;
    constructor(private navSvc: NavigationService, private elementRef: ElementRef, private route: Router) {
        this.envColor = environment.color !== undefined ? environment.color : '';
    }

    ngOnInit() {
        this.navSvc.getItems().subscribe((categories: HunterPermissionCategory[]) => {
            this.categories = categories;
            this.filterItems();
        }, (error: RestStatus) => {
            console.log(error);
        });
    }

    ngAfterViewInit() {
        this.elementRef.nativeElement.style.backgroundColor = this.envColor;
    }

    filterItems() {
        this.mult = this.filter !== undefined && this.filter !== '';
        this.items = Array.from([]);
        this.categories.sort((a, b) => {
            if (a.name < b.name) return -1;
            if (a.name > b.name) return 1;
            return 0;
        });
        for (let c of this.categories) {
            c.permissions.sort((a, b) => {
                if (a.name < b.name) return -1;
                if (a.name > b.name) return 1;
                return 0;
            });
            let links: MenuItem[] = Array.from([]);

            c.permissions
                .filter(p => this.showItem(p.name))
                .forEach(p => {
                    links.push({
                        label: p.name,
                        icon: 'fa-' + p.icon,
                        automationId: p.id,
                        routerLink: p.route,
                        routerLinkActiveOptions: { routerLinkActive: 'active' }, 
                        command: (event) => {
                            //event.originalEvent: Browser event
                            //event.item: menuitem metadata
                            this.filter = undefined;
                            this.filterItems();
                        }
                    });
                });
            if (links.length > 0) {
                this.items.push({
                    label: c.name,
                    icon: 'fa-' + c.icon,
                    items: links,
                    expanded: (this.filter !== undefined && this.filter !== '') || links.find(l => l.routerLink === this.route.url) !== undefined
                });
            }
        }
    }

    showItem(pName: string) {
        if (this.filter === undefined || this.filter === null || this.filter.trim() === '') {
            return true;
        }
        if (pName === undefined || pName === null) {
            return false;
        }
        return pName.toLowerCase().indexOf(this.filter.toLowerCase()) !== -1;
    }
}