import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { InternalSharedModule } from '../shared/shared.module';
import { SecurityRoutingModule } from './security-routing.module';
import { LoginComponent } from './components/login.component';
import { AuthGuard } from './guards/auth.guard';
import { AuthService } from './services/auth.service';
import { TokenService } from './services/token.service';
import { LogInterceptor } from './interceptors/log.interceptor';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { MessageService } from 'primeng/components/common/messageservice';


@NgModule({
    imports: [ InternalSharedModule, SecurityRoutingModule, HttpClientModule ],
    declarations: [ LoginComponent ],
    providers: [ SecurityRoutingModule, AuthGuard, AuthService, TokenService, MessageService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LogInterceptor,
      multi: true,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
    ],
    exports: [ LoginComponent, HttpClientModule ]
  })
  export class SecurityModule { }