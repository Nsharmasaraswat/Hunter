import { HttpClient } from "@angular/common/http";
import { Component, ElementRef, Injectable, Input, OnInit } from "@angular/core";
import { viewParentEl } from "@angular/core/src/view/util";
import { ActivatedRoute, Router } from "@angular/router";
import { AuthService } from "../../../security/services/auth.service";
import { TokenService } from "../../../security/services/token.service";
import { HunterWidget } from "../../../shared/model/HunterWidget";
import { NavigationService } from "../../../shared/services/navigation.service";
import { SocketService } from "../../../shared/services/socket.service";
import { WidgetService } from "../../services/widget.service";


@Component({
  selector: "chart",
  providers: [NavigationService],
  templateUrl: "./chart.component.html",
  styleUrls: ["./chart.component.scss"],
})
@Injectable()
export class ChartComponent implements OnInit {
  public widget: HunterWidget = new HunterWidget({});
  public wgtService: WidgetService;

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private socket: SocketService,
    private tokenSvc: TokenService,
    private authSvc: AuthService,
    private element: ElementRef
  ) {
    this.wgtService = new WidgetService(
      this.http,
      this.router,
      this.route,
      this.socket,
      this.tokenSvc,
      this.authSvc
    );
  }
  ngOnInit() {
    this.dragElement(this.element.nativeElement, this.wgtService);
  }

  @Input()
  set _widget(c: HunterWidget) {
    this.widget = c;
    this.wgtService.mydata = [];
    this.wgtService.processedData = [];
    this.wgtService.selectedWidget = this.widget;
    this.wgtService.setLoadValues();
  }


  dragElement(elmnt, wgtService) {
    var pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;

    if (this.widget.params.top != undefined && this.widget.params.top != null) {
      elmnt.style.top = this.widget.params.top;
    }
    if (this.widget.params.left != undefined && this.widget.params.left != null) {
      elmnt.style.left = this.widget.params.left;
    }

    elmnt.onmousedown = dragMouseDown;

    function dragMouseDown(e) {
      e = e || window.event;
      e.preventDefault();
      // get the mouse cursor position at startup:
      pos3 = e.clientX;
      pos4 = e.clientY;
      document.onmouseup = closeDragElement;
      // call a function whenever the cursor moves:
      document.onmousemove = elementDrag;
    }

    function elementDrag(e) {
      e = e || window.event;
      e.preventDefault();
      // calculate the new cursor position:
      pos1 = pos3 - e.clientX;
      pos2 = pos4 - e.clientY;
      pos3 = e.clientX;
      pos4 = e.clientY;
      // set the element's new position:
      elmnt.style.top = (elmnt.offsetTop - pos2) + "px";
      elmnt.style.left = (elmnt.offsetLeft - pos1) + "px";
      
    }

    function closeDragElement() {
      // stop moving when mouse button is released:
      document.onmouseup = null;
      document.onmousemove = null;
      wgtService.updateWidgetParam(elmnt.style.top, elmnt.style.left);
    }
  }
}
