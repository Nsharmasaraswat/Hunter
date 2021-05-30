import { Component } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { environment } from '../../../environments/environment';
import { SocketService } from '../../shared/services/socket.service';
import { RawData } from '../../shared/classes/RawData';



@Component({
    templateUrl: './rtlsws.component.html'
  })
  export class RtlsComponent {
    
    private socketSubscription: Subscription;
    msgs: RawData[] = [];
    private stream: any;

    constructor(private socket: SocketService) {
        this.stream = this.socket.connect(environment.wscore + 'user');
     
        this.socketSubscription = this.stream.subscribe(
            (message:RawData) => {
              console.log(message);
              this.msgs.push(message);
            }
        );
     
      }
     
      ngOnDestroy() {
        this.socketSubscription.unsubscribe()
      }
    

  }
  