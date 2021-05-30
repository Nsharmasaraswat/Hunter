import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {Growl} from "primereact/growl";
import { withTranslation } from 'react-i18next';
import {Checkbox} from "primereact/checkbox";
import {UserPermissionService} from "../service/UserPermissionService";
import {Dropdown} from "primereact/dropdown";
import {ProgressSpinner} from "primereact/progressspinner";

class UserPermission extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            radioValue: 'String',
            locale: localStorage.getItem('i18nextLng'),
            chipsValue: [],
            checkboxValue: [],
            allPermissions: [],
            dropDownData: [],
            methods: [],
            loading: true,
            buttonLoading: false,
        };
        this.userPermisssionService = new UserPermissionService();
        this.handleSaveUserPermission = this.handleSaveUserPermission.bind(this);
        this.handleCreateUserField = this.handleCreateUserField.bind(this);
        this.handleDeleteUserField = this.handleDeleteUserField.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
        this.validate = this.validate.bind(this);
        this.onCheckboxChange = this.onCheckboxChange.bind(this);
    }

    onCheckboxChange(event, row){
        let _dataTable = this.state.dataTableValue;
        let index = this.state.dataTableValue.indexOf(row);
        let _values = row.methods;
        if (event.checked)
            _values.push(event.value);
        else
            _values.splice(_values.indexOf(event.value), 1);
        _dataTable[index].methods = _values;
        this.setState({dataTableValue: _dataTable});
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/user-permission').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/user-permission')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        const locales = {
            sp : 'spanish',
            pr : 'portuguese',
            en : 'name'
        };
        const userTypeId = this.props.location.pathname.split('/')[2];
        this.setState({userTypeId});
        this.userPermisssionService.getAlPermissions().then(response => {
            let _data = response.data;
            let dropDownData = _data.map(each=>{
                delete each._id;
                return {
                    value: JSON.stringify(each),
                    label: each[locales[this.state.locale]]
                }
            })
            let allPermissions = _data.map(each=>{
                delete each._id;
                return each;
            })
            this.setState({allPermissions, dropDownData});
        });
        this.userPermisssionService.getAllUserPermissions(userTypeId).then(response => {
            this.setState({userPermission: response.data[0], dataTableValue: response.data[0].paths, loading: false});
        });
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if(this.state.locale !== localStorage.getItem('i18nextLng')){
            let _data = this.state.allPermissions;
            const locales = {
                sp : 'spanish',
                pr : 'portuguese',
                en : 'name'
            };
            let dropDownData = _data.map(each=>{
                return {
                    value: JSON.stringify(each),
                    label: each[locales[localStorage.getItem('i18nextLng')]]
                }
            });
            this.setState({dropDownData, locale: localStorage.getItem('i18nextLng')})
        }
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" className="p-button-danger" onClick={() => {
                    this.handleDeleteUserField(rowData);
                }}/>
            }
        </div>;
    }

    handleDeleteUserField(rowData){
        let _dataTable = this.state.dataTableValue;
        const position = this.state.dataTableValue.indexOf(rowData);
        _dataTable.splice(position,1);
    }

    validate(){
        let selected = JSON.parse(this.state.selectedPermission);
        const isThere = this.state.dataTableValue.filter((e)=>e.name===selected.name);
        if(isThere.length > 0){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('permission_added') });
            return false;
        }
        return true;
    }

    handleCreateUserField(){
        if(this.validate()){
            this.setState({buttonLoading: true});
            let _data = this.state.dataTableValue;
            _data.push(JSON.parse(this.state.selectedPermission));
            this.setState({
                selectedPermission: null,
                dataTableValue: _data,
                display: false,
                buttonLoading: false,
            })
        }
    }

    handleSaveUserPermission(){
        if(this.state.dataTableValue.length > 0)
            this.userPermisssionService.addUserPermissions(this.state.userPermission._id, this.state.dataTableValue).then(async (response) => {
                this.growl.show({severity: 'success', summary: this.props.t('success'), detail: 'Permission updated Successfully!'});
            }).catch((err) => {
                let error = err.response.data.message;
                this.growl.show({severity: 'error', summary: this.props.t('error'), detail: error || 'Can not update Permission'});
            });

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
                <Dialog header={ this.props.t('add')+' '+this.props.t('permissions')} visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid p-fluid">
                        <div className="p-col-12 p-md-12">
                            <div >
                                <p>{this.props.t('select_permission')}</p>
                                <Dropdown options={this.state.dropDownData} value={this.state.selectedPermission} onChange={event => {
                                  //console.log('Eadch---->',JSON.parse(event.value));
                                    this.setState({selectedPermission: event.value})
                                }} autoWidth={false} />
                            </div>
                        </div>
                    </div>
                </Dialog>
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('permission_add')} icon="pi pi-cog" onClick={() =>
                        this.setState({display: true})
                    }/>
                }
                {
                    this.state.methods.indexOf('patch') >= 0 &&
                    <Button type="button" label={this.props.t('save')} style={{marginLeft: 10}} icon="pi pi-cog"
                            onClick={this.handleSaveUserPermission}/>
                }
            </div>
        );
        let actionHeader = <Button type="button" icon="pi pi-cog"/>;

        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('permissions')}</h1>
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
                                    <Column body={row => {
                                        return (
                                            <Checkbox value="get" inputId="cb1"
                                                      onChange={(event) => this.onCheckboxChange(event, row)}
                                                      checked={row.methods.indexOf('get') > -1}/>
                                        );
                                    }} header={this.props.t('view')}/>
                                    <Column body={row => {
                                        return (
                                            <Checkbox value="find" inputId="cb2"
                                                      onChange={(event) => this.onCheckboxChange(event, row)}
                                                      checked={row.methods.indexOf('find') > -1}/>
                                        );
                                    }} header={this.props.t('list')}/>
                                    <Column body={row => {
                                        return (
                                            <Checkbox value="create" inputId="cb3"
                                                      onChange={(event) => this.onCheckboxChange(event, row)}
                                                      checked={row.methods.indexOf('create') > -1}/>
                                        );
                                    }} header={this.props.t('add')}/>
                                    <Column body={row => {
                                        return (
                                            <Checkbox value="patch" inputId="cb3"
                                                      onChange={(event) => this.onCheckboxChange(event, row)}
                                                      checked={row.methods.indexOf('patch') > -1}/>
                                        );
                                    }} header={this.props.t('edit')}/>
                                    <Column body={row => {
                                        return (
                                            <Checkbox value="remove" inputId="cb3"
                                                      onChange={(event) => this.onCheckboxChange(event, row)}
                                                      checked={row.methods.indexOf('remove') > -1}/>
                                        );
                                    }} header={this.props.t('delete')}/>
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

export default withTranslation()(UserPermission)
