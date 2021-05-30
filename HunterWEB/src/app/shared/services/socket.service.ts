import { Injectable } from '@angular/core';
import { WebSocketService } from 'angular2-websocket-service';
import { SpinnerVisibilityService } from 'ng-http-loader/services/spinner-visibility.service';
import { QueueingSubject } from 'queueing-subject';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class SocketService {
  private inputStream: QueueingSubject<any>;
  public outputStream: Observable<any>;

  constructor(private socketFactory: WebSocketService, private spinner: SpinnerVisibilityService) { }

  public connect(url: string): Observable<any> {
    this.spinner.show();
    this.outputStream = this.socketFactory.connect(
      url,
      this.inputStream = new QueueingSubject<any>()
    );
    
    return this.outputStream.do(() => this.spinner.hide());
  }

  connect2(url: string) {
    return new Promise(function(resolve, reject) {
        var server = new WebSocket(url);
        server.onopen = function() {
            resolve(server);
        };
        server.onerror = function(err) {
            reject(err);
        };
    });
}

  public disconnect() {
    this.inputStream.unsubscribe();
  }

  public send(message: any): void {
    // If the websocket is not connected then the QueueingSubject will ensure 
    // that messages are queued and delivered when the websocket reconnects. 
    // A regular Subject can be used to discard messages sent when the websocket 
    // is disconnected.
    this.inputStream.next(message)
  }

  public isConnected(): boolean {
    return this.outputStream != null && this.inputStream != null && !this.inputStream.closed;
  }

  public hideLoadIndicator() {
    this.spinner.hide();
  }

  public showLoadIndicator() {
    this.spinner.show();
  }
}