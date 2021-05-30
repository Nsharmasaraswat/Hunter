import React, {Component} from 'react';
import {Growl} from "primereact/growl";
import {withTranslation} from "react-i18next";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import {AppointmentService} from "../service/AppointmentService";
import moment from "moment";
import {ProgressSpinner} from "primereact/progressspinner";
import { Calendar, momentLocalizer } from "react-big-calendar";
import "react-big-calendar/lib/css/react-big-calendar.css";
import CustomOrderCalendar from "./CustomOrderCalendar";

const localizer = momentLocalizer(moment);

class AppointmentCalendar extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            orderData:[],
            methods: [],
            selectedDate: '',
            role: JSON.parse(localStorage.getItem('daym-user')).role,
            selectedMonth: '',
            loading: true,
            fullcalendarEvents: [
            ],
            fullcalendarOptions: {
                plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
                defaultView: 'dayGridMonth',
                defaultDate: new Date(),
                header: {
                    left: 'prev,next, today',
                    center: 'title',
                    right: 'dayGridMonth,timeGridWeek,timeGridDay'
                },
                editable: false
            },
        };
        this.appointmentService = new AppointmentService();
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/appointment-calendar').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/appointment-calendar')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }

        this.appointmentService.getAllAppointmentsForCalendar().then(response => {
            let _data = response.data.map((each,index)=>{
                return {
                    id: index,
                    start:  moment(moment(each.deliveryOn).format('YYYY-MM-DD')+'T'+moment(each.deliveryTime).format('HH:mm:ss')).toDate(),
                    end:  moment(moment(each.deliveryOn).format('YYYY-MM-DD')+'T'+moment(each.deliveryTime).format('HH:mm:ss')).toDate(),
                    title: each.product.name+'('+each.dock.name+')',
                    url: "#/manage-appointment/"+each.appointment._id
                }
            });
            this.setState({fullcalendarEvents: [..._data], orderData: response.data});
        }).catch((err)=>{

        }).finally(()=>{
            this.setState({loading: false})
        });
    }


    render() {

        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('all')+' '+this.props.t('appointments')}</h1>
                        {
                            this.state.loading ?
                                <div className="p-col-12" style={{display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
                                    <ProgressSpinner style={{width: '50px', height: '50px'}} strokeWidth="3" animationDuration=".5s"/>
                                </div> :
                                <CustomOrderCalendar orders={this.state.orderData} />
                                // <Calendar
                                //     localizer={localizer}
                                //     defaultDate={new Date()}
                                //     defaultView="week"
                                //     events={this.state.fullcalendarEvents}
                                //     style={{ height: "100vh" }}
                                //     eventPropGetter={
                                //         (event) => {
                                //             let newStyle = {
                                //                 maxLines: 2,
                                //             };
                                //             return {
                                //                 style: newStyle
                                //             };
                                //         }
                                //     }
                                //     onSelectEvent={(info)=>{
                                //         window.location = info.url;
                                //     }}
                                // />
                                // <FullCalendar events={this.state.fullcalendarEvents} options={this.state.fullcalendarOptions}/>
                        }
                    </div>
                </div>
            </div>
        );
    }
}

export default withTranslation()(AppointmentCalendar)
