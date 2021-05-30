import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import {DockService} from "../service/DockService";
import {withTranslation} from "react-i18next";
import {ProgressSpinner} from "primereact/progressspinner";
import moment from "moment";
import {Calendar} from "primereact/calendar";
import {AppointmentService} from "../service/AppointmentService";
import {MultiSelect} from "primereact/multiselect";
import GoogleMapPolygonPicker from "./GoogleMapPolygonPicker";
import wkt from "wkt";
import {WarehouseService} from "../service/WarehouseService";

class Docks extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            productTypes:[],
            methods: [],
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
        };
        this.DockService = new DockService();
        this.wareHouseService = new WarehouseService();
        this.appointmentService = new AppointmentService();
        this.viewAdmins = this.viewAdmins.bind(this);
        this.handleCreateDock = this.handleCreateDock.bind(this);
        this.handleDeleteDock = this.handleDeleteDock.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" disabled={this.state.deleteLoading} className="p-button-danger"
                        style={{marginRight: '.5em'}} onClick={() => {
                    this.handleDeleteDock(rowData);
                }}/>
            }
            {
                this.state.methods.indexOf('patch') >= 0 &&
                <Button type="button" icon="pi pi-pencil" className="p-button-warning" onClick={() => {
                    this.setState({
                        display: true,
                        name: rowData.name,
                        wkt: rowData.wkt,
                        startingTime: new Date(rowData.startingTime),
                        endingTime: new Date(rowData.endingTime),
                        selectedProducts: rowData.productTypes,
                        editData: rowData
                    });
                }}/>
            }
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/dock').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/dock')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        const parentId = this.props.location.pathname.split('/')[2];
        const locationId = this.props.location.pathname.split('/')[3];
        this.setState({locationId,parentId});
        this.DockService.getAllDocks(parentId||'').then(response => {
            // console.log(response.data);
            this.setState({dataTableValue: response.data, loading: false})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });
        this.wareHouseService.getWareHouseDetails(parentId).then(
          res => {
            this.setState({
              wareHouse : res.data
            });
            this.appointmentService.getAllProductTypes().then(response => {
              this.setState({allProducts: response.data.map((each)=>{
                  return {
                    label: each.name,
                    value: each._id
                  };
                })})
            }).catch((err) => {
              let error = err.response.data.message;
              this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
              });
            });
          }
        )

    }

    validate(){
        if(!this.state.name){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        if(!this.state.startingTime){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('start_time') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        if(!this.state.endingTime){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('end_time') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        if(!this.state.selectedProducts || this.state.selectedProducts.length === 0){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('product_types') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        if(!this.state.wkt){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('location') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        return true;
    }

    handleCreateDock(){
        if(this.validate()) {
            this.setState({buttonLoading: true});
            if (this.state.editData) {
                let _data = {name: this.state.name,startingTime: this.state.startingTime,endingTime: this.state.endingTime,productTypes: this.state.selectedProducts, wkt: this.state.wkt};
                this.DockService.editDock(this.state.editData._id, _data).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('dock') + ' ' + this.props.t('edited_Successfully')
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
                        detail: error || this.props.t('can_not_edit') + ' ' + this.props.t('dock')
                    });
                    this.setState({buttonLoading: false});
                });
            } else {
                this.DockService.createDock(this.state.name,this.state.startingTime,this.state.endingTime,this.state.selectedProducts, this.state.locationId, this.state.parentId,this.state.wkt).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('dock') + ' ' + this.props.t('created_Successfully')
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
                        detail: error || this.props.t('can_not_create') + ' ' + this.props.t('dock')
                    });
                    this.setState({buttonLoading: false});
                });
            }
        }

    }

    handleDeleteDock(rowData){
        this.setState({deleteLoading: true});
        this.DockService.deleteDock(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('dock') + ' ' + this.props.t('deleted_Successfully') });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false,
            })
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_delete') + ' ' + this.props.t('dock') });
            this.setState({deleteLoading: false});
        });
    }

    viewAdmins(rowData, column) {
        return <div>
            <Button label={this.props.t('view_admins')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/manage-admins/'+rowData._id;
            }}/>
        </div>;
    }

    render() {
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateDock} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        const tableHeader = (
            <div style={{display: 'flex',justifyContent: 'flex-end'}}>
              {this.state.wareHouse &&
              <Dialog
                header={(this.state.editData ? this.props.t('edit') : this.props.t('add')) + ' ' + this.props.t('docks')}
                visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter}
                onHide={() => this.setState({display: false})}>
                <div className="p-grid p-fluid">
                  <div className="p-col-12 p-md-12">
                    <InputText placeholder={this.props.t('name')} style={{width: '100%'}} value={this.state.name}
                               onChange={(e) => this.setState({name: e.target.value})}/>
                  </div>
                  <div className="p-col-12 p-md-6">
                    <Calendar locale={{
                        firstDayOfWeek: parseInt(this.props.t('firstDayOfWeek')),
                        dayNames: JSON.parse(this.props.t('dayNames')),
                        dayNamesShort: JSON.parse(this.props.t('dayNamesShort')),
                        dayNamesMin: JSON.parse(this.props.t('dayNamesMin')),
                        monthNames: JSON.parse(this.props.t('monthNames')),
                        monthNamesShort: JSON.parse(this.props.t('monthNamesShort')),
                        today: this.props.t('today'),
                        clear: this.props.t('clear'),
                    }} placeholder={this.props.t('start_time')} timeOnly={true} showTime={true}
                              value={this.state.startingTime} onChange={(e) => {
                      this.setState({startingTime: e.value});
                    }}/>
                  </div>
                  <div className="p-col-12 p-md-6">
                    <Calendar locale={{
                        firstDayOfWeek: parseInt(this.props.t('firstDayOfWeek')),
                        dayNames: JSON.parse(this.props.t('dayNames')),
                        dayNamesShort: JSON.parse(this.props.t('dayNamesShort')),
                        dayNamesMin: JSON.parse(this.props.t('dayNamesMin')),
                        monthNames: JSON.parse(this.props.t('monthNames')),
                        monthNamesShort: JSON.parse(this.props.t('monthNamesShort')),
                        today: this.props.t('today'),
                        clear: this.props.t('clear'),
                    }} placeholder={this.props.t('end_time')} timeOnly={true} showTime={true}
                              value={this.state.endingTime} onChange={(e) => {
                      this.setState({endingTime: e.value});
                    }}/>
                  </div>
                  <div className="p-col-12 p-md-12">
                    <p>{this.props.t('product_types')}</p>
                    <MultiSelect value={this.state.selectedProducts} options={this.state.allProducts}
                                 onChange={event => {
                                   this.setState({selectedProducts: event.value})
                                 }} filter={true}/>
                  </div>
                </div>
                <div className="p-grid">
                  <div className="p-col-12 p-md-12">
                    <GoogleMapPolygonPicker
                      language={this.props.t('mapLocale')}
                      setCoordinates={(value) => {
                      this.setState({wkt: value});
                    }} coordinate={this.state.editData ? wkt.parse(this.state.editData.wkt).coordinates[0] : []}
                    initialCenter={this.state.wareHouse ? wkt.parse(this.state.wareHouse.wkt).coordinates[0][0] : []}/>
                  </div>
                </div>
              </Dialog>
              }
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_dock')} icon="pi pi-cog"
                            onClick={() => this.setState({
                                display: true,
                                name: '',
                                startingTime: '',
                                endingTime: '',
                                selectedProducts: [],
                                editData: null
                            })}/>
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
                        }}>{this.props.t('no_dock')}</h2>
                    </div>
                </div>
            );
        }
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('docks')}</h1>
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
                                    <Column body={(row) => {
                                        return moment(row.startingTime).local().format('hh:mm A')
                                    }} header={this.props.t('start_time')}/>
                                    <Column body={(row) => {
                                        return moment(row.endingTime).local().format('hh:mm A');
                                    }} header={this.props.t('end_time')}/>
                                    <Column header={this.props.t('admins')} body={this.viewAdmins}
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
export default withTranslation()(Docks)
