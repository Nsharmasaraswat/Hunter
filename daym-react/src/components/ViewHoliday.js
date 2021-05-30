import React, {Component} from 'react';
import {Growl} from "primereact/growl";
import {HolidayService} from "../service/HolidayService";
import {withTranslation} from "react-i18next";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import {FullCalendar} from "primereact/fullcalendar";

class MangeHoliday extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            methods: [],
            selectedDate: '',
            selectedMonth: '',
            fullcalendarOptions: {
                plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
                defaultView: 'dayGridMonth',
                defaultDate: new Date(),
                header: {
                    left: 'prev,next, today',
                    center: 'title',
                    right: ''
                },
                editable: false
            },
        };
        this.holidayService = new HolidayService();
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/view-holiday').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/view-holiday')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        this.holidayService.getAllHolidays().then(response => {
            let _data = response.data.map((each,index)=>{
                return {
                    id: index,
                    start: (new Date().getFullYear() + '-' + (each.month < 10 ? '0' : '')+ each.month + '-' + (each.date < 10 ? '0' : '')+each.date),
                    title: each.name
                }
            });
            let _prev_data = response.data.map((each,index)=>{
                return {
                    id: index,
                    start: (((new Date().getFullYear())-1) + '-' + (each.month < 10 ? '0' : '')+ each.month + '-' + (each.date < 10 ? '0' : '')+each.date),
                    title: each.name
                }
            });
            let _next_data = response.data.map((each,index)=>{
                return {
                    id: index,
                    start: (((new Date().getFullYear())+1) + '-' + (each.month < 10 ? '0' : '')+ each.month + '-' + (each.date < 10 ? '0' : '')+each.date),
                    title: each.name
                }
            });
            this.setState({fullcalendarEvents: [..._data, ..._prev_data,..._next_data]});
        }).catch((err)=>{

        });
    }


    render() {
        const locales = {
            sp : 'es',
            pr : 'pt',
            en : 'en'
        };
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('all')+' '+this.props.t('holidays')}</h1>
                        <FullCalendar events={this.state.fullcalendarEvents} options={{...this.state.fullcalendarOptions, ...{locale: locales[localStorage.getItem('i18nextLng')]}}} />
                    </div>
                </div>
            </div>
        );
    }
}

export default withTranslation()(MangeHoliday)
