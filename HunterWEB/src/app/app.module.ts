import { HttpClientJsonpModule, HttpClientModule } from "@angular/common/http";
import { ErrorHandler, NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { WebSocketService } from "angular2-websocket-service";
import { NgHttpLoaderModule } from "ng-http-loader/ng-http-loader.module";
import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { GlobalErrorHandler } from "./error-handler";
import { SecurityModule } from "./security/security.module";
import { InternalSharedModule } from "./shared/shared.module";
import { DashboardComponent } from './_home/components/dashboard.component';
import { MenuComponent } from "./_home/components/menu.component";
import { UserComponent } from "./_home/components/user.component";
import { HomeComponent } from "./_home/home.component";
import { MessageService } from "primeng/components/common/messageservice";
import { TabMenuModule } from "primeng/tabmenu";
import { PanelMenuModule } from "primeng/panelmenu";
import { InputSwitchModule } from 'primeng/inputswitch';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    MenuComponent,
    UserComponent,
    DashboardComponent
  ],
  imports: [
    BrowserModule,
    SecurityModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    InternalSharedModule,
    HttpClientModule,
    HttpClientJsonpModule,
    NgHttpLoaderModule,
    TabMenuModule,
    PanelMenuModule,
    InputSwitchModule
  ],
  providers: [
    WebSocketService,
    MessageService,
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
