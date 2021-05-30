import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import {HolidayService} from "../service/HolidayService";
import {withTranslation} from "react-i18next";
import {Calendar} from "primereact/calendar";
import { ProgressSpinner } from 'primereact/progressspinner';
import "./Table.css";

class MangeHoliday extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            methods: [],
            selectedDate: '',
            selectedMonth: '',
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
        };
        this.holidayService = new HolidayService();
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
                    const d = new Date(rowData.month + '/' + rowData.date + '/' + new Date().getFullYear());
                    this.setState({display: true, name: rowData.name, date2: d, editData: rowData});
                }}/>
            }
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/manage-holiday').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/manage-holiday')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        this.holidayService.getAllHolidays().then(response => {
            this.setState({dataTableValue: response.data, loading: false})
        }).catch((err)=>{

        });
    }

    validate(){
        if(!this.state.name){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('name') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        } else if(!this.state.date2){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('date') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }
        return true;
    }

    handleCreateWarehouse(){
        if(this.validate()) {
            this.setState({buttonLoading: true});
            if (this.state.editData) {
                let _data = {name: this.state.name,date: this.state.selectedDate,month: this.state.selectedMonth};
                this.holidayService.editHoliday(this.state.editData._id,_data).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('holiday') + ' ' + this.props.t('edited_Successfully')
                    });
                    let _dataTable = this.state.dataTableValue;
                    const position = this.state.dataTableValue.indexOf(this.state.editData);
                    _dataTable[position] = response.data;
                    this.setState({
                        dataTableValue: _dataTable,
                        display: false,
                        buttonLoading: false
                    })
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({
                        severity: 'error',
                        summary: this.props.t('error'),
                        detail: error || this.props.t('can_not_edit') + ' ' + this.props.t('holiday')
                    });
                    this.setState({buttonLoading: false});
                });
            } else {
                this.holidayService.createHoliday(this.state.name, this.state.selectedDate, this.state.selectedMonth).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('holiday') + ' ' + this.props.t('created_Successfully')
                    });
                    this.setState({
                        dataTableValue: [response.data, ...this.state.dataTableValue],
                        display: false,
                        buttonLoading: false
                    })
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({
                        severity: 'error',
                        summary: this.props.t('error'),
                        detail: error || this.props.t('can_not_create') + ' ' + this.props.t('holiday')
                    });
                    this.setState({buttonLoading: false});
                });
            }
        }

    }
    handleDeleteWarehouse(rowData){
        this.setState({deleteLoading: true});
        this.holidayService.deleteHoliday(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('holiday') + ' ' + this.props.t('deleted_Successfully') });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false,
            })
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_delete') + ' ' + this.props.t('holiday') });
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
                <Dialog contentStyle={{overflowY: 'unset !important'}} header={(this.state.editData ? this.props.t('edit'): this.props.t('add'))+' '+this.props.t('holiday')} visible={this.state.display} modal={true} style={{width: '30vw',maxHeight: '100vh'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid-m p-fluid">
                        <div className="p-col-12 p-md-12">
                            <InputText placeholder={this.props.t('name')} value={this.state.name} onChange={(e) => this.setState({name: e.target.value})} />
                        </div>
                        <div className="p-col-12 p-md-12">
                            <Calendar locale={{
                                firstDayOfWeek: parseInt(this.props.t('firstDayOfWeek')),
                                dayNames: JSON.parse(this.props.t('dayNames')),
                                dayNamesShort: JSON.parse(this.props.t('dayNamesShort')),
                                dayNamesMin: JSON.parse(this.props.t('dayNamesMin')),
                                monthNames: JSON.parse(this.props.t('monthNames')),
                                monthNamesShort: JSON.parse(this.props.t('monthNamesShort')),
                                today: this.props.t('today'),
                                clear: this.props.t('clear'),
                            }} placeholder={this.props.t('holiday')} value={this.state.date2} onChange={(e) => {
                                let d = new Date(e.value);
                                this.setState({selectedDate: d.getDate(), selectedMonth: d.getMonth()+1, date2: d});
                            }}/>
                        </div>

                    </div>
                </Dialog>
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_holiday')} icon="pi pi-cog"
                            onClick={() => this.setState({display: true, name: '', date2: '', editData: null})}/>
                }
            </div>
        );
        let actionHeader = <Button type="button" icon="pi pi-cog"/>;

        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />

                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('holidays')}</h1>
                        {
                            this.state.loading ?
                                <div className="p-col-12" style={{display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
                                    <ProgressSpinner style={{width: '50px', height: '50px'}} strokeWidth="3" animationDuration=".5s"/>
                                </div> :
                                <DataTable ref={(el) => this.dt = el} value={this.state.dataTableValue}
                                           selectionMode="single" header={tableHeader} paginator={true}
                                           rows={10}
                                           responsive={true} selection={this.state.dataTableSelection1}
                                           onSelectionChange={event => this.setState({dataTableSelection1: event.value})}>
                                    <Column field="name" header={this.props.t('name')} sortable={true}/>
                                    <Column body={(row) => {
                                        if (row.month === 1)
                                            return this.props.t('january');
                                        else if (row.month === 2)
                                            return this.props.t('february');
                                        else if (row.month === 3)
                                            return this.props.t('march');
                                        else if (row.month === 4)
                                            return this.props.t('april');
                                        else if (row.month === 5)
                                            return this.props.t('may');
                                        else if (row.month === 6)
                                            return this.props.t('june');
                                        else if (row.month === 7)
                                            return this.props.t('july');
                                        else if (row.month === 8)
                                            return this.props.t('august');
                                        else if (row.month === 9)
                                            return this.props.t('september');
                                        else if (row.month === 10)
                                            return this.props.t('october');
                                        else if (row.month === 11)
                                            return this.props.t('november');
                                        else
                                            return this.props.t('december');
                                    }} header={this.props.t('month')}/>
                                    <Column field="date" header={this.props.t('day')} sortable={true}/>
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

export default withTranslation()(MangeHoliday)
