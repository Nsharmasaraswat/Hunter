import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {HeadQuarterService} from "../service/HeadQuarterService";
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import { withTranslation } from 'react-i18next';
import {RadioButton} from "primereact/radiobutton";
import {ProgressSpinner} from "primereact/progressspinner";
import GoogleMapPicker from "./GoogleMapPicker";
import wkt from "wkt";

class HeadQuaters extends Component {

    constructor() {
        super();
        this.state = {
            radioValue: '1',
            dataTableValue:[],
            methods: [],
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
        };
        this.headquaterService = new HeadQuarterService();
        this.handleCreateHeadQuarter = this.handleCreateHeadQuarter.bind(this);
        this.handleDeleteHeadQuarter = this.handleDeleteHeadQuarter.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
        this.viewWareHouse = this.viewWareHouse.bind(this);
        this.viewGate = this.viewGate.bind(this);
        this.validate = this.validate.bind(this);
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" disabled={this.state.deleteLoading} className="p-button-danger"
                        style={{marginRight: '.5em'}} onClick={() => {
                    this.handleDeleteHeadQuarter(rowData);
                }}/>
            }
            {
                this.state.methods.indexOf('patch') >= 0 &&
                <Button type="button" icon="pi pi-pencil" className="p-button-warning" onClick={() => {
                    this.setState({
                        display: true,
                        hqName: rowData.name,
                        radioValue: JSON.stringify(rowData.type),
                        editData: rowData
                    });
                }}/>
            }
        </div>;
    }

    viewGate(rowData, column) {
        return <div style={{padding: '0px 8px'}}>
            <Button label={this.props.t('view_gates')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/gate/'+rowData._id;
            }}/>
        </div>;
    }
    viewWareHouse(rowData, column) {
        return <div>
            <Button label={this.props.t('view_wareHouses')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/warehouse/'+rowData._id;
            }}/>
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/locations').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/locations')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        this.headquaterService.getAllHeadquaters().then(response => {
            this.setState({dataTableValue: response.data, loading: false})
        });
    }

    validate(){
        if(!this.state.hqName){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        return true;
    }

    handleCreateHeadQuarter(){
        if(this.validate()) {
            this.setState({buttonLoading: true});
            if(this.state.editData){
                let _data = {name: this.state.hqName, type: this.state.radioValue, wkt: wkt.stringify(this.state.coordinates)};
                this.headquaterService.editHeadQuarter(this.state.editData._id, _data).then(async (response)=>{
                    this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('location') + ' ' + this.props.t('edited_Successfully') });
                    let _dataTable = this.state.dataTableValue;
                    const position = this.state.dataTableValue.indexOf(this.state.editData);
                    _dataTable[position] = response.data;
                    this.setState({
                        dataTableValue: _dataTable,
                        display:false,
                        buttonLoading: false,
                    })
                }).catch((err)=>{
                    let error = err.response.data.message;
                    this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_edit') + ' ' + this.props.t('location') });
                    this.setState({buttonLoading: false});
                });
            }else {
                this.headquaterService.createHeadQuarter(this.state.hqName, this.state.radioValue, wkt.stringify(this.state.coordinates)).then(async (response)=>{
                    this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('location') + ' ' + this.props.t('created_Successfully') });
                    this.setState({
                        dataTableValue: [response.data,...this.state.dataTableValue],
                        display:false,
                        buttonLoading: false,
                    })
                }).catch((err)=>{
                    let error = err.response.data.message;
                    this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_create') + ' ' + this.props.t('location') });
                    this.setState({buttonLoading: false});
                });
            }
        }
    }

    handleDeleteHeadQuarter(rowData){
        this.setState({deleteLoading: true});
        this.headquaterService.deleteHeadQuarter(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('location') + ' ' + this.props.t('deleted_Successfully') });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false,
            })
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_delete') + ' ' + this.props.t('location') });
            this.setState({deleteLoading: false});
        });
    }

    render() {
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateHeadQuarter} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        const tableHeader = (
            <div style={{display: 'flex',justifyContent: 'flex-end'}}>
                <Dialog header={(this.state.editData ? this.props.t('edit'): this.props.t('add'))+' '+this.props.t('location')} visible={this.state.display} modal={true} style={{width: '60vw'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid">
                        <div className="p-col-12 p-md-6 p-grid">
                            <div className="p-col-12 p-md-12">
                                <InputText placeholder={this.props.t('name')} style={{width: '100%'}} value={this.state.hqName} onChange={(e) => this.setState({hqName: e.target.value})} />
                                <div className="p-fluid">
                                    <p>{this.props.t('type')}</p>
                                    <div className="p-grid">
                                        <div className="p-col-12 p-md-6">
                                            <RadioButton value="1" inputId="rb1" onChange={event => this.setState({radioValue: event.value})} checked={this.state.radioValue === "1"}/>
                                            <label htmlFor="rb1" className="p-radiobutton-label">{this.props.t('headquarter')}</label>
                                        </div>
                                        <div className="p-col-12 p-md-6">
                                            <RadioButton value="2" inputId="rb2" onChange={event => this.setState({radioValue: event.value})} checked={this.state.radioValue === "2"}/>
                                            <label htmlFor="rb2" className="p-radiobutton-label">{this.props.t('distribution_center')}</label>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        </div>
                        <div className="p-col-12 p-md-6 p-grid">
                            <GoogleMapPicker
                              setCoordinates={(value)=>this.setState({coordinates: value})}
                              coordinate={this.state.editData ? wkt.parse(this.state.editData.wkt).coordinates : [-5.266008, -37.048647]}
                              language={this.props.t('mapLocale')}

                            />
                        </div>
                    </div>
                </Dialog>
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_location')} icon="pi pi-cog"
                            onClick={() => this.setState({display: true, hqName: '', hqMetaName: '', editData: null})}/>
                }
            </div>
        );
        let actionHeader = <Button type="button" icon="pi pi-cog"/>;
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('all_locations')}</h1>
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
                                        if (row.type === 1)
                                            return this.props.t('headquarter');
                                        else
                                            return this.props.t('distribution_center');
                                    }} header={this.props.t('type')} sortable={true}/>
                                    <Column header={this.props.t('wareHouses')} body={this.viewWareHouse}
                                            style={{textAlign: 'center'}}/>
                                    <Column header={this.props.t('gates')} body={this.viewGate}
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

export default withTranslation()(HeadQuaters)
