import { CommonModule, registerLocaleData } from '@angular/common';
import localePT from '@angular/common/locales/pt';
import { LOCALE_ID, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { LeafletDrawModule } from '@asymmetrik/ngx-leaflet-draw';
import { StorageServiceModule } from 'angular-webstorage-service';
import { AccordionModule, CalendarModule, CheckboxModule, ConfirmationService, ConfirmDialogModule, DataGridModule, DataTableModule, DialogModule, DropdownModule, EditorModule, FileUploadModule, GrowlModule, InputMaskModule, InputSwitchModule, InputTextModule, OrderListModule, OverlayPanelModule, PanelModule, PasswordModule, PickListModule, ProgressBarModule, RadioButtonModule, ScrollPanelModule, SharedModule, SidebarModule, SliderModule, SpinnerModule, TabViewModule, ToolbarModule, TooltipModule } from 'primeng/primeng';
import { TableModule } from 'primeng/table';
import { LocationRestService } from "../process/services/location-rest.service";
import { AlertComponent } from './components/alert.component';
import { RawDataComponent } from './components/rawdata.component';
import { SelectComponent } from './components/select.component';
import { AutofocusDirective } from './directive/autofocus.directive';
import { MainScrollerDirective } from './directive/mainscroller.directive';
import { OnlyNumber } from './directive/onlynumbers.directive';
import { Uppercase } from './directive/uppercaseletters.directive';
import { FindPipe } from './pipes/find.pipe';
import { KeysPipe } from './pipes/keys.pipe';
import { ArraySortPipe } from './pipes/sort.pipe';
import { SumPipe } from './pipes/sum.pipe';
import { ThingToPropertyPipe } from './pipes/thing-to-property.pipe';
import { AlertService } from './services/alert.service';
import { LocalStorageService } from './services/localstorage.service';
import { NavigationService } from './services/navigation.service';
import { SocketService } from './services/socket.service';
import { FilterPropPipe } from './pipes/filterProp';
import { NotaFiscalComponent } from "./components/modelcomponents/nota-fiscal.component";
import { OrdemSeparacaoComponent } from "./components/modelcomponents/separacao.component";
import { OrdemMovimentacaoComponent } from "./components/modelcomponents/movimentacao.component";
import { OrdemConferenciaComponent } from "./components/modelcomponents/conferencia.component";
import { PalletComponent } from "./components/modelcomponents/pallet.component";
import { PalletHistoryComponent } from "./components/modelcomponents/pallet-history.component";
import { RemoveCommaPipe } from './pipes/removeComma';

registerLocaleData(localePT);

@NgModule({
  imports: [CommonModule, GrowlModule, DataTableModule, SharedModule, StorageServiceModule,
    CalendarModule, PickListModule, PanelModule, ScrollPanelModule, DropdownModule, ToolbarModule, DataGridModule, InputSwitchModule,
    InputTextModule, CheckboxModule, EditorModule, TableModule, DialogModule, ProgressBarModule, RadioButtonModule,
    InputMaskModule, LeafletModule, LeafletDrawModule, ConfirmDialogModule, AccordionModule, SpinnerModule, OverlayPanelModule,
    OrderListModule, SliderModule, SidebarModule, TabViewModule, TooltipModule, FileUploadModule, FormsModule],
  declarations: [KeysPipe, ArraySortPipe, SumPipe, FindPipe, RemoveCommaPipe, ThingToPropertyPipe, FilterPropPipe, RawDataComponent, AlertComponent, OnlyNumber, 
    AutofocusDirective, MainScrollerDirective, Uppercase, SelectComponent, NotaFiscalComponent, OrdemSeparacaoComponent, OrdemMovimentacaoComponent, OrdemConferenciaComponent, 
    PalletComponent, PalletHistoryComponent],
  providers: [{ provide: LOCALE_ID, useValue: 'pt' }, SocketService, AlertService, NavigationService, LocalStorageService, LocationRestService, ConfirmationService],
  exports: [CommonModule, FormsModule, KeysPipe, ArraySortPipe, SumPipe, FindPipe, ThingToPropertyPipe, FilterPropPipe, RemoveCommaPipe,
    RawDataComponent, AlertComponent, GrowlModule, DataTableModule,
    SharedModule, CalendarModule, PickListModule, ScrollPanelModule, PanelModule, DropdownModule, ToolbarModule,
    DataGridModule, InputSwitchModule, InputTextModule, CheckboxModule, AutofocusDirective, MainScrollerDirective, OnlyNumber, EditorModule, TableModule, Uppercase, DialogModule, SelectComponent,
    ProgressBarModule, RadioButtonModule, InputMaskModule, PasswordModule, LeafletModule, LeafletDrawModule, ConfirmDialogModule, AccordionModule, SpinnerModule,
    OrderListModule, SliderModule, SidebarModule, TabViewModule, TooltipModule, FileUploadModule, OverlayPanelModule,
    NotaFiscalComponent, OrdemSeparacaoComponent, OrdemMovimentacaoComponent, OrdemConferenciaComponent, PalletComponent, PalletHistoryComponent]
})
export class InternalSharedModule { }
