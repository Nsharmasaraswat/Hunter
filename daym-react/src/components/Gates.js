import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import {GateService} from "../service/GateService";
import {withTranslation} from "react-i18next";
import {ProgressSpinner} from "primereact/progressspinner";
import GoogleMapPolygonPicker from "./GoogleMapPolygonPicker";
import wkt from "wkt";
import {Dropdown} from "primereact/dropdown";
import {HeadQuarterService} from "../service/HeadQuarterService";

class Gates extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            methods: [],
            types: [],
            locale: '',
            type: 1,
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
        };
        this.gatesService = new GateService();
        this.hqService = new HeadQuarterService();
        this.handleCreateGate = this.handleCreateGate.bind(this);
        this.handleDeleteGate = this.handleDeleteGate.bind(this);
        this.viewSecurities = this.viewSecurities.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" disabled={this.state.deleteLoading} className="p-button-danger"
                        style={{marginRight: '.5em'}} onClick={() => {
                    this.handleDeleteGate(rowData);
                }}/>
            }
            {
                this.state.methods.indexOf('patch') >= 0 &&
                <Button type="button" icon="pi pi-pencil" className="p-button-warning" onClick={() => {
                    this.setState({
                        display: true,
                        name: rowData.name,
                        wkt: rowData.wkt,
                        cameraId: rowData.cameraId,
                        type: rowData.gateType,
                        editData: rowData
                    });
                }}/>
            }
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/gate').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/gate')[0].methods});
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
          this.gatesService.getAllGates(locationId||'').then(response => {
          //console.log(response.data);
            this.setState({dataTableValue: response.data, loading: false})
          }).catch((err)=>{

          });
        }
      );

    }
    componentDidUpdate(prevProps, prevState, snapshot) {
        if(this.state.locale !== localStorage.getItem('i18nextLng')){
            this.setState({types: [
                    {
                        label: this.props.t('entrance'),
                        value: 1
                    },
                    {
                        label: this.props.t('exit'),
                        value: 2
                    }
                ], locale: localStorage.getItem('i18nextLng')})
        }
    }
    validate(){
        if(!this.state.name){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        if(!this.state.cameraId){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('camera_id') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        if(!this.state.wkt){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('location') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        return true;
    }

    handleCreateGate(){
        if(this.validate()) {
            this.setState({buttonLoading: true});
            if (this.state.editData) {
                let _data = {name: this.state.name,cameraId: this.state.cameraId,wkt: this.state.wkt,gateType: this.state.type};
                this.gatesService.editGate(this.state.editData._id, _data).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('gate') + ' ' + this.props.t('edited_Successfully')
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
                        detail: error || this.props.t('can_not_edit') + ' ' + this.props.t('gate')
                    });
                    this.setState({buttonLoading: false});
                });
            } else {
                this.gatesService.createGate(this.state.name,this.state.cameraId, this.state.locationId, this.state.wkt, this.state.type).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('gate') + ' ' + this.props.t('created_Successfully')
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
                        detail: error || this.props.t('can_not_create') + ' ' + this.props.t('gate')
                    });
                    this.setState({buttonLoading: false});
                });
            }
        }


    }
    handleDeleteGate(rowData){
        this.setState({deleteLoading: true});
        this.gatesService.deleteGate(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('gate') + ' ' + this.props.t('deleted_Successfully') });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false
            })
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_delete') + ' ' + this.props.t('gate') });
            this.setState({deleteLoading: false});
        });
    }
    viewSecurities(rowData, column) {
        return <div>
            <Button label={this.props.t('view_security')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/manage-security/'+rowData._id;
            }}/>
        </div>;
    }

    render() {
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateGate} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        const tableHeader = (
            <div style={{display: 'flex',justifyContent: 'flex-end'}}>
              {this.state.hq &&
              <Dialog
                header={(this.state.editData ? this.props.t('edit') : this.props.t('add')) + ' ' + this.props.t('gates')}
                visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter}
                onHide={() => this.setState({display: false})}>
                <div className="p-grid">
                  <div className="p-col-12 p-md-12">
                    <InputText placeholder={this.props.t('name')} style={{width: '100%'}} value={this.state.name}
                               onChange={(e) => this.setState({name: e.target.value})}/>
                  </div>
                  <div className="p-col-12 p-md-12">
                    <InputText placeholder={this.props.t('camera_id')} style={{width: '100%'}}
                               value={this.state.cameraId} onChange={(e) => this.setState({cameraId: e.target.value})}/>
                  </div>
                  <div className="p-col-12 p-md-12">
                    <Dropdown
                      style={{width: '100%'}}
                      placeholder={this.props.t('type')}
                      value={this.state.type}
                      options={this.state.types}
                      onChange={(event) => {
                        this.setState({type: event.value});
                      }}/>
                  </div>
                  <div className="p-col-12 p-md-12">
                    <GoogleMapPolygonPicker
                      language={this.props.t('mapLocale')}
                      initialCenter={wkt.parse(this.state.hq.wkt).coordinates}
                      setCoordinates={(value) => {
                      this.setState({wkt: value});
                    }} coordinate={this.state.editData ? wkt.parse(this.state.editData.wkt).coordinates[0] : []}/>
                  </div>
                </div>
              </Dialog>
              }
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_gate')} icon="pi pi-cog"
                            onClick={() => this.setState({display: true, name: '', cameraId: '', type: 1, editData: null})}/>
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
                        }}>{this.props.t('no_gates')}</h2>
                    </div>
                </div>
            );
        }
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('gates')}</h1>
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
                                    <Column field="cameraId" header={this.props.t('camera_id')} sortable={true}/>
                                    <Column body={(row) => {
                                        if (row.occupiedStatus === 1)
                                            return this.props.t('not_occupied');
                                        else
                                            return this.props.t('occupied');
                                    }} header={this.props.t('status')} />
                                    <Column body={(row) => {
                                        if (row.gateType === 1)
                                            return this.props.t('entrance');
                                        else
                                            return this.props.t('exit');
                                    }} header={this.props.t('type')} />
                                    <Column header={this.props.t('securities')} body={this.viewSecurities}
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
export default withTranslation()(Gates)
