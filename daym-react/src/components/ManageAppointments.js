import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import { FileUpload } from 'primereact/fileupload';
import {withTranslation} from "react-i18next";
import {AppointmentService} from "../service/AppointmentService";
import {Dropdown} from "primereact/dropdown";
import {OrderService} from "../service/OrderService";
import {Calendar} from "primereact/calendar";
import {ProgressSpinner} from "primereact/progressspinner";
import moment from "moment";
import {HolidayService} from "../service/HolidayService";
import GoogleMapViewer from "./GoogleMapViewer";

class ManageAppointments extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            receipt:[],
            loading: true,
            buttonLoading: false,
            buttonLoadingNotify: false,
            invalidDates: [],
            methods: [],
            deleteLoading: false,
            user: JSON.parse(localStorage.getItem('daym-user'))
        };
        this.appointmentService = new AppointmentService();
        this.orderService = new OrderService();
        this.holidayService = new HolidayService();
        this.handleCreateOrder = this.handleCreateOrder.bind(this);
        this.handleDeleteOrder = this.handleDeleteOrder.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
        this.validate = this.validate.bind(this);
        this.uploadReceipt = this.uploadReceipt.bind(this);
        this.handleSchedule = this.handleSchedule.bind(this);
        this.handleAddDriver = this.handleAddDriver.bind(this);
        this.updateStatusToThree = this.updateStatusToThree.bind(this);
        this.handleGenerateTicket = this.handleGenerateTicket.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleLocateDriver = this.handleLocateDriver.bind(this);
        this.handleNotifyDriver = this.handleNotifyDriver.bind(this);
    }

    uploadReceipt(event) {
            this.appointmentService.uploadFile(event.files).then((response)=>{
                let files = [];
                if (response.data.file) {
                    files.push(response.data.file);
                } else {
                    files = response.data.files;
                }
                this.setState({receipt: files});
                this.updateStatusToThree(files);
            }).catch((err) => {
                let error = err.response.data.message;
                this.growl.show({
                    severity: 'error',
                    summary: this.props.t('error'),
                    detail: error || 'Something Went Wrong'
                });
            });
    }

    updateStatusToThree(files) {
        this.appointmentService.updateStatusToThree(this.state.appointmentId, files).then((response)=>{
            this.setState({appointmentDetails: {...this.state.appointmentDetails,...response.data}})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" disabled={this.state.deleteLoading} className="p-button-danger"
                        style={{marginRight: '.5em'}} onClick={() => {
                    this.handleDeleteOrder(rowData);
                }}/>
            }
            {
                this.state.methods.indexOf('patch') >= 0 &&
                <Button type="button" icon="pi pi-pencil" className="p-button-warning" onClick={() => {
                    this.setState({display: true, quantity: rowData.quantity, editData: rowData});
                }}/>
            }
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/manage-appointment').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/manage-appointment')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        const appointmentId = this.props.location.pathname.split('/')[2];
        this.setState({appointmentId});

        this.appointmentService.getAppointment(appointmentId||'').then(response => {
            let _receipts = [];
            if(response.data.receipt){
                if(typeof response.data.receipt === 'string'){
                    _receipts.push(response.data.receipt);
                }else{
                    _receipts = response.data.receipt;
                }
            }
            this.appointmentService.getAllDriver(response.data.supplier._id).then(response => {
                this.setState({allDriver: response.data.map((each)=>{
                        return {
                            label: each.name,
                            value: each._id
                        };
                    })})
            }).catch((err) => {
                let error = err.response.data.message;
                this.growl.show({
                    severity: 'error',
                    summary: this.props.t('error'),
                    detail: error || 'Something Went Wrong'
                });
            });
            this.setState({dataTableValue: response.data.orders, loading: false, appointmentDetails: response.data, receipt: _receipts})
        }).catch((err) => {
          console.log('Errrrrrr----->',err);

        });

        this.appointmentService.getAllProducts().then(response => {
            this.setState({allProducts: response.data.map((each)=>{
                    return {
                        label: `${each.name}(${each.productType.name})`,
                        value: JSON.stringify(each)
                    };
                })})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });

        this.appointmentService.getAllGates().then(response => {
            this.setState({allGatesEntry: response.data.filter(e=>e.gateType===1).map((each)=>{
                    return {
                        label: each.name,
                        value: each._id
                    };
                }),allGatesExit: response.data.filter(e=>e.gateType===2).map((each)=>{
                    return {
                        label: each.name,
                        value: each._id
                    };
                })})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });

        this.appointmentService.getAllDocks().then(response => {
            this.setState({allDocks: response.data.map((each)=>{
                    return {
                        label: `${each.name}(${each.parentId.name})`,
                        value: JSON.stringify(each)
                    };
                })})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });


        let userId = JSON.parse(localStorage.getItem('daym-user'))._id;
        this.appointmentService.getAllTrucks(userId).then(response => {
            this.setState({allTrucks: response.data.map((each)=>{
                    return {
                        label: each.name,
                        value: each._id
                    };
                })})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });
    }

    validate(){
        if(this.state.editData){
            if(!this.state.quantity || this.state.quantity === 0){
                this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('quantity_cant')  });
                return false;
            }
        }else{
            if(!(this.state.selectedProduct && this.state.quantity && this.state.selectedDock)){
                this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('all_fields_mandatory')});
                return false;
            }else if(this.state.quantity === 0){
                this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('quantity_cant')  });
                return false;
            }
        }

        return true;
    }

    handleCreateOrder(){
        if(this.validate()) {
            this.setState({buttonLoading: true});
            if (this.state.editData) {
                this.orderService.editOrder(this.state.editData._id, this.state.appointmentId, this.state.quantity ).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('order') + ' ' + this.props.t('edited_Successfully')
                    });
                    let _dataTable = this.state.dataTableValue;
                    const position = this.state.dataTableValue.indexOf(this.state.editData);
                    _dataTable[position] = response.data;
                    this.setState({
                        dataTableValue: _dataTable,
                        display: false,
                        buttonLoading: false,
                    })
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({
                        severity: 'error',
                        summary: this.props.t('error'),
                        detail: error || 'Something Went Wrong'
                    });
                    this.setState({buttonLoading: false});
                });
            } else {
                this.orderService.createOrder(this.state.appointmentId,JSON.parse(this.state.selectedProduct)._id, this.state.quantity, JSON.parse(this.state.selectedDock)._id).then(async (response) => {
                    this.growl.show({
                        severity: 'success',
                        summary: this.props.t('success'),
                        detail: this.props.t('order') + ' ' + this.props.t('created_Successfully')
                    });
                    this.setState({
                        dataTableValue: [response.data, ...this.state.dataTableValue],
                        display: false,
                        buttonLoading: false,
                    })
                }).catch((err) => {
                    let error = err.response.data.message;
                    this.growl.show({
                        severity: 'error',
                        summary: this.props.t('error'),
                        detail: error || 'Something Went Wrong'
                    });
                    this.setState({buttonLoading: false});
                });
            }
        }

    }

    handleDeleteOrder(rowData){
        this.setState({deleteLoading: true});
        this.orderService.deleteOrder(rowData._id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('order') + ' ' + this.props.t('deleted_Successfully') });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false
            })
        }).catch((err)=>{
            let error = err.response.data.message;
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_delete') + ' ' + this.props.t('order') });
            this.setState({deleteLoading: false});
        });
    }

    handleSchedule(){
        this.appointmentService.updateStatusToFour(this.state.appointmentId, this.state.selectedGateEntry, this.state.selectedGateExit).then((response)=>{
            this.setState({appointmentDetails: {...this.state.appointmentDetails,...response.data}})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });
    }

    handleLocateDriver(){
        this.appointmentService.getDriverCoordinate(this.state.appointmentId).then((response)=>{
            this.setState({driverCoordinates: response.coordinates, lastUpdated: response.coordinateLastUpdated, display1: true})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });
    }

    handleNotifyDriver(){
        if(this.state.textMesage && this.state.textMesage !== ''){
            let drivers = [];
            this.setState({buttonLoadingNotify: true});
            drivers.push(this.state.appointmentDetails.driver._id);
            this.appointmentService.notifyDriver(this.state.textMesage, drivers).then((response)=>{
                this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('notify_success')});
                this.setState({driverCoordinates: response.coordinates, lastUpdated: response.coordinateLastUpdated, display2: false, buttonLoadingNotify: false})
            }).catch((err) => {
                let error = err.response.data.message;
                this.setState({buttonLoadingNotify: false});
                this.growl.show({
                    severity: 'error',
                    summary: this.props.t('error'),
                    detail: error || 'Something Went Wrong'
                });
            });
        }

    }

    handleAddDriver(){

        // if(!this.state.selectedDriver){
        //     this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('driver_cant')  });
        // } else {


            this.appointmentService.addDriver(this.state.appointmentId, this.state.selectedDriver, this.state.selectedTruck, this.state.appointmentDetails ? this.state.appointmentDetails.operationType === 2 ? {status: 3} : {}: {}).then((response)=>{
                this.setState({appointmentDetails: {...this.state.appointmentDetails,...response.data}})
            }).catch((err) => {
                let error = err.response.data.message;
                this.growl.show({
                    severity: 'error',
                    summary: this.props.t('error'),
                    detail: error || 'Something Went Wrong'
                });
            });
        // }

    }

    handleGenerateTicket(){
        const langStorage = localStorage.getItem('i18nextLng');
        const currentLang = langStorage === 'en' ? 'english' : langStorage === 'sp' ? 'spanish' : 'portuguese';
        this.appointmentService.generateTicket(this.state.appointmentId,currentLang).then((response)=>{
            this.setState({appointmentDetails: {...this.state.appointmentDetails,...response.data}})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });
    }

    handleCancel(){
        this.appointmentService.cancelAppointment(this.state.appointmentId).then((response)=>{
            this.setState({appointmentDetails: {...this.state.appointmentDetails,...response.data}})
        }).catch((err) => {
            let error = err.response.data.message;
            this.growl.show({
                severity: 'error',
                summary: this.props.t('error'),
                detail: error || 'Something Went Wrong'
            });
        });
    }

    render() {
        const statusValues = [ this.props.t('initiated'),this.props.t('pending'),this.props.t('changed_supplier'),
            this.props.t('confirmed_supplier'), this.props.t('scheduled'),this.props.t('accept_security'),
            this.props.t('reject_security'),this.props.t('truck_enter'),
            this.props.t('complete'),this.props.t('cancelled')];
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateOrder} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );

        const tableHeader = (
            <div style={{display: 'flex', justifyContent: 'flex-end'}}>
                <Dialog header={(this.state.editData ? this.props.t('edit'): this.props.t('add'))+' '+this.props.t('orders')} visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid">
                        {
                            !this.state.editData &&
                            <div className="p-col-12 p-md-12">
                                <Dropdown
                                    style={{width: '100%'}}
                                    placeholder={this.props.t('product')}
                                    value={this.state.selectedProduct}
                                    options={this.state.allProducts}
                                    onChange={(event) => {
                                      const selectedProduct = JSON.parse(event.value);
                                      this.setState({selectedProduct: event.value, selectedDocks: this.state.allDocks.filter((e)=>
                                              JSON.parse(e.value).productTypes.indexOf(selectedProduct.productType._id) >= 0
                                          )});
                                    }}/>
                            </div>
                        }
                        <div className="p-col-12 p-md-12">
                            <InputText placeholder={this.props.t('quantity')} style={{width: '100%'}} value={this.state.quantity} onChange={(e) => this.setState({quantity: e.target.value})} type={'number'}/>
                        </div>
                        {
                            !this.state.editData &&
                            <div className="p-col-12 p-md-12">
                                <Dropdown style={{width: '100%'}} placeholder={this.props.t('dock')}
                                          value={this.state.selectedDock} options={this.state.selectedDocks}
                                          onChange={(event) => this.setState({selectedDock: event.value})}/>
                            </div>
                        }
                    </div>
                </Dialog>
            </div>
        );

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
                {
                     this.state.user.role === 4 &&
                     (this.state.appointmentDetails && this.state.appointmentDetails.operationType === 1 &&
                         (this.state.appointmentDetails.status === 1 || this.state.appointmentDetails.status === 2)) &&
                        <>
                            <div className="p-col-6">
                                <h3>{this.props.t('receipt')}</h3>
                                <FileUpload name="demo" customUpload uploadHandler={this.uploadReceipt} accept="application/pdf"/>
                            </div>
                        </>

                }

                {
                    (this.state.appointmentDetails &&
                        ((this.state.appointmentDetails.user && this.state.user._id === this.state.appointmentDetails.user._id ) || (this.state.appointmentDetails.userType
                            &&  this.state.user.userType._id === this.state.appointmentDetails.userType._id ))) && (this.state.appointmentDetails
                                && this.state.appointmentDetails.status === 3) &&
                    <div className="card p-col-12" style={{margin: 6,width: '98%'}}>
                        <div className="p-grid p-fluid">
                            <div className="p-col-12">
                                <h2>{this.props.t('schedule_appointment')}</h2>
                            </div>
                            <div className="p-col-4">
                                <Dropdown style={{width: '100%'}} placeholder={this.props.t('entrance')+' '+this.props.t('gate')}
                                          value={this.state.selectedGateEntry} options={this.state.allGatesEntry}
                                          onChange={(event) => this.setState({selectedGateEntry: event.value})}/>
                            </div>
                            <div className="p-col-4">
                                <Dropdown style={{width: '100%'}} placeholder={this.props.t('exit')+' '+this.props.t('gate')}
                                          value={this.state.selectedGateExit} options={this.state.allGatesExit}
                                          onChange={(event) => this.setState({selectedGateExit: event.value})}/>
                            </div>
                            <div className="p-col-4">
                                <Button icon="pi pi-check" onClick={this.handleSchedule} label={this.props.t('schedule_appointment')} />
                            </div>
                        </div>
                    </div>

                }
                {
                    this.state.user.role === 4 &&
                    (this.state.appointmentDetails && (this.state.appointmentDetails.status === 1 || this.state.appointmentDetails.status === 2 )) &&
                    <div className="card p-col-12" style={{margin: 6,width: '98%'}}>
                        <div className="p-grid p-fluid">
                            <div className="p-col-12">
                                <h2>{this.props.t('add_driver')}</h2>
                            </div>
                            <div className="p-col-6">
                                <Dropdown style={{width: '100%'}} placeholder={this.props.t('drivers')}
                                          value={this.state.selectedDriver} options={this.state.allDriver}
                                          onChange={(event) => this.setState({selectedDriver: event.value})}/>
                            </div>
                            <div className="p-col-6">
                                <Dropdown style={{width: '100%'}} placeholder={this.props.t('trucks')}
                                          value={this.state.selectedTruck} options={this.state.allTrucks}
                                          onChange={(event) => this.setState({selectedTruck: event.value})}/>
                            </div>
                            <div className="p-col-6">
                                <Button icon="pi pi-check" onClick={this.handleAddDriver} label={this.props.t('add_driver')} />
                            </div>
                        </div>
                    </div>

                }
                {
                    (this.state.appointmentDetails &&
                        ((this.state.appointmentDetails.user && this.state.user._id === this.state.appointmentDetails.user._id )
                            || (this.state.appointmentDetails.userType &&  this.state.user.userType._id === this.state.appointmentDetails.userType._id )))
                                && !this.state.appointmentDetails.ticket && this.state.appointmentDetails.status === 4 &&
                    <div className="p-col-4 p-fluid">
                        <Button icon="pi pi-check" onClick={this.handleGenerateTicket} label={this.props.t('generate_ticket')} />
                    </div>
                }
                {
                    (this.state.appointmentDetails && this.state.appointmentDetails.status === 7) &&
                    <div className="p-col-4 p-fluid">
                        <Dialog header={this.props.t('notify_driver')} visible={this.state.display2} modal={true} style={{width: '30vw'}} footer={(
                            <div>
                                <Button icon="pi pi-times" onClick={() => this.setState({display2:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                                {
                                    !this.state.buttonLoadingNotify ?
                                        <Button icon="pi pi-check" onClick={this.handleNotifyDriver} label={this.props.t('save')} /> :
                                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                                }
                            </div>
                        )} onHide={() => this.setState({display2:false})}>
                            <div className="p-grid">
                                <div className="p-col-12 p-md-12">
                                    <InputText placeholder={this.props.t('message')} style={{width: '100%'}} value={this.state.textMesage} onChange={(e) => this.setState({textMesage: e.target.value})}/>
                                </div>
                            </div>
                        </Dialog>
                        <Button icon="pi pi-check" onClick={()=> this.setState({display2: true})} label={this.props.t('notify_driver')} />
                    </div>
                }
                {
                    (this.state.appointmentDetails && this.state.appointmentDetails.status === 7) &&
                    <div className="p-col-4 p-fluid">
                        <Button icon="pi pi-check" onClick={this.handleLocateDriver} label={this.props.t('locate_driver')} />
                    </div>
                }
                {
                    this.state.appointmentDetails  && this.state.appointmentDetails.ticket &&
                    <div className="p-col-4 p-fluid">
                        <Button icon="pi pi-check" onClick={()=>{
                            window.location = this.state.appointmentDetails.ticket;
                        }} label={this.props.t('view_ticket')} />
                    </div>
                }

                {
                    this.state.appointmentDetails  && this.state.receipt.length > 0 &&
                    <div className="p-col-4 p-fluid">
                        <Dropdown
                            style={{width: '100%'}}
                            placeholder={this.props.t('view_receipt')}
                            value={this.state.selectedReciept}
                            options={this.state.receipt.map((each, i)=>{
                                return {
                                    label: 'Receipt ' + i,
                                    value: each
                                }
                            })}
                            onChange={(event) => {
                                window.open(event.value, '_blank');
                                this.setState({selectedReciept: event.value})
                            }}
                        />
                    </div>
                }
                {
                    (this.state.appointmentDetails &&
                        ((this.state.appointmentDetails.user && this.state.user._id === this.state.appointmentDetails.user._id )
                            || (this.state.appointmentDetails.userType &&  this.state.user.userType._id === this.state.appointmentDetails.userType._id )))
                                && this.state.appointmentDetails.status < 3 && this.state.appointmentDetails.status !== -1 &&
                    <div className="p-col-4 p-fluid">
                        <Button icon="pi pi-check" onClick={this.handleCancel} label={this.props.t('cancel')} />
                    </div>
                }
                {
                    !this.state.loading &&
                    <div className="card p-col-12" style={{margin: 6,width: '98%'}}>
                        <div className="p-grid">
                            <div className="p-col-3">
                                <h4>{this.props.t('schedule')}</h4>
                                <h3>{this.state.appointmentDetails && moment(this.state.appointmentDetails.deliveryOn).format("DD-MM-YYYY")}</h3>
                            </div>
                            <div className="p-col-3">
                                <h4>{this.props.t('supplier')}</h4>
                                <h3>{this.state.appointmentDetails && this.state.appointmentDetails.supplier.name}</h3>
                            </div>
                            <div className="p-col-3">
                                <h4>{this.props.t('appointment_for')}</h4>
                                {/*<h3>{this.state.appointmentDetails && this.state.appointmentDetails.user.name}</h3>*/}
                                <h3>{this.state.appointmentDetails && this.state.appointmentDetails.user ? this.state.appointmentDetails.user.name : this.state.appointmentDetails.userType.name}</h3>
                            </div>
                            <div className="p-col-3">
                                <h4>{this.props.t('status')}</h4>
                                <h3>{this.state.appointmentDetails && this.state.appointmentDetails.status !== -1  ? statusValues[this.state.appointmentDetails.status] : 'Cancelled'}</h3>
                            </div>
                        </div>
                    </div>
                }
                {
                    this.state.appointmentDetails &&
                    <div className="card p-col-12" style={{margin: 6,width: '98%'}}>
                        <div className="p-grid">
                            <div className="p-col-3">
                                <h4>{this.props.t('entrance')+' '+this.props.t('gate')}</h4>
                                <h3>{this.state.appointmentDetails && this.state.appointmentDetails.entranceGate ? this.state.appointmentDetails.entranceGate.name : this.props.t('not_added')}</h3>
                            </div>
                            <div className="p-col-3">
                                <h4>{this.props.t('exit')+' '+this.props.t('gate')}</h4>
                                <h3>{this.state.appointmentDetails && this.state.appointmentDetails.exitGate ? this.state.appointmentDetails.exitGate.name : this.props.t('not_added')}</h3>
                            </div>
                            <div className="p-col-3">
                                <h4>{this.props.t('driver')}</h4>
                                <h3>{this.state.appointmentDetails && this.state.appointmentDetails.driver ? this.state.appointmentDetails.driver.name : this.props.t('not_added')}</h3>
                            </div>
                            <div className="p-col-3">
                                <h4>{this.props.t('truck')}</h4>
                                <h3>{this.state.appointmentDetails && this.state.appointmentDetails.truck ? this.state.appointmentDetails.truck.name : this.props.t('not_added')}</h3>
                            </div>
                        </div>
                    </div>
                }
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('orders')}</h1>
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
                                    <Column body={(row) => row.product.name} header={this.props.t('product')}/>
                                    <Column header={this.props.t('quantity')} body={(row) => row.quantity}
                                            style={{textAlign: 'center'}}/>
                                    <Column header={this.props.t('dock')} body={(row) => row.dock.name}
                                            style={{textAlign: 'center'}}/>
                                    <Column header={this.props.t('delivery_time')} body={(row) => moment(row.deliveryTime).local().format('hh:mm A')}
                                            style={{textAlign: 'center'}}/>
                                    {
                                        this.state.appointmentDetails && this.state.appointmentDetails.status < 3 && this.state.appointmentDetails.status !== -1 &&
                                        <Column header={actionHeader} body={this.actionButtons}
                                                style={{textAlign: 'center', width: '8em'}}/>
                                    }

                                </DataTable>
                        }
                    </div>
                </div>
                <Dialog header={this.props.t('locate_driver')} visible={this.state.display1} modal={true} style={{width: '30vw'}} footer={
                    <div>
                        <Button icon="pi pi-times" onClick={() => this.setState({display1:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                    </div>
                } onHide={() => this.setState({display1:false})}>
                    <div className="p-grid">
                        <div className="p-col-12 p-md-12">
                            <p>
                                {`Last Updated At : ${moment(this.state.lastUpdated).local().format('DD/MM/YY hh:mm A')}`}
                            </p>
                        </div>
                        <div className="p-col-12 p-md-12">
                            <GoogleMapViewer language={this.props.t('mapLocale')} coordinate={this.state.driverCoordinates} setCoordinates={()=>{}}/>
                            {/*<GoogleMapViewer coordinate={[85.8245,20.2961]} setCoordinates={()=>{}}/>*/}
                        </div>
                    </div>
                </Dialog>
            </div>
        );
    }
}

export default withTranslation()(ManageAppointments)
