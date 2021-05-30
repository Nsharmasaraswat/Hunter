
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/components/common/messageservice';
import { environment } from '../../../../environments/environment';
import { AddressService } from '../../services/address.service';
// import { Spinkit } from 'ng-http-loader/spinkits';



class AddressModel {
  createdAt: string
  fields: any
  id: string
  model: any
  metaname: null
  name: string
  siblings: any
  status: null
  updatedAt: string
  wkt: string
}

@Component({
  templateUrl: './new-manager-address.component.html'
})
export class NewManagerAddressComponent implements OnInit {

  addressModelSelected = null
  addressModelSelectedDialog = null
  // public spinkit = Spinkit;
  selected: any = null;
  // AddresssModels: [] = []
  cities1: any;
  data: any[] = [];
  first: number = 0;

  displayDialog: boolean;

  address: any;

  dialogAddress = {
    createdAt: null,
    fields: null,
    id: null,
    model: null,
    metaname: null,
    name: null,
    siblings: null,
    status: null,
    updatedAt: null,
    wkt: null
  }

  addressModels = [
    { label: 'Select Address Model', value: null }
  ]
  AddresssModels2 = [
    { label: 'Select Address Model', value: null }
  ]

  addressModel = null

  addressSel

  constructor(
    private http: HttpClient,
    private msgSvc: MessageService,
    private newEMService: AddressService
  ) {

  }

  ngOnInit() {
    this.loadData()
  }

  loadData() {

    this.newEMService.loadAllAddressModels().subscribe((msg: any) => {
      console.log(msg)
      msg.map(addressModel => {
        this.addressModels.push({
          label: addressModel.name,
          value: addressModel
        })

      });

    }, error => {
      this.enviarMsg(error)
    });
  }



  getAddressAll(addressModelMetaname) {
    this.addressModel = addressModelMetaname;
    if (addressModelMetaname) {
      this.addressSel = addressModelMetaname
      this.newEMService.getAllAddress(this.addressModel.metaname).subscribe((lst: any[]) => {
        console.log(lst);
        this.data = lst;
      }, error => {
        this.enviarMsg(error)
      });

    } else {
      console.log('nulll esta')
    }
  }

  onRowSelect(event) {
    this.dialogAddress = event.data
    this.displayDialog = true;
    this.addressModelSelectedDialog = this.addressSel;
  }

  enviarMsg(error) {
    console.log(error);
    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
  }

  showDialogToAdd() {
    this.dialogAddress = new AddressModel()
    this.displayDialog = true;
    this.addressModelSelectedDialog = '';
  }

  updateAddress() {
    this.dialogAddress.model = this.addressModelSelectedDialog;
    if (this.dialogAddress.id) {
      this.newEMService.updateAddress(this.dialogAddress.id, this.dialogAddress).subscribe(data =>  {

        this.msgSvc.add({ severity: 'success', summary: 'Address Updated', detail: 'Address was updated successfully' });
        this.displayDialog = false;
        this.getAddressAll(this.addressSel)
      }, error => {
        this.enviarMsg(error)
      });

    } else {
      this.dialogAddress.model = this.addressModelSelectedDialog
      this.newEMService.setAddress(this.dialogAddress).subscribe(data =>  {
        this.msgSvc.add({ severity: 'success', summary: 'Address Created', detail: 'A new Address was successfully created' });
        this.displayDialog = false;
        this.getAddressAll(this.addressSel)
      }, error => {
        this.enviarMsg(error)
      });
    }
  }

  deleteAddress() {
    if (this.dialogAddress.id) {
      this.newEMService.deleteAddress(this.dialogAddress.id).subscribe(() =>  {
        this.msgSvc.add({ severity: 'success', summary: 'Address Deleted', detail: 'the Address has been deleted successfully' });
        this.displayDialog = false;
        this.getAddressAll(this.addressSel)
      }, error => {
        this.enviarMsg(error)
      });
    }
  }



}
