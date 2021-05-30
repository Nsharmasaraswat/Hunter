import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import {WarehouseService} from "../service/WarehouseService";
import {withTranslation} from "react-i18next";
import {ProgressSpinner} from "primereact/progressspinner";
import GoogleMapPolygonPicker from "./GoogleMapPolygonPicker";
import wkt from "wkt";
import {HeadQuarterService} from "../service/HeadQuarterService";

class WareHouses extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            methods: [],
            loading: true,
            buttonLoading: false,
            deleteLoading: false
        };
        this.warehouseService = new WarehouseService();
        this.hqService = new HeadQuarterService();
        this.handleCreateWarehouse = this.handleCreateWarehouse.bind(this);
        this.handleDeleteWarehouse = this.handleDeleteWarehouse.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
        this.viewDocs = this.viewDocs.bind(this);
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
                    this.setState({display: true, name: rowData.name, wkt: rowData.wkt, editData: rowData});
                }}/>
            }
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/warehouse').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/warehouse')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        const locationId = this.props.location.pathname.split('/')[2];
        this.setState({locationId});
        this.hqService.getHeadQuarterDetails(locationId).then(
          res => {
            this.setState({
              hq : res.data
            });
            this.warehouseService.getAllWarehouses(locationId||'').then(response => {
              this.setState({dataTableValue: response.data, loading: false})
            }).catch((err)=>{

            });
          }
        );

    }

    validate(){
        if(!this.state.name){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        if(!this.state.wkt){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('location') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        return true;
    }

    handleCreateWarehouse(){
        if(this.validate()) {
            this.setState({buttonLoading: true});
            if (this.state.editData) {
                let _data = {name: this.state.name , wkt: this.state.wkt};
                this.warehouseService.editWarehouse(this.state.editData._id, _data).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('wareHouses') + ' ' + this.props.t('edited_Successfully')
                    });
                    let _dataTable = this.state.dataTableValue;
                    const position = this.state.dataTableValue.indexOf(this.state.editData);
                    _dataTable[position] = response.data;
                    this.setState({
                        dataTableValue: _dataTable,
                        display: false,
                        buttonLoading: false,
                    })
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({
                        severity: 'error',
                        summary: this.props.t('error'),
                        detail: error || this.props.t('can_not_edit') + ' ' + this.props.t('wareHouses')
                    });
                    this.setState({buttonLoading: false});
                });
            } else {
                this.warehouseService.createWarehouse(this.state.name, this.state.locationId, this.state.wkt).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('wareHouses') + ' ' + this.props.t('created_Successfully')
                    });
                    this.setState({
                        dataTableValue: [response.data, ...this.state.dataTableValue],
                        display: false,
                        buttonLoading: false,
                    })
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({
                        severity: 'error',
                        summary: this.props.t('error'),
                        detail: error || this.props.t('can_not_create') + ' ' + this.props.t('wareHouses')
                    });
                    this.setState({buttonLoading: false});
                });
            }
        }

    }
    handleDeleteWarehouse(rowData){
        this.setState({deleteLoading: true});
        this.warehouseService.deleteWarehouse(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('wareHouses') + ' ' + this.props.t('deleted_Successfully') });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false,
            })
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_delete') + ' ' + this.props.t('wareHouses') });
            this.setState({deleteLoading: false});
        });
    }
    viewDocs(rowData, column) {
        return <div>
            <Button label={this.props.t('view_docks')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/dock/'+rowData._id+"/"+this.state.locationId;
            }}/>
        </div>;
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
              {this.state.hq &&
              <Dialog
                header={(this.state.editData ? this.props.t('edit') : this.props.t('add')) + this.props.t('wareHouses')}
                visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter}
                onHide={() => this.setState({display: false})}>

                <div className="p-grid">
                  <div className="p-col-12 p-md-12">
                    <InputText placeholder={this.props.t('name')} style={{width: '100%'}} value={this.state.name}
                               onChange={(e) => this.setState({name: e.target.value})}/>
                  </div>
                  <div className="p-col-12 p-md-12">
                    <GoogleMapPolygonPicker language={this.props.t('mapLocale')} setCoordinates={(value) => {
                      this.setState({wkt: value});
                    }}
                                            initialCenter={wkt.parse(this.state.hq.wkt).coordinates}
                                            coordinate={this.state.editData ? wkt.parse(this.state.editData.wkt).coordinates[0] : []}/>
                  </div>
                </div>
              </Dialog>
              }
                {
                    this.state.methods.indexOf('create') >= 0  &&
                    <Button type="button" label={this.props.t('create_warehouse')} icon="pi pi-cog"
                            onClick={() => this.setState({display: true, name: '', metaname: '', editData: null})}/>
                }
            </div>
        );
        let actionHeader = <Button type="button" icon="pi pi-cog"/>;
        if(!this.props.location.pathname.split('/')[2]){
            return (
                <div className="p-grid" style={{height: '75vh'}}>
                    <div className="p-col-12" style={{height: '75vh'}}>
                        <h2 style={{
                            marginTop: '35vh',
                            textAlign: 'center'
                        }}>{this.props.t('no_warehouse')}</h2>
                    </div>
                </div>
            );
        }
      // console.log(this.state.loading);
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('wareHouses')}</h1>
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
                                    <Column body={(row) => {
                                        if (row.occupiedStatus === 1)
                                            return this.props.t('not_occupied');
                                        else
                                            return this.props.t('occupied');
                                    }} header={this.props.t('status')} sortable={true}/>
                                    <Column header={this.props.t('docks')} body={this.viewDocs}
                                            style={{textAlign: 'center'}}/>

                                    <Column header={actionHeader} body={this.actionButtons}
                                            style={{textAlign: 'center', width: '8em'}}/>
                                </DataTable>
                        }
                    </div>
                </div>
            </div>
        );
    }
}

export default withTranslation()(WareHouses)
