import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/components/common/messageservice";
import { environment } from "../../../environments/environment";
import { TokenService } from "../../security/services/token.service";
import { SocketService } from "../../shared/services/socket.service";

@Component({
    selector: 'file-upload',
    templateUrl: './upload-images.component.html',
    styleUrls: ['upload-images.component.scss'],
})

export class UploadImagesComponent implements OnInit {

    fileUploadREST = environment.coreserver + 'file/upload';

    constructor(private msgSvc: MessageService, private socket: SocketService,
        private token: TokenService, private http: HttpClient,
        private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {

    }

    upload(event) {
        let size = event.files.length;
        let summary = size + ' File' + (size == 1 ? '' : 's') + 'Uploaded';

        this.msgSvc.add({ severity: 'info', summary: summary, detail: 'Start Processing Them' });
    }

    beforeUpload(event) {
        console.log(event.xhr);
    }
}