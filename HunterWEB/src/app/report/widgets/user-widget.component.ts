import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { viewParentEl } from "@angular/core/src/view/util";
import { ActivatedRoute, Router } from "@angular/router";
import { AuthService } from "../../security/services/auth.service";
import { TokenService } from "../../security/services/token.service";
import { SocketService } from "../../shared/services/socket.service";
import { WidgetService } from "../services/widget.service";



@Component({
  selector: "widgets",
  templateUrl: "./user-widget.component.html",
  styleUrls: ["./user-widget.component.scss"],
})
export class UserWidgetComponent implements OnInit {
  public wgtService: WidgetService;
  
  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private socket: SocketService,
    private tokenSvc: TokenService,
    private authSvc: AuthService
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
    this.wgtService.findUserDashboard();
  }
}
