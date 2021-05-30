import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Growl} from "primereact/growl";
import {withTranslation} from "react-i18next";
import "./Table.css";
import {ProgressSpinner} from "primereact/progressspinner";
import {AppointmentService} from "../service/AppointmentService";
import moment from "moment";



class AllAppointments extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            user:JSON.parse(localStorage.getItem('daym-user')),
            userValues: [],
            userFields: [],
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
        };
        this.appointmentService = new AppointmentService();
        this.changeStatus = this.changeStatus.bind(this);
    }

    changeStatus(id, status, appointment, row) {
        this.appointmentService.changeStatus(id, status, appointment).then(response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(row);
            _dataTable[position] = response.data;
            this.setState({
                dataTableValue: _dataTable,
            })
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('some_error')});
        }).finally(()=>{
            this.setState({loading: false});
        });
    }

    componentDidMount() {
      // addLocale('es', {
      //   firstDayOfWeek: 1,
      //   dayNames: ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'],
      //   dayNamesShort: ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'],
      //   dayNamesMin: ['D', 'L', 'M', 'X', 'J', 'V', 'S'],
      //   monthNames: ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'],
      //   monthNamesShort: ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'],
      //   today: 'Hoy',
      //   clear: 'Claro'
      // });
      //
      // locale('es');
        // if(this.state.user.role === 8) {
        let query = this.state.user.role === 8 ? '' : '&todayAppointments=true';
        this.appointmentService.getAllAppointments(query).then(response => {
            // console.log('Ressss---->',response.data);
            this.setState({dataTableValue: response.data});
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('some_error')});
        }).finally(()=>{
            this.setState({loading: false});
        });
        // }
    }

    render() {
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('appointments')}</h1>
                        {
                            this.state.loading ?
                                <div className="p-col-12"
                                     style={{display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
                                    <ProgressSpinner style={{width: '50px', height: '50px'}} strokeWidth="3"
                                                     animationDuration=".5s"/>
                                </div> :
                                <DataTable ref={(el) => this.dt = el} value={this.state.dataTableValue}
                                           selectionMode="single" paginator={true} rows={10}
                                           responsive={true} selection={this.state.dataTableSelection1}
                                           onSelectionChange={event => this.setState({dataTableSelection1: event.value})}>
                                    <Column header={this.props.t('driver')} body={(row)=>{
                                        return row.appointment.driver ? row.appointment.driver.name ?? '' : '';
                                    }}/>
                                    <Column header={this.props.t('supplier')} body={(row)=>{
                                        return row.appointment.supplier ? row.appointment.supplier.name ?? '' : '';
                                    }}/>
                                    <Column header={this.props.t('wareHouse')} body={(row)=>{
                                        return row.warehouse ? row.warehouse.name ?? '' : '';
                                    }}/>
                                    <Column header={this.props.t('dock')} body={(row)=>{
                                        return row.dock ? row.dock.name ?? '' : '';
                                    }}/>
                                    <Column header={this.props.t('status')} body={(row)=>{
                                        if(row.status === 1){
                                            return 'Truck Entered WareHouse';
                                        }else if (row.status === 2){
                                            return 'Order Completed';
                                        }else if (row.status === 4){
                                            return 'Truck Arrived';
                                        }
                                    }}/>
                                    <Column header={this.props.t('delivery_time')} body={(row)=>{
                                        return moment(row.deliveryTime).local().format('hh:mm A')
                                    }}/>
                                    <Column field="totalElapsedTime" header={this.props.t('elapse_time')} sortable={true}/>
                                    <Column header={this.props.t('action')} body={(row)=>{
                                        if(row.status === 1){
                                            return <Button label={this.props.t('truck_entered')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                                                this.changeStatus(row._id,4,row.appointment._id,row);
                                            }}/>
                                        }else if (row.status === 4){
                                            return <Button label={this.props.t('order_complete')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                                                this.changeStatus(row._id,2,row.appointment._id,row);
                                            }}/>
                                        }
                                    }}/>
                                </DataTable>
                        }
                    </div>
                </div>
            </div>
        );
    }
}

export default withTranslation()(AllAppointments)
