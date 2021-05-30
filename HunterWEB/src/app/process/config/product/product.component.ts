import { Component, OnInit } from '@angular/core';
import { Spinkit } from 'ng-http-loader/spinkits';
import { MessageService } from 'primeng/components/common/messageservice';
import { HunterModelField } from '../../../shared/model/HunterModelField';
import { HunterProduct, HunterProductModel } from '../../../shared/model/HunterProduct';
import { NewManagerProductService } from '../../services/new-manager-product.service';

@Component({
  templateUrl: './product.component.html'
})
export class ProductComponent implements OnInit {

  productModelSelected = null
  productModelSelectedDialog = null
  public spinkit = Spinkit;
  selected: any = null;
  // productsModels: [] = []
  cities1: any;
  data: any[] = [];
  first: number = 0;

  displayDialog: boolean;

  produto: any;

  dialogProduct: HunterProduct;

  productsModels = [
    { label: 'Select Product Model', value: null }
  ]
  productsModels2 = [
    { label: 'Select Product Model', value: null }
  ]

  productModel = null

  productSel: HunterProductModel;

  constructor(
    private msgSvc: MessageService,
    private newPMService: NewManagerProductService
  ) {

  }

  ngOnInit() {
    this.loadData()
  }

  loadData() {
    this.newPMService.loadAllProdutsModels().subscribe((msg: any) => {
      msg.map(productModel => {
        this.productsModels.push({
          label: productModel.name,
          value: productModel
        })
      }, error => {
        this.enviarMsg(error)
      });

    });
  }


  getProduct(productModelMetaname) {
    this.productModel = productModelMetaname;
    if (productModelMetaname) {
      this.productSel = productModelMetaname
      this.newPMService.getAllProduct(this.productModel.metaname).subscribe((lst: any[]) => {
        this.data = lst;
      }, error => {
        this.enviarMsg(error)
      });
    }
  }

  onRowSelect(event) {
    this.dialogProduct = event.data
    this.displayDialog = true;
    this.productModelSelectedDialog = this.productSel;
  }

  enviarMsg(error) {
    console.log(error);
    this.msgSvc.add({ severity: 'error', summary: 'ERRO AO EXECUTAR AÇÃO', detail: 'Verificar Console' });
  }

  showDialogToAdd() {
    this.dialogProduct = new HunterProduct('');
    this.displayDialog = true;
    this.productModelSelectedDialog = '';
  }

  updateProduct() {
    this.dialogProduct.model = this.productModelSelectedDialog;
    if (this.dialogProduct.id) {
      this.newPMService.updateProduct(this.dialogProduct.id, this.dialogProduct).subscribe(data => {

        this.msgSvc.add({ severity: 'success', summary: 'Product Updated', detail: 'Product was updated successfully' });
        this.displayDialog = false;
        this.getProduct(this.productSel)
      }, error => {
        this.enviarMsg(error)
      });

    } else {
      console.log(this.dialogProduct)
      this.dialogProduct.model = this.productModelSelectedDialog
      this.newPMService.setProduct(this.dialogProduct).subscribe(data => {
        this.msgSvc.add({ severity: 'success', summary: 'Product Created', detail: 'A new product was successfully created' });
        this.displayDialog = false;
        this.getProduct(this.productSel)
      }, error => {
        this.enviarMsg(error)
      });
    }
  }

  deleteProduct() {
    if (this.dialogProduct.id) {
      this.newPMService.deleteProduct(this.dialogProduct.id).subscribe(() => {
        this.msgSvc.add({ severity: 'success', summary: 'Product Created', detail: 'the product has been deleted successfully' });
        this.displayDialog = false;
        this.getProduct(this.productSel)
      }, error => {
        this.enviarMsg(error)
      });
    }
  }

  getValue(pmf: string): string {
    let pf = this.dialogProduct.fields.find(pf => pf.model.id === pmf);
    return pf === null || pf === undefined ? '' : pf.value;
  }

  updateField(pmf: string, value: string) {
    this.dialogProduct.fields.find(pf => pf.model.id === pmf).value = value;
  }
}