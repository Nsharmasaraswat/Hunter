import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SocketService } from '../../shared/services/socket.service';
import { RawData } from '../../shared/classes/RawData';


@Component({
  templateUrl: './view-origin.component.html'
})
export class ViewOriginComponent implements OnInit, OnDestroy {

  private socketSubscription: Subscription;
  itens: any = {};
  msgs: any[] = [];
  origin: string;
  private stream: any;

  constructor(private http: HttpClient, private socket: SocketService) {

  }
  ngOnInit(): void {
    this.http.get(environment.processserver + "origin").subscribe(data => {
      this.itens = data;
    });
  }

  conecta(event) {
    console.log(event.target.value);
    // if(this.stream != null) {
    //   this.stream.close();
    // }
    this.stream = this.socket.connect(environment.wsprocess + 'origin/' + event.target.value);

    this.socketSubscription = this.stream.subscribe(
      (message: RawData) => {
        console.log('received message from server: ', message);
        if(message.tagId in this.msgs) {
          this.msgs[message.tagId].count++;
          this.msgs[message.tagId].createdAt = new Date();
          this.msgs[message.tagId].lastUpdated = new Date();
        } else {
          this.msgs[message.tagId] = {};
          this.msgs[message.tagId].count = 1;
          this.msgs[message.tagId].lastUpdated = new Date();
        }
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
