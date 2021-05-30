import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import { withTranslation } from 'react-i18next';
import {Dropdown} from "primereact/dropdown";
import {AppointmentService} from "../service/AppointmentService";
import {ProgressSpinner} from "primereact/progressspinner";
import {InputSwitch} from "primereact/inputswitch";
import {Calendar} from "primereact/calendar";

class Appointments extends Component {

    constructor(props) {
        super();
        this.state = {
            dataTableValue:[],
            ordersList: [],
            ordersApiList: [],
            allDocks: [],
            locale: '',
            allType: [],
            type: 1,
            methods: [],
            loading: true,
            buttonLoading: false,
            automatic: true,
            deliveryTime: new Date(),
            user: JSON.parse(localStorage.getItem('daym-user'))

        };
        this.appointmentService = new AppointmentService();
        this.handleCreateHeadQuarter = this.handleCreateHeadQuarter.bind(this);
        this.viewGate = this.viewGate.bind(this);
        this.validate = this.validate.bind(this);
    }

    viewGate(rowData, column) {
        return <div style={{padding: '0px 8px'}}>
            <Button label={this.props.t('view_details')} className="p-button-info p-button-raised"  style={{marginRight: '.5em'}} onClick={()=>{
                window.location = '#/manage-appointment/'+rowData._id;
            }}/>
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/appointments').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/appointments')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }

        this.appointmentService.getAllAppointments().then(response => {
            this.setState({dataTableValue: response.data, loading: false})
        });

        this.appointmentService.getAllProducts().then(response => {
            this.setState({allProducts: response.data.map((each)=>{
                    return {
                        label: `${each.name}(${each.productType.name})`,
                        value: JSON.stringify(each)
                    };
                })})
        });

        this.appointmentService.getAllDocks().then(response => {
            this.setState({allDocks: response.data.map((each)=>{
                    return {
                        label: `${each.name}(${each.parentId.name})`,
                        value: JSON.stringify(each)
                    };
                })})
        });

        this.appointmentService.getAllSuppliers(JSON.parse(localStorage.getItem('daym-user')).role === 4, JSON.parse(localStorage.getItem('daym-user')).role === 4 ).then(response => {
            this.setState({allSuppliers: response.data.map((each)=>{
                    return {
                        label: each.name,
                        value: each._id
                    };
                })})
        });
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if(this.state.locale !== localStorage.getItem('i18nextLng')){
            this.setState({allType: [
                    {
                        label: this.props.t('daily'),
                        value: 1
                    },
                    {
                        label: this.props.t('weekly'),
                        value: 2
                    }
                ], locale: localStorage.getItem('i18nextLng')})
        }
    }

    validate(){
        if(!this.state.supplier){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('supplier') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }else if(this.state.ordersApiList.length === 0){
            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('orders') + ' ' + this.props.t('can_not_be_empty') });
            return false;
        }else if(this.state.type === 1){
            if(!this.state.deliveryDate){
                this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('delivery_date') + ' ' + this.props.t('can_not_be_empty') });
                return false;
            }
        }else if(this.state.type === 2){
            if(!this.state.startDate){
                this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('startDate') + ' ' + this.props.t('can_not_be_empty') });
                return false;
            }else if(!this.state.endDate){
                this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('endDate') + ' ' + this.props.t('can_not_be_empty') });
                return false;
            }
        }
        return true;
    }

    handleCreateHeadQuarter(){
        if(this.validate()) {
            this.setState({buttonLoading: true});
            this.appointmentService.createAppointment(this.state.supplier, this.state.ordersApiList,
                this.state.deliveryDate,this.state.type,
                JSON.parse(localStorage.getItem('daym-user')).role === 4 ? 2 : 1 ,this.state.type === 2 ? {
                startDate: this.state.startDate,
                endDate: this.state.endDate,
            } : {}).then(async (response)=>{
                this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: this.props.t('appointment') + ' ' + this.props.t('created_Successfully') });
                if(response.data.length > 0){
                    this.setState({
                        dataTableValue: [...response.data,...this.state.dataTableValue],
                    })
                }else {
                    this.setState({
                        dataTableValue: [response.data,...this.state.dataTableValue],
                    })
                }
                this.setState({
                    display:false,
                    buttonLoading: false,
                })

            }).catch((err)=>{
                let error = err.response.data.message;
                this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('can_not_create') + ' ' + this.props.t('appointment') });
                this.setState({buttonLoading: false});
            });
        }
    }

    render() {
        const statusValues = [ this.props.t('initiated'),this.props.t('pending'),this.props.t('changed_supplier'),
            this.props.t('confirmed_supplier'), this.props.t('scheduled'),this.props.t('accept_security'),
            this.props.t('reject_security'),this.props.t('truck_enter'),
            this.props.t('complete'),this.props.t('cancelled')];
        let datatableBrands = statusValues.map((each, index)=>{
            if (each === this.props.t('cancelled'))
                return {label: each, value: -1};
            else
                return {label: each, value: index};
        });

        let brandFilter = <Dropdown style={{width: '100%'}} placeholder={this.props.t('select_type')} value={this.state.datatableBrand} options={datatableBrands} onChange={(event)=>{
            this.dt.filter(event.value, 'status', 'equals');
            this.setState({datatableBrand: event.value});
        }}/>
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateHeadQuarter} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        const tableHeader = (
            <div style={{display: 'flex',justifyContent: 'flex-end'}}>
                <Dialog header={(this.props.t('add'))+' '+this.props.t('appointment')} visible={this.state.display} modal={true} style={{width: '70vw'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid">
                        <div className="p-grid p-col-12 p-md-5 p-fluid">
                            <div className="p-col-12 p-md-12">
                                <Dropdown style={{width: '100%'}} placeholder={this.props.t('appointment_type')}
                                          value={this.state.type}
                                          options={this.state.allType}
                                          onChange={(event)=>{
                                              this.setState({
                                                  type: event.target.value,
                                                  automatic: event.target.value !== 2
                                              });
                                          }}/>
                            </div>
                            <div className="p-col-12 p-md-12">
                                <Dropdown style={{width: '100%'}} placeholder={JSON.parse(localStorage.getItem('daym-user')).role === 4 ?this.props.t('user'):this.props.t('supplier')}
                                          value={this.state.supplier}
                                          options={this.state.allSuppliers}
                                          onChange={(event)=>{
                                              this.setState({supplier: event.target.value});
                                          }}/>
                            </div>
                            {
                                this.state.type === 1 ?
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
                                        }} placeholder="Delivery Date" value={this.state.deliveryDate} onChange={(e) => this.setState({deliveryDate: e.value,deliveryTime: e.value})}/>
                                    </div> :
                                    <>
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
                                            }} placeholder="Start Date" value={this.state.startDate} onChange={(e) => this.setState({startDate: e.value,deliveryTime: e.value})}/>
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
                                            }} placeholder="End Date" value={this.state.endDate} onChange={(e) => this.setState({endDate: e.value})}/>
                                        </div>
                                    </>
                            }
                        </div>
                        <div className="p-grid p-col-12 p-md-7">
                            <DataTable ref={(el) => this.dt = el} value={this.state.ordersList} selectionMode="single" header={
                                <div style={{display: 'flex',justifyContent: 'flex-end'}}>
                                    <Dialog header={this.props.t('add_order')} visible={this.state.display1} modal={true} style={{width: '30vw'}} footer={
                                        <div>
                                            <Button icon="pi pi-times" onClick={() => this.setState({display1:false, selectedProduct: null, selectedDock: null, quantity: ''})} label={this.props.t('cancel')} className="p-button-secondary" />
                                            <Button icon="pi pi-check" onClick={()=>{
                                                if(this.state.selectedProduct && this.state.quantity && this.state.selectedDock && this.state.deliveryTime){
                                                    let _orders = this.state.ordersList;
                                                    let _ordersApi = this.state.ordersApiList;
                                                    _orders.push({
                                                        product: JSON.parse(this.state.selectedProduct).name,
                                                        quantity: this.state.quantity,
                                                        dock: JSON.parse(this.state.selectedDock).name
                                                    });
                                                    _ordersApi.push({
                                                        product: JSON.parse(this.state.selectedProduct)._id,
                                                        quantity: this.state.quantity,
                                                        dock: JSON.parse(this.state.selectedDock)._id,
                                                        ...(this.state.automatic ? {} : {deliveryTime: this.state.deliveryTime}),
                                                    });
                                                    this.setState({
                                                        selectedProduct: null, quantity: '', selectedDock: null, ordersList: _orders, ordersApiList: _ordersApi, display1: false
                                                    });
                                                }else {
                                                    this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('all_fields_mandatory')});
                                                    return false;
                                                }

                                            }} label={this.props.t('save')} />
                                        </div>
                                    } onHide={() => this.setState({display1:false, quantity: '', selectedDock: null, selectedProduct: null})}>
                                        <div className="p-grid">
                                            <div className="p-col-12 p-md-12">
                                                <Dropdown style={{width: '100%'}} placeholder={this.props.t('product')} value={this.state.selectedProduct} options={this.state.allProducts} onChange={(event)=>{
                                                    const selectedProduct = JSON.parse(event.value);
                                                    this.setState({selectedProduct: event.value, selectedDocks: this.state.allDocks.filter((e)=>
                                                            JSON.parse(e.value).productTypes.indexOf(selectedProduct.productType._id) >= 0
                                                    )});
                                                }}/>
                                            </div>
                                            <div className="p-col-12 p-md-12">
                                                <InputText placeholder={this.props.t('quantity')} style={{width: '100%'}} value={this.state.unitTagId} onChange={(e) => this.setState({quantity: e.target.value})} type={'number'} min={1}/>
                                            </div>
                                            <div className="p-col-12 p-md-12">
                                                <Dropdown style={{width: '100%'}} placeholder={this.props.t('dock')} value={this.state.selectedDock} options={this.state.selectedDocks} onChange={(event)=>this.setState({selectedDock: event.value})}/>
                                            </div>
                                            {
                                                this.state.type === 1 && <div className="p-col-12 p-md-12">
                                                    <h5>{this.props.t('automatic_time')}</h5>
                                                    <InputSwitch checked={this.state.automatic} onChange={event => this.setState({automatic: event.value})} />
                                                </div>
                                            }
                                            {
                                                !this.state.automatic &&
                                                <div className="p-col-12 p-md-12">
                                                    <Calendar placeholder="Time" timeOnly={true} showTime={true} value={this.state.deliveryTime} onChange={(e) => {
                                                        // console.log('Delivery Time ----> ', e.value);
                                                        this.setState({deliveryTime: e.value});
                                                    }}/>
                                                </div>
                                            }
                                        </div>
                                    </Dialog>
                                    <Button type="button" label={this.props.t('add_order')} icon="pi pi-cog" onClick={() => {
                                        if(!(this.state.deliveryDate || this.state.startDate)){
                                            this.growl.show({ severity: 'warn', summary: this.props.t('validation_failed'), detail: this.props.t('add_deliverydate')});
                                            return;
                                        }
                                        this.setState({display1: true});
                                    }}/>
                                </div>
                            } paginator={true} rows={10}
                                       responsive={true} selection={this.state.ordersList1} onSelectionChange={event => this.setState({ordersList1: event.value})}>
                                <Column field="product" header={this.props.t('name')} sortable={true} />
                                <Column field="quantity" header={this.props.t('quantity')} sortable={true} />
                                <Column field="dock" header={this.props.t('dock')} sortable={true} />
                                <Column header={this.props.t('delete')} body={(rowData)=>
                                    <Button type="button" icon="pi pi-times" className="p-button-danger" style={{marginRight: '.5em'}} onClick={()=>{
                                        let _ordersList = this.state.ordersList;
                                        let _ordersAPIList = this.state.ordersApiList;
                                        _ordersList.splice(_ordersList.indexOf(rowData),1);
                                        _ordersAPIList.splice(_ordersList.indexOf(rowData),1);
                                        this.setState({ordersApiList: _ordersAPIList,ordersList: _ordersList});
                                    }}/>
                                } style={{textAlign:'center', width: '8em'}}/>
                            </DataTable>
                        </div>
                    </div>
                </Dialog>
                {
                    this.state.user.role < 5 && this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_appointment')} icon="pi pi-cog" onClick={() => this.setState({display: true, hqName: '', hqMetaName: '', editData: null})}/>
                }
            </div>
        );
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('appointments')}</h1>{
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
                                <Column body={(row) => row.supplier.name} header={this.props.t('supplier')}/>
                                <Column body={(row) => row.user ? row.user.name : row.userType.name} header={this.props.t('appointment_for')}/>
                                <Column body={(row) => row.orderCount + ' ' + this.props.t('products')}
                                        header={this.props.t('products')}/>
                                <Column body={(row) => row.status !== -1 ? statusValues[row.status] : this.props.t('cancelled')}
                                        header={this.props.t('status')} field="status" filter={true}
                                        filterElement={brandFilter}/>
                                {
                                    this.state.user.role < 5 &&
                                    <Column header={this.props.t('view_details')} body={this.viewGate}
                                            style={{textAlign: 'center'}}/>
                                }
                            </DataTable>
                    }
                    </div>
                </div>
            </div>
        );
    }
}

export default withTranslation()(Appointments);
