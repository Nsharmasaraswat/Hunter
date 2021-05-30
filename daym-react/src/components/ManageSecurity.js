import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import {withTranslation} from "react-i18next";
import "./Table.css";
import {UsersService} from "../service/UsersService";
import {UserFieldService} from "../service/UserFieldService";
import {Dropdown} from "primereact/dropdown";
import {ProgressSpinner} from "primereact/progressspinner";

class ManageSecurity extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            userValues: [],
            userFields: [],
            methods: [],
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
        };
        this.userService = new UsersService();
        this.userFieldService = new UserFieldService();
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
                    this.setState({
                        display: true,
                        name: rowData.name,
                        password: rowData.password,
                        userName: rowData.userName,
                        userValues: rowData.fields.map((each) => {
                            return ({
                                "userField": each.userField._id,
                                "value": each.value,
                                "required": each.required
                            });
                        }),
                        editData: rowData
                    });
                }}/>
            }
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/manage-security').length > 0;
    }


    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/manage-security')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        const locationId = this.props.location.pathname.split('/')[2];
        this.setState({locationId});
        this.userService.getSecurityType().then(response =>{
            const userTypeId = response.data[0]._id;
            this.setState({userTypeId});
            this.userFieldService.getAllFields(userTypeId).then(response => {
                let _fields = response.data.map((each)=>{
                    return({
                        "userField": each._id,
                        "value": "",
                        "required": each.required
                    });
                });
                this.setState({userValues: _fields, userFields: response.data})
            }).catch((err)=>{
                let error = err.response.data.message;
                this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('some_error')});
            });
            this.userService.getAllSecurityGuards(userTypeId, locationId).then(res => {
              //console.log('Dataaaaa---->',res);
                this.setState({dataTableValue: res.data, loading: false});
            }).catch((err)=>{
                let error = err.response || err.response.data.message;
                this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('some_error')});
            });
        }).catch((err)=>{

        });

    }

    validate(){
        let _emptyFields = this.state.userValues.filter(each => each.required && !each.value);

        if(!this.state.name){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }else if(!this.state.userName){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('userName') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }else if(_emptyFields.length > 0){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('all_fields_mandatory') + ' ! ' + this.props.t('can_not_be_empty') });
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
                    password: this.state.password,
                    userName: this.state.userName,
                    userType: this.state.userTypeId,
                    fields: this.state.userValues,
                };
                this.userService.editUser(this.state.editData._id, _body).then(async (response) => {
                    let _dataTable = this.state.dataTableValue;
                    const position = this.state.dataTableValue.indexOf(this.state.editData);
                  //console.log('Position',position);
                    _dataTable[position] = response.data;
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('security') + ' ' + this.props.t('edited_Successfully')
                    });
                    this.setState({
                        dataTableValue: _dataTable,
                        display: false,
                        buttonLoading: false,
                        name: '',
                        password: '',
                        userName: '',
                        userValues: this.state.userValues.map((each)=>{
                            return({
                                "userField": each.userField,
                                "value": "",
                                "required": each.required
                            });
                        }),
                    });
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_edit') + ' ' + this.props.t('security') });
                    this.setState({buttonLoading: false});
                });
            } else {
                const _body = {
                    name: this.state.name,
                    password: this.state.password,
                    userName: this.state.userName,
                    userType: this.state.userTypeId,
                    fields: this.state.userValues,
                    gate: this.state.locationId
                };
              //console.log(_body);
                this.userService.createUser(_body).then(async (response) => {
                  //console.log('Resssss---->', response.data);
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('security') + ' ' + this.props.t('created_Successfully')
                    });
                    this.setState({
                        dataTableValue: [response.data, ...this.state.dataTableValue],
                        display: false,
                        buttonLoading: false,
                        name: '',
                        password: '',
                        userName: '',
                        userValues: this.state.userValues.map((each)=>{
                            return({
                                "userField": each.userField,
                                "value": "",
                                "required": each.required
                            });
                        }),
                    });
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_create') + ' ' + this.props.t('security') });
                    this.setState({buttonLoading: false});
                });
            }
        }
    }
    handleDeleteWarehouse(rowData){
        this.setState({deleteLoading: true});
        this.userService.deleteUser(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({
                severity: 'success',
                summary: this.props.t('success'),
                detail: this.props.t('security') + ' ' + this.props.t('deleted_Successfully')
            });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false
            });
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_delete') + ' ' + this.props.t('security') });
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
                <Dialog header={(this.state.editData ? this.props.t('edit'): this.props.t('add'))+' '+this.props.t('security')} visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid">
                        <div className="p-grid p-col-12 p-md-12">
                            <div className="p-col-12 p-md-12">
                                <InputText placeholder={this.props.t('name')} style={{width: '100%'}} value={this.state.name} onChange={(e) => this.setState({name: e.target.value})} />
                            </div>
                            <div className="p-col-12 p-md-12">
                                <InputText placeholder={this.props.t('userName')} style={{width: '100%'}} value={this.state.userName} onChange={(e) => this.setState({userName: e.target.value})} />
                            </div>
                            {
                                !this.state.editData && <div className="p-col-12 p-md-12">
                                    <InputText placeholder={this.props.t('password')} style={{width: '100%'}} value={this.state.password} onChange={(e) => this.setState({password: e.target.value})} />
                                </div>
                            }
                            {
                                this.state.userFields.map((each,index)=>
                                    <div className="p-col-12 p-md-12" key={index}>
                                        {
                                            each.type === 'Select' ?
                                                <Dropdown style={{width: '100%'}} placeholder={each.name}
                                                          value={this.state.userValues[index].value}
                                                          options={each.values}
                                                          onChange={(event)=>{
                                                              let _values = this.state.userValues;
                                                              _values[index].value = event.value;
                                                              this.setState({userValues: _values});
                                                          }}/>

                                                :
                                                <InputText placeholder={each.name} style={{width: '100%'}} type={each.type} value={this.state.userValues[index].value} onChange={(e) => {
                                                    let _values = this.state.userValues;
                                                    _values[index].value = e.target.value;
                                                    this.setState({userValues: _values});
                                                }} />
                                        }
                                    </div>
                                )
                            }
                        </div>
                    </div>
                </Dialog>
                <Button
                    type="button"
                    label={this.props.t('view_fields')}
                    icon="pi pi-cog"
                    style={{marginRight: 8}}
                    onClick={()=>{
                        window.location = '#/user-field/'+this.state.userTypeId;
                    }}
                />
                <Button
                    type="button"
                    label={this.props.t('add_permission')}
                    icon="pi pi-cog"
                    style={{marginRight: 8}}
                    onClick={() => {
                        if(this.state.userTypeId){
                            window.location = '#/user-permission/'+this.state.userTypeId;
                        }
                    }}/>
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_security')} icon="pi pi-cog"
                            onClick={() => this.setState({
                                display: true,
                                name: '',
                                password: '',
                                userName: '',
                                userValues: this.state.userValues.map((each) => {
                                    return ({
                                        "userField": each.userField,
                                        "value": "",
                                        "required": each.required
                                    });
                                }),
                                editData: null
                            })}/>
                }
            </div>
        );

        const viewDetails = (rowData) => <div>
            <Button label={this.props.t('view') + ' ' + this.props.t('security_details')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                this.setState({detailsShow: true, selectedFields: rowData.fields})
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
                        }}>{this.props.t('no_warehouse')}</h2>
                    </div>
                </div>
            );
        }
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('securities')}</h1>
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
                                    <Column field="userName" header={this.props.t('userName')} sortable={true}/>
                                    <Column header={this.props.t('security_details')}
                                            body={(rowData) => viewDetails(rowData)} style={{textAlign: 'center'}}/>
                                    <Column header={actionHeader} body={this.actionButtons}
                                            style={{textAlign: 'center', width: '8em'}}/>
                                </DataTable>
                        }
                    </div>
                </div>
                <Dialog header={this.props.t('security_details')} visible={this.state.detailsShow} modal={true} style={{width: '40vw'}} footer={
                    <div>
                        <Button icon="pi pi-times" onClick={() => this.setState({detailsShow:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                    </div>
                } onHide={() => this.setState({detailsShow:false, selectedUnits: [], selectedFields: []})}>
                    <div className="p-grid">
                        <div className="p-grid p-col-12 p-md-12">
                            <DataTable ref={(el) => this.dt = el} value={this.state.selectedFields} header={this.props.t('security_fields')} selectionMode="single" paginator={true} rows={7}
                                       responsive={true} selection={this.state.selectedFields1} onSelectionChange={event => this.setState({selectedFields1: event.value})}>
                                <Column body={(row)=>row.userField.name} header={this.props.t('user_field')} style={{textAlign:'center'}}/>
                                <Column field="value" header={this.props.t('value')} sortable={true} />
                            </DataTable>
                        </div>
                    </div>
                </Dialog>
            </div>
        );
    }
}

export default withTranslation()(ManageSecurity)
