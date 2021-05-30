import { HttpClient } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { environment } from '../../../environments/environment';
import { SocketService } from '../../shared/services/socket.service';
import { RawData } from '../../shared/classes/RawData';

@Injectable()
export class OriginWsService implements OnDestroy {
  
  private socketSubscription: Subscription;
  msgs: any[] = [];
  origin: string;
  private stream: any;

  constructor(private http: HttpClient, private socket: SocketService) {}

  conecta() {
    this.stream = this.socket.connect(environment.wsprocess + 'origin/' );

    this.socketSubscription = this.stream.subscribe(
      (message: RawData) => {
        console.log('received message from server: ', message);
        /*if(message.tagId in this.msgs) {
          this.msgs[message.tagId].count++;
          this.msgs[message.tagId].createdAt = new Date();
          this.msgs[message.tagId].lastUpdated = new Date();
        } else {
          this.msgs[message.tagId] = {};
          this.msgs[message.tagId].count = 1;
          this.msgs[message.tagId].lastUpdated = new Date();
        }*/
        //this.msgs.push(message);
        // this.msgs.slice();
      }
    );
  }

  ngOnDestroy() {
    if (this.socketSubscription != null) {
      this.socketSubscription.unsubscribe();
    }
  }

}