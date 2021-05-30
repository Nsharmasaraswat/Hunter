import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {Growl} from "primereact/growl";
import {withTranslation} from "react-i18next";
import { ProgressSpinner } from 'primereact/progressspinner';
import "./Table.css";
import GoogleMapViewer from "./GoogleMapViewer";
import {TruckService} from "../service/TruckService";

class TrucksOvertime extends Component {

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
        this.truckService = new TruckService();
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/trucks-overtime').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/trucks-overtime')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        this.truckService.getAllTrucksOvertime().then(response => {
          //console.log('Trucks---',response.data);
            this.setState({dataTableValue: response.data, loading: false})
        }).catch((err)=>{

        });
    }



    render() {

        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />

                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('truck_overtime')}</h1>
                        {
                            this.state.loading ?
                                <div className="p-col-12" style={{display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
                                    <ProgressSpinner style={{width: '50px', height: '50px'}} strokeWidth="3" animationDuration=".5s"/>
                                </div> :
                                <DataTable ref={(el) => this.dt = el} value={this.state.dataTableValue}
                                           selectionMode="single" header={null} paginator={true}
                                           rows={10}
                                           responsive={true} selection={this.state.dataTableSelection1}
                                           onSelectionChange={event => this.setState({dataTableSelection1: event.value})}>
                                    <Column header={this.props.t('truck')} body={(row)=>row.truck.name}/>
                                    <Column header={this.props.t('licensePlate')} body={(row)=>row.truck.licensePlate}/>
                                    <Column header={this.props.t('driver')} body={(row)=>row.user.name}/>
                                    <Column header={this.props.t('view_appointment')} body={(row)=>
                                        <Button
                                            icon=""
                                            onClick={()=> window.location = '#/manage-appointment/'+row.appointment._id}
                                            label={this.props.t('view_appointment')}
                                            className="p-button-secondary"
                                        />
                                    } style={{textAlign: 'center', width: '12em'}}/>
                                    <Column header={this.props.t('locate_driver')} body={(row)=>
                                        <Button
                                            icon=""
                                            onClick={()=>this.setState({driverCoordinates: row.coordinates, display1: true})}
                                            label={this.props.t('locate_driver')}
                                            className="p-button-secondary"
                                        />
                                    } style={{textAlign: 'center', width: '12em'}}/>
                                </DataTable>
                        }
                    </div>
                    <Dialog header={this.props.t('locate_driver')} visible={this.state.display1} modal={true} style={{width: '30vw'}} footer={
                        <div>
                            <Button icon="pi pi-times" onClick={() => this.setState({display1:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                        </div>
                    } onHide={() => this.setState({display1:false})}>
                        <div className="p-grid">
                            <div className="p-col-12 p-md-12">
                                <GoogleMapViewer language={this.props.t('mapLocale')} coordinate={this.state.driverCoordinates} setCoordinates={()=>{}}/>
                                {/*<GoogleMapViewer coordinate={[85.8245,20.2961]} setCoordinates={()=>{}}/>*/}
                            </div>
                        </div>
                    </Dialog>
                </div>

            </div>
        );
    }
}

export default withTranslation()(TrucksOvertime)
