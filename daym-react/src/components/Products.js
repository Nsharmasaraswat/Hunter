import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import {withTranslation} from "react-i18next";
import {ProductService} from "../service/ProductService";
import {ProductFieldService} from "../service/ProductFieldService";
import {Dropdown} from "primereact/dropdown";
import "./Table.css";
import {ProgressSpinner} from "primereact/progressspinner";

class Products extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            productModel:JSON.parse(localStorage.getItem('product_model')),
            productValues: [],
            productFields: [],
            productUnits: [],
            methods: [],
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
            units: [
                'EPC96',
                'EPC128',
                'LICENSEPLATES',
                'QRCODE',
                'UPCA',
                'UPCE',
                'EAN13',
                'CODE39',
                'CODE93',
                'CODE128',
                'ITF',
                'CODABAR',
                'NFC',
                'BLE',
                'RTLS',
                'EXTERNAL_SYSTEM'
            ]
        };
        this.productService = new ProductService();
        this.productFieldService = new ProductFieldService();
        this.handleCreateWarehouse = this.handleCreateWarehouse.bind(this);
        this.handleDeleteWarehouse = this.handleDeleteWarehouse.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
        this.validate = this.validate.bind(this);
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" disabled={this.state.deleteLoading} className="p-button-danger"
                        style={{marginRight: '.5em'}} onClick={() => {
                    this.handleDeleteWarehouse(rowData);
                }}/>
            }
            {
                this.state.methods.indexOf('patch') >= 0 &&
                <Button type="button" icon="pi pi-pencil" className="p-button-warning" onClick={() => {
                  //console.log(this.state.productValues);
                    this.setState({
                        display: true,
                        name: rowData.name,
                        productCode: rowData.productCode,
                        barcode: rowData.barCode,
                        userValues: this.state.productFields.map((each,i) => {
                            return ({
                                "userField": each._id,
                                "value": rowData.fields[i] ? rowData.fields[i].value : '',
                                "required": each.required
                            });
                        }),
                        productUnits: rowData.units,
                        editData: rowData
                    });
                }}/>
            }
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/products').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/products')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        let unitValues = this.state.units.map((each)=>{
            return({
                "label": each,
                "value": each
            });
        });
        const productModelId = this.props.location.pathname.split('/')[2];
        this.setState({productModelId,unitValues});
        this.productFieldService.getAllFields(productModelId).then(response => {
            let _fields = response.data.map((each)=>{
                return({
                    "productField": each._id,
                    "value": "",
                    "required": each.required
                });
            });
            this.setState({productValues: _fields, productFields: response.data})
            this.productService.getAllProducts(productModelId).then(res => {
              //console.log('Dataaaaa---->',res);
                this.setState({dataTableValue: res.data, loading: false});
            }).catch((err)=>{
                let error = err.response || err.response.data.message;
                this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('some_error')});
            });
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('some_error')});
        });
    }
    validate(){
        let _emptyFields = this.state.productValues.filter(each => each.required && !each.value);

        if(!this.state.name){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        } else if(this.state.editData && !this.state.barcode){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('barcode') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        } else if(this.state.editData && !this.state.productCode){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('productCode') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        } else if(_emptyFields.length > 0){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('all_fields_mandatory') + ' ! ' + this.props.t('can_not_be_empty') });
            return false;
        } else if(this.state.editData && this.state.productUnits.length === 0){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('no_units') });
            return false;
        }
        return true;
    }

    handleCreateWarehouse(){
        if(this.validate()) {
            this.setState({buttonLoading: true});
            if (this.state.editData) {
                const _body = {
                    name: this.state.name,
                    productCode: this.state.productCode,
                    barCode: this.state.barcode,
                    productType: this.state.productModelId,
                    fields: this.state.productValues,
                    units: this.state.productUnits,
                };
                this.productService.editProduct(this.state.editData._id, _body).then(async (response) => {
                    let _dataTable = this.state.dataTableValue;
                    const position = this.state.dataTableValue.indexOf(this.state.editData);
                  //console.log('Position',position);
                    _dataTable[position] = response.data;
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('product') + ' ' + this.props.t('edited_Successfully')
                    });
                    this.setState({
                        dataTableValue: _dataTable,
                        display: false,
                        buttonLoading: false,
                        name: '',
                        productCode: '',
                        barcode: '',
                        productValues: this.state.productValues.map((each)=>{
                            return({
                                "productField": each.productField,
                                "value": "",
                                "required": each.required
                            });
                        }),
                        productUnits: [],
                    });
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_edit') + ' ' + this.props.t('product') });
                    this.setState({buttonLoading: false});
                });
            } else {
                const _body = {
                    name: this.state.name,
                    productCode: this.state.productCode,
                    barCode: this.state.barcode,
                    productType: this.state.productModelId,
                    fields: this.state.productValues,
                    units: this.state.productUnits,
                };
              //console.log(_body);
                this.productService.createProduct(_body).then(async (response) => {
                  //console.log('Resssss---->', response.data);
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('product') + ' ' + this.props.t('created_Successfully')
                    });
                    this.setState({
                        dataTableValue: [response.data, ...this.state.dataTableValue],
                        display: false,
                        buttonLoading: false,
                        name: '',
                        productCode: '',
                        barcode: '',
                        productValues: this.state.productValues.map((each)=>{
                            return({
                                "productField": each.productField,
                                "value": "",
                                "required": each.required
                            });
                        }),
                        productUnits: [],
                    });
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_create') + ' ' + this.props.t('product') });
                    this.setState({buttonLoading: false});
                });
            }
        }
    }
    handleDeleteWarehouse(rowData){
        this.setState({deleteLoading: true});
        this.productService.deleteProduct(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('product') + ' ' + this.props.t('deleted_Successfully') });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false,
            });
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_delete') + ' ' + this.props.t('product') });
            this.setState({deleteLoading: false});
        });
    }


    render() {
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateWarehouse} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        const tableHeader = (
            <div style={{display: 'flex',justifyContent: 'flex-end'}}>
                <Dialog header={(this.state.editData ? this.props.t('edit'): this.props.t('add'))+' '+this.props.t('product')} visible={this.state.display} modal={true} style={{maxWidth: '70vw', minWidth: '30vw'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid">
                        <div className={this.state.editData ? 'p-grid p-col-12 p-md-5' : 'p-grid p-col-12 p-md-12'}>
                            <div className="p-col-12 p-md-12">
                                <InputText placeholder={this.props.t('name')} style={{width: '100%'}} value={this.state.name} onChange={(e) => this.setState({name: e.target.value})} />
                            </div>
                            {
                                this.state.editData &&
                                    <>
                                        <div className="p-col-12 p-md-12">
                                            <InputText placeholder={this.props.t('barcode')} style={{width: '100%'}} value={this.state.barcode} onChange={(e) => this.setState({barcode: e.target.value})} />
                                        </div>
                                        <div className="p-col-12 p-md-12">
                                            <InputText placeholder={this.props.t('productCode')} style={{width: '100%'}} value={this.state.productCode} onChange={(e) => this.setState({productCode: e.target.value})} />
                                        </div>
                                    </>
                            }
                            {
                                this.state.productFields.map((each,index)=>
                                    <div className="p-col-12 p-md-12" key={index}>

                                        {
                                            each.type === 'Select' ?
                                                <Dropdown style={{width: '100%'}} placeholder={each.name}
                                                          value={this.state.productValues[index].value}
                                                          options={each.values}
                                                          onChange={(event)=>{
                                                              let _values = this.state.productValues;
                                                              _values[index].value = event.value;
                                                              this.setState({productValues: _values});
                                                          }}/>
                                                :
                                                <InputText placeholder={each.name} style={{width: '100%'}} type={each.type}
                                                           value={this.state.productValues[index].value} onChange={(e) => {
                                                    let _values = this.state.productValues;
                                                    _values[index].value = e.target.value;
                                                    this.setState({productValues: _values});
                                                }}/>
                                        }
                                    </div>
                                )
                            }
                        </div>
                        {
                            this.state.editData &&
                            <div className="p-grid p-col-12 p-md-7">
                                <DataTable ref={(el) => this.dt = el} value={this.state.productUnits} selectionMode="single" header={
                                    <div style={{display: 'flex',justifyContent: 'flex-end'}}>
                                        <Dialog header={this.props.t('add_unit')} visible={this.state.display1} modal={true} style={{width: '30vw'}} footer={
                                            <div>
                                                <Button icon="pi pi-times" onClick={() => this.setState({display1:false, unitName: '', unitType: '', unitTagId: ''})} label={this.props.t('cancel')} className="p-button-secondary" />
                                                <Button icon="pi pi-check" onClick={()=>{
                                                    if(this.state.unitName && this.state.unitType && this.state.unitTagId){
                                                        let _units = this.state.productUnits;
                                                        _units.push({
                                                            name: this.state.unitName,
                                                            type: this.state.unitType,
                                                            tagId: this.state.unitTagId
                                                        });
                                                        this.setState({
                                                            unitName: '', unitType: '', unitTagId: '', productUnits: _units, display1: false
                                                        });
                                                    }else {
                                                        this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('all_fields_mandatory')});
                                                        return false;
                                                    }

                                                }} label={this.props.t('save')} />
                                            </div>
                                        } onHide={() => this.setState({display1:false, unitName: '', unitType: '', unitTagId: ''})}>
                                            <div className="p-grid">
                                                <div className="p-col-12 p-md-12">
                                                    <Dropdown style={{width: '100%'}} placeholder={this.props.t('unit_type')} value={this.state.unitType} options={this.state.unitValues} onChange={(event)=>this.setState({unitType: event.value})}/>
                                                </div>
                                                <div className="p-col-12 p-md-12">
                                                    <InputText placeholder={this.props.t('unit_tagId')} style={{width: '100%'}} value={this.state.unitTagId} onChange={(e) => this.setState({unitTagId: e.target.value})} />
                                                </div>
                                                <div className="p-col-12 p-md-12">
                                                    <InputText placeholder={this.props.t('unit_name')} style={{width: '100%'}} value={this.state.unitName} onChange={(e) => this.setState({unitName: e.target.value})} />
                                                </div>
                                            </div>
                                        </Dialog>
                                        <Button type="button" label={this.props.t('add_unit')} icon="pi pi-cog" onClick={() => this.setState({display1: true})}/>
                                    </div>
                                } paginator={true} rows={10}
                                           responsive={true} selection={this.state.productUnits1} onSelectionChange={event => this.setState({productUnits1: event.value})}>
                                    <Column field="name" header={this.props.t('unit_name')} sortable={true} />
                                    <Column field="type" header={this.props.t('unit_type')} sortable={true} />
                                    <Column field="tagId" header={this.props.t('unit_tagId')} sortable={true} />
                                    <Column header={this.props.t('delete')} body={(rowData)=>
                                        <Button type="button" icon="pi pi-times" className="p-button-danger" style={{marginRight: '.5em'}} onClick={()=>{
                                            let _productUnits = this.state.productUnits;
                                            _productUnits.splice(_productUnits.indexOf(rowData),1);
                                            this.setState({productUnits: _productUnits});
                                        }}/>
                                    } style={{textAlign:'center', width: '8em'}}/>
                                </DataTable>
                            </div>
                        }
                    </div>
                </Dialog>
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_product')} icon="pi pi-cog"
                            onClick={() => this.setState({
                                display: true,
                                name: '',
                                productCode: '',
                                barcode: '',
                                productValues: this.state.productValues.map((each) => {
                                    return ({
                                        "productField": each.productField,
                                        "value": "",
                                        "required": each.required
                                    });
                                }),
                                productUnits: [],
                                editData: null
                            })}/>
                }
            </div>
        );
        const viewDetails = (rowData) => <div>
            <Button label={this.props.t('view_product_detail')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                this.setState({detailsShow: true, selectedUnits: rowData.units, selectedFields: rowData.fields})
            }}/>
        </div>;
        let actionHeader = <Button type="button" icon="pi pi-cog"/>;
        if(!this.props.location.pathname.split('/')[2]){
            return (
                <div className="p-grid" style={{height: '75vh'}}>
                    <div className="p-col-12" style={{height: '75vh'}}>
                        <h2 style={{
                            marginTop: '35vh',
                            textAlign: 'center'
                        }}>{this.props.t('no_product_category')}</h2>
                    </div>
                </div>
            );
        }
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('products')}</h1>
                        {
                            this.state.loading ?
                                <div className="p-col-12"
                                     style={{display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
                                    <ProgressSpinner style={{width: '50px', height: '50px'}} strokeWidth="3"
                                                     animationDuration=".5s"/>
                                </div> :
                                <DataTable ref={(el) => this.dt = el} value={this.state.dataTableValue}
                                           selectionMode="single" header={tableHeader} paginator={true} rows={10}
                                           responsive={true} selection={this.state.dataTableSelection1}
                                           onSelectionChange={event => this.setState({dataTableSelection1: event.value})}>
                                    <Column field="name" header={this.props.t('name')} sortable={true}/>
                                    <Column field="barCode" header={this.props.t('barcode')} sortable={true}/>
                                    <Column field="productCode" header={this.props.t('productCode')} sortable={true}/>
                                    <Column header={this.props.t('product_detail')}
                                            body={(rowData) => viewDetails(rowData)} style={{textAlign: 'center'}}/>
                                    <Column header={actionHeader} body={this.actionButtons}
                                            style={{textAlign: 'center', width: '8em'}}/>
                                </DataTable>
                        }
                    </div>
                </div>
                <Dialog header={this.props.t('product_detail')} visible={this.state.detailsShow} modal={true} style={{width: '70vw'}} footer={
                    <div>
                        <Button icon="pi pi-times" onClick={() => this.setState({detailsShow:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                    </div>
                } onHide={() => this.setState({detailsShow:false, selectedUnits: [], selectedFields: []})}>
                    <div className="p-grid">
                        <div className="p-grid p-col-12 p-md-6">
                            <DataTable ref={(el) => this.dt = el} value={this.state.selectedFields} header={this.props.t('product_fields')} selectionMode="single" paginator={true} rows={7}
                                       responsive={true} selection={this.state.selectedFields1} onSelectionChange={event => this.setState({selectedFields1: event.value})}>
                                <Column body={(row)=>row.productField.name} header={this.props.t('product_field')} style={{textAlign:'center'}}/>
                                <Column field="value" header={this.props.t('value')} sortable={true} />
                            </DataTable>
                        </div>
                        <div className="p-grid p-col-12 p-md-6">
                            <DataTable ref={(el) => this.dt = el} value={this.state.selectedUnits} selectionMode="single" header={this.props.t('product_units')} paginator={true} rows={7}
                                       responsive={true} selection={this.state.selectedUnits1} onSelectionChange={event => this.setState({selectedUnits1: event.value})}>
                                <Column field="name" header={this.props.t('unit_name')} sortable={true} />
                                <Column field="type" header={this.props.t('unit_type')} sortable={true} />
                                <Column field="tagId" header={this.props.t('unit_tagId')} sortable={true} />
                            </DataTable>
                        </div>
                    </div>
                </Dialog>
            </div>
        );
    }
}

export default withTranslation()(Products)
