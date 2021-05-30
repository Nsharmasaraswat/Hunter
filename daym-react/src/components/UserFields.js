import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import { withTranslation } from 'react-i18next';
import {UserFieldService} from "../service/UserFieldService";
import {RadioButton} from "primereact/radiobutton";
import {Chips} from "primereact/chips";
import {ProgressSpinner} from "primereact/progressspinner";
import {Checkbox} from "primereact/checkbox";

class UserFields extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            radioValue: 'String',
            chipsValue: [],
            methods: [],
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
            required: false,
        };
        this.userFieldService = new UserFieldService();
        this.handleCreateUserField = this.handleCreateUserField.bind(this);
        this.handleDeleteUserField = this.handleDeleteUserField.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
        this.validate = this.validate.bind(this);
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/user-field').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/user-field')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        const userTypeId = this.props.location.pathname.split('/')[2];
        this.setState({userTypeId});
        this.userFieldService.getAllFields(userTypeId).then(response => {
            this.setState({dataTableValue: response.data, loading: false})
        });
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('patch') >= 0 &&
                <Button type="button" icon="pi pi-pencil" className="p-button-warning" onClick={() => {
                    this.setState({
                        display: true,
                        name: rowData.name,
                        radioValue: rowData.type,
                        chipsValue: rowData.values.map((each) => each.label),
                        required: rowData.required,
                        editData: rowData
                    });
                }}/>
            }
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" disabled={this.state.deleteLoading} className="p-button-danger"
                        style={{marginLeft: '.5em'}} onClick={() => {
                    this.handleDeleteUserField(rowData);
                }}/>
            }
        </div>;
    }

    handleDeleteUserField(rowData){
        this.setState({deleteLoading: true});
        this.userFieldService.deleteUserField(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: 'User Type deleted Successfully!' });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false,
            })
        }).catch((err)=>{
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: 'Can not delete User Type' });
            this.setState({deleteLoading: false});
        });

    }
    validate(){
        if(!this.state.name){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name_cannot_be_blank') });
            return false;
        }else if(this.state.radioValue === 'Select' && this.state.chipsValue.length === 0){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('drop_down_value_is_required') });
            return false;
        }
        return true;
    }
    handleCreateUserField(){
        if(this.validate())
            this.setState({buttonLoading: true});
            if(this.state.editData){
                this.userFieldService.editUserField(this.state.editData._id,this.state.name,this.state.radioValue,this.state.chipsValue,this.state.userTypeId,this.state.required).then(async (response)=>{
                    this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: 'User field edited Successfully!' });
                    let _dataTable = this.state.dataTableValue;
                    const position = this.state.dataTableValue.indexOf(this.state.editData);
                    _dataTable[position] = response.data;
                    this.setState({
                        dataTableValue: _dataTable,
                        display:false,
                        buttonLoading: false,
                    })
                }).catch((err)=>{
                    this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: 'Can not edit User field' });
                    this.setState({buttonLoading: false});
                });
            }else {
                this.userFieldService.createUserField(this.state.name,this.state.radioValue,this.state.chipsValue,this.state.userTypeId,this.state.required).then(async (response) => {
                    this.growl.show({severity: 'success', summary: this.props.t('success'), detail: 'User field created Successfully!'});
                    this.setState({
                        dataTableValue: [response.data, ...this.state.dataTableValue],
                        display: false,
                        buttonLoading: false,
                    })
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({severity: 'error', summary: this.props.t('error'), detail: error || 'Can not create User field'});
                    this.setState({buttonLoading: false});
                });
            }
    }

    render() {
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateUserField} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        const tableHeader = (
            <div style={{display: 'flex',justifyContent: 'flex-end'}}>
                <Dialog header={(this.state.editData ? this.props.t('edit'): this.props.t('add'))+' '+this.props.t('fields')} visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid">
                        <div className="p-col-12 p-md-12">
                            <InputText placeholder={this.props.t('name')} style={{width: '100%'}} value={this.state.name} onChange={(e) => this.setState({name: e.target.value})} />
                        </div>
                        <div className="p-col-12 p-md-12">
                            <p>{this.props.t('field_type')}</p>
                            <div className="p-grid">
                                <div className="p-col-12 p-md-4">
                                    <RadioButton value="String" inputId="rb1" onChange={event => this.setState({radioValue: event.value})} checked={this.state.radioValue === "String"}/>
                                    <label htmlFor="rb1" className="p-radiobutton-label">{this.props.t('text')}</label>
                                </div>
                                <div className="p-col-12 p-md-4">
                                    <RadioButton value="Number" inputId="rb2" onChange={event => this.setState({radioValue: event.value})} checked={this.state.radioValue === "Number"}/>
                                    <label htmlFor="rb2" className="p-radiobutton-label">{this.props.t('number')}</label>
                                </div>
                                <div className="p-col-12 p-md-4">
                                    <RadioButton value="Select" inputId="rb3" onChange={event => this.setState({radioValue: event.value})} checked={this.state.radioValue === "Select"}/>
                                    <label htmlFor="rb3" className="p-radiobutton-label">{this.props.t('dropdown')}</label>
                                </div>
                                <div className="p-col-12 p-md-4" style={{marginTop: 5}}>
                                    <Checkbox value="get" inputId="cb2"
                                              onChange={(event) => this.setState({required: event.checked})}
                                              checked={this.state.required}/>
                                    <label htmlFor="cb2" className="p-checkbox-label">{this.props.t('required')}</label>
                                </div>
                            </div>
                        </div>
                        {
                            this.state.radioValue === 'Select' && <div className="p-col-12 p-md-12">
                                <p>{this.props.t('dropdown_options')}</p>
                                <Chips
                                    value={this.state.chipsValue}
                                    placeholder={this.props.t('type_enter')}
                                    onChange={(e) => {
                                        let value = e.value;
                                        this.setState({chipsValue:value});
                                    }}/>
                            </div>
                        }

                    </div>
                </Dialog>
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_field')} icon="pi pi-cog" onClick={() =>
                        this.setState({display: true, name: '', radioValue: 'String', chipsValue: [],required: false, editData: null})
                    }/>
                }
            </div>
        );
        let actionHeader = <Button type="button" icon="pi pi-cog"/>;

        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('fields')}</h1>
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
                                    <Column field="type" header={this.props.t('type')} sortable={true}/>
                                    <Column header={this.props.t('required')} body={(rowData,column)=>{
                                        return rowData.required ? this.props.t('mandatory') : this.props.t('optional');
                                    }}/>
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

export default withTranslation()(UserFields)
