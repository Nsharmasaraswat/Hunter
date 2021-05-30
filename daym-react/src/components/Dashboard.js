import React, {Component} from 'react';
import {UsersService} from "../service/UsersService";
import {Growl} from "primereact/growl";
import {withTranslation} from "react-i18next";
import {ProgressSpinner} from "primereact/progressspinner";
import {Column} from "primereact/column";
import {DataTable} from "primereact/datatable";
import moment from "moment";
import {Button} from "primereact/button";
import * as FileSaver from 'file-saver';
import * as XLSX from 'xlsx';
import {InputText} from "primereact/inputtext";
import {MultiSelect} from "primereact/multiselect";

class Dashboard extends Component {


  exportToCSV = () => {
    const ws = XLSX.utils.json_to_sheet(this.getExactData());
    const wb = { Sheets: { 'data': ws }, SheetNames: ['data'] };
    const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    const data = new Blob([excelBuffer], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'});
    FileSaver.saveAs(data, 'AllAppointments.xlsx');
  }

  getExactData = () => {
    let data = this.state.dataTableValue;
    if(this.state.startDate && this.state.startDate !== '')
      data = data.filter(
        each => moment(each.schedulingTime).isAfter(moment(this.state.startDate,'YYYY-MM-DD'))
      )
    if(this.state.endDate && this.state.endDate !== '')
      data = data.filter(
        each => moment(each.schedulingTime).isBefore(moment(this.state.endDate,'YYYY-MM-DD'))
      )

    return data;
  };
  getInTimeArrival = () => {
    const data = this.getExactData();
    const inTimeData =  data.filter((each) => moment(each.schedulingTime).isAfter(each.arrivalTime));
    return ((inTimeData.length/data.length) * 100).toFixed(2);
  };

  getAverageElapsedTime = () => {
    const data = this.getExactData();
    return data.reduce(
      (total, each) => {
        return total + moment(each.departureTime).diff(moment(each.arrivalTime, 'YYYY-MM-DD'), 'minutes')
      }, 0
    );
  };

    constructor() {
        super();
        this.state = {
            dataTableValue: [],
            loading: true,
            dashboardData: {},
            methods: [],
            startDate: '',
            endDate: '',
            type: null,
            showData: true
        };
        this.userService = new UsersService();
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        let user = JSON.parse(localStorage.getItem('daym-user'));
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/')[0].methods});
          this.userService.getDashboardData().then(res => {
            this.setState({dataTableValue: res.orders, dashboardData: res, loading: false});
          }).catch((err)=>{
            let error = err.response || err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('some_error')});
          });
        }else {
            // if(user.role === 4)
            // //     window.location = '#/accessdenied';
            // // else
            //     window.location = '#/appointments';
          this.setState({loading:false,showData : false})
        }

    }

    render(){
        return <>
            <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
            {
                !this.state.loading ? <div >
                  {
                    this.state.showData &&
                        <div className="p-grid layout-dashboard">
                        <div className="p-col-12 p-md-6 p-lg-4">
                            <div className="overview-box card">
                                <div className="overview-box-title">{this.props.t('avg_elapsed')}</div>
                                <div className="overview-box-value">{this.getAverageElapsedTime()+' mins'}</div>
                                <img src="assets/layout/images/dashboard/graph-purchases.svg" alt="roma"/>
                            </div>
                        </div>
                        <div className="p-col-12 p-md-6 p-lg-4">
                            <div className="overview-box card">
                                <div className="overview-box-title">{this.props.t('in_time')}</div>
                                <div className="overview-box-value">{this.getInTimeArrival()+' %'}</div>
                                <img src="assets/layout/images/dashboard/graph-messages.svg" alt="roma"/>
                            </div>
                        </div>
                        <div className="p-col-12 p-md-12 p-lg-12" style={{marginTop: 40}}>
                        </div>
                        <DataTable ref={(el) => this.dt = el} value={this.getExactData()}
                                   selectionMode="single"
                                   onFilter={(v)=>{
                                     console.log(v);
                                   }}
                                   header={
                                     <div style={{display: 'flex',justifyContent: 'flex-end',alignItems: 'center'}}>
                                       <Button
                                         type="button"
                                         style={{
                                           margin : '10px'
                                         }}
                                         label={this.props.t('export')}
                                         onClick={this.exportToCSV}/>
                                       {
                                         this.props.t('from')
                                       }
                                       <InputText
                                         type={'date'}
                                         value={this.state.startDate}
                                         onChange={(e) => {
                                           console.log('Start date',e.target.value);
                                           this.setState({
                                              startDate : e.target.value
                                           })
                                         }}
                                         style={{
                                           margin : '10px'
                                         }}
                                       />{
                                       this.props.t('to')
                                     }
                                       <InputText
                                         type={'date'}
                                         value={this.state.endDate}
                                         style={{
                                           margin : '10px'
                                         }}
                                         onChange={(e) => {
                                           console.log('End date',e.target.value);
                                           this.setState({
                                             endDate : e.target.value
                                           })
                                         }}
                                       />
                                       <Button
                                         type="button"
                                         label={this.props.t('reset')}
                                         onClick={()=>{
                                          this.setState({
                                            startDate : '' ,
                                            endDate: ''
                                          })
                                         }}/>
                                     </div>
                                   }
                                        paginator={true}
                                        rows={10}
                                        responsive={true}
                                        selection={this.state.dataTableSelection1}
                                        onSelectionChange={event => this.setState({dataTableSelection1: event.value})}>
                            <Column field="supplier" header={this.props.t('supplier')} sortable={true} filter />
                            <Column field="materialType" header={this.props.t('product')} sortable={true} filter/>
                            <Column field="amount" header={this.props.t('amount')} sortable={true} filter/>
                            <Column body={(row)=>row.operationType === 1 ? this.props.t('unload') : this.props.t('load')}
                                    header={this.props.t('type')}
                                    field={'type'}
                                    style={{textAlign:'center'}}
                                    filter
                                    filterElement={<MultiSelect
                                      value={this.state.type}
                                      options={[
                                        this.props.t('load'),
                                        this.props.t('unload')
                                      ]}
                                      itemTemplate={(option) => (
                                        <div className="p-multiselect-representative-option">
                                          <span className="image-text">{option.name}</span>
                                        </div>
                                      )}
                                      onChange={(e)=> {
                                        this.dt.filter(e.name, 'type', 'in');
                                        this.setState({type:e.value});
                                      }}
                                      optionLabel="name"
                                      optionValue="name"
                                      placeholder="All"
                                      className="p-column-filter" />
                                    }
                            />
                            <Column field="vehicle" header={this.props.t('truck')} sortable={true} filter/>
                            <Column field="vehicleLicense" header={this.props.t('licensePlate')} sortable={true} filter/>
                            <Column field="driver" header={this.props.t('driver')} sortable={true} filter/>
                            <Column field="driverUserId" header={this.props.t('driver')+' '+this.props.t('userName')} sortable={true} filter/>
                            <Column field="warehouse" header={this.props.t('wareHouse')} sortable={true} filter/>
                            <Column body={(row)=>moment(row.schedulingTime).format('YYYY-MM-DD hh:mm A')} header={this.props.t('schedule')} style={{textAlign:'center'}}/>
                            <Column body={(row)=>moment(row.arrivalTime).format('YYYY-MM-DD hh:mm A')} header={this.props.t('arrivalTime')} style={{textAlign:'center'}}/>
                            <Column body={(row)=>moment(row.departureTime).format('YYYY-MM-DD hh:mm A')} header={this.props.t('departureTime')} style={{textAlign:'center'}}/>
                        </DataTable>
                    </div>}</div> :
                    <div className="p-col-12"
                         style={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh'}}>
                        <ProgressSpinner style={{width: '50px', height: '50px'}} strokeWidth="3"
                                         animationDuration=".5s"/>
                    </div>
            }

        </>
    }
}
export default withTranslation()(Dashboard)
