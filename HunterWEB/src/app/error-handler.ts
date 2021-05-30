import { ErrorHandler, Inject, Injectable, Injector } from '@angular/core';
import { Router } from '@angular/router';
import { BAD_REQUEST, FORBIDDEN, UNAUTHORIZED } from "http-status-codes";
import { MessageService } from "primeng/components/common/messageservice";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
    private static readonly REFRESH_PAGE_ON_TOAST_CLICK_MESSAGE: string = "Ocorreu um erro inesperado: ";
    private static readonly DEFAULT_ERROR_TITLE: string = "ERRO";

    constructor(@Inject(MessageService) private msgSvc: MessageService, @Inject(Injector) private injector: Injector) { }

    handleError(error) {
        const router = this.injector.get(Router);
        const message = error.message ? error.message : error.toString();
        let httpErrorCode = error.httpErrorCode;

        switch (httpErrorCode) {
            case UNAUTHORIZED:
                router.navigate(['/security/login']);
                break;
            case FORBIDDEN:
                router.navigate(['/security/login']);
                break;
            case BAD_REQUEST:
                this.showError(message);
                break;
            default:
                this.showError(message);
        }
        throw error;
    }

    private showError(message: string) {
        this.msgSvc.add({ severity: 'error', summary: GlobalErrorHandler.DEFAULT_ERROR_TITLE, detail: GlobalErrorHandler.REFRESH_PAGE_ON_TOAST_CLICK_MESSAGE + message });
    }
}
