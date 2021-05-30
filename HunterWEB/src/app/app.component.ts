import { Component, HostListener } from '@angular/core';
import { SpinnerVisibilityService } from 'ng-http-loader/services/spinner-visibility.service';
import { Spinkit } from 'ng-http-loader/spinkits';
import { Message } from 'primeng/components/common/api';
import { MessageService } from 'primeng/components/common/messageservice';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  providers: [MessageService]
})
export class AppComponent {
  public spinkit = Spinkit;
  msgs: Message[] = [];

  constructor(private messageService: MessageService, private spinner: SpinnerVisibilityService) { }

  @HostListener('document:keydown.escape', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    this.spinner.hide();
  }
}
