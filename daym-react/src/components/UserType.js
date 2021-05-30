import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import { withTranslation } from 'react-i18next';
import {UserGroupService} from "../service/UserGroupService";
import {Dropdown} from "primereact/dropdown";
import {ProgressSpinner} from "primereact/progressspinner";

class UserType extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
            role: 2,
            methods: [],
            roles: [
                {
                    label: 'Admin',
                    value: 2
                },
                {
                    label: 'Employee',
                    value: 3
                }
            ]
        };
        this.userGroupService = new UserGroupService();
        this.handleCreateUserGroup = this.handleCreateUserGroup.bind(this);
        this.handleDeleteUserGroup = this.handleDeleteUserGroup.bind(this);
        this.addPermission = this.addPermission.bind(this);
        this.addFields = this.addFields.bind(this);
        this.viewUsers = this.viewUsers.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
        this.validate = this.validate.bind(this);
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('patch') >= 0 && <Button type="button" icon="pi pi-pencil" className="p-button-warning" onClick={() => {
                    this.setState({display: true, name: rowData.name, description: rowData.description, role: rowData.role, editData: rowData});
                }}/>
            }
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" disabled={this.state.deleteLoading} className="p-button-danger"
                        style={{marginLeft: '.5em'}} onClick={() => {
                    this.handleDeleteUserGroup(rowData);
                }}/>
            }
        </div>;
    }

    viewUsers(rowData, column) {
        return <div style={{padding: '0px 8px'}}>
            <Button label={this.props.t('view_user')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/users/'+rowData._id;
            }}/>
        </div>;
    }

    addPermission(rowData, column) {
        return <div>
            <Button label={this.props.t('view_permissions')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/user-permission/'+rowData._id;
            }}/>
        </div>;
    }

    addFields(rowData, column) {
        return <div>
            <Button label={this.props.t('view_fields')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/user-field/'+rowData._id;
            }}/>
        </div>;
    }

    handleDeleteUserGroup(rowData){
        this.setState({deleteLoading: true});
        this.userGroupService.deleteUserGroup(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: 'User Type deleted Successfully!' });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false
            })
        }).catch((err)=>{
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: 'Can not delete User Type' });
            this.setState({deleteLoading: false});
        });
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/user-type').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/user-type')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        this.userGroupService.getAllUserGroup().then(response => {
            this.setState({dataTableValue: response.data, loading: false})
        });
    }

    validate(){
        if(!this.state.name){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name_cannot_be_blank') });
            return false;
        }
        return true;
    }
    handleCreateUserGroup(){
        if(this.validate())
            this.setState({buttonLoading: true});
            if(this.state.editData){
                this.userGroupService.editUserGroup(this.state.editData._id,this.state.name,this.state.description,this.state.role).then(async (response)=>{
                    this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: 'User Type edited Successfully!' });
                    let _dataTable = this.state.dataTableValue;
                    const position = this.state.dataTableValue.indexOf(this.state.editData);
                    _dataTable[position] = response.data;
                    this.setState({
                        dataTableValue: _dataTable,
                        display:false,
                        buttonLoading: false,
                    })
                }).catch((err)=>{
                    this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: 'Can not edit User Type' });
                    this.setState({buttonLoading: false});
                });
            }else {
                this.userGroupService.createUserGroup(this.state.name, this.state.description, this.state.role).then(async (response) => {
                    this.growl.show({severity: 'success', summary: this.props.t('success'), detail: 'User Type created Successfully!'});
                    this.setState({
                        dataTableValue: [response.data, ...this.state.dataTableValue],
                        display: false,
                        buttonLoading: false
                    })
                }).catch((err) => {
                    this.growl.show({severity: 'error', summary: this.props.t('error'), detail: 'Can not create User Type'});
                    this.setState({buttonLoading: false});
                });
            }
    }

    render() {
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false, name: '', description: '', role: 2, editData: null})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateUserGroup} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        const tableHeader = (
            <div style={{display: 'flex',justifyContent: 'flex-end'}}>
                <Dialog header={(this.state.editData ? this.props.t('edit'): this.props.t('add'))+' '+this.props.t('user_type')} visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter}
                        onHide={() => this.setState({display:false, name: '', description: '', role: 2, editData: null})}>
                    <div className="p-grid">
                        <div className="p-col-12 p-md-12">
                            <InputText placeholder={this.props.t('name')} style={{width: '100%'}} value={this.state.name} onChange={(e) => this.setState({name: e.target.value})} />
                        </div>
                        <div className="p-col-12 p-md-12">
                            <InputText placeholder={this.props.t('description')} style={{width: '100%'}} value={this.state.description} onChange={(e) => this.setState({description: e.target.value})} />
                        </div>
                        <div className="p-col-12 p-md-12">
                            <Dropdown style={{width: '100%'}} placeholder={this.props.t('role')}
                                      value={this.state.role}
                                      options={this.state.roles}
                                      onChange={(event)=>{this.setState({role: event.value});}}/>
                        </div>
                    </div>
                </Dialog>
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_userGroup')} icon="pi pi-cog" onClick={() => this.setState({display: true, hqName: '', hqMetaName: '', editData: null})}/>
                }
            </div>
        );
        let actionHeader = <Button type="button" icon="pi pi-cog"/>;

        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('user_type')}</h1>
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
                                    <Column field="description" header={this.props.t('description')} sortable={true}/>
                                    <Column header={this.props.t('fields')} body={this.addFields}
                                            style={{textAlign: 'center'}}/>
                                    <Column header={this.props.t('permissions')} body={this.addPermission}
                                            style={{textAlign: 'center'}}/>
                                    <Column header={this.props.t('user')} body={this.viewUsers}
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

export default withTranslation()(UserType)
