import React, {Component} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column'
import {Button} from 'primereact/button';
import {HeadQuarterService} from "../service/HeadQuarterService";
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Growl} from "primereact/growl";
import {AddressModelService} from "../service/AddressModelService";
import { withTranslation } from 'react-i18next';
import {UnitService} from "../service/UnitService";
import {Dropdown} from "primereact/dropdown";
import i18next from "i18next";
import {ProgressSpinner} from "primereact/progressspinner";

class Units extends Component {

    constructor() {
        super();
        this.state = {
            dataTableValue:[],
            methods: [],
            loading: true,
            buttonLoading: false,
            deleteLoading: false,
            type:{
                'type': 'QRCODE',
                'view': false
            },
            types: [
                {
                    label: 'QRCODE', value: {
                        'type': 'QRCODE',
                        'view': false
                    }
                },
                {
                    label: 'ITF', value: {
                        'type': 'ITF',
                        'view': true
                    }
                }
            ]
        };
        this.responsiveOptions = [
            {
                breakpoint: '1024px',
                numVisible: 3,
                numScroll: 3
            },
            {
                breakpoint: '768px',
                numVisible: 2,
                numScroll: 2
            },
            {
                breakpoint: '560px',
                numVisible: 1,
                numScroll: 1
            }
        ];

        this.unitService = new UnitService();
        this.handleCreateUnit = this.handleCreateUnit.bind(this);
        this.handleDeleteUnit = this.handleDeleteUnit.bind(this);
        this.actionButtons = this.actionButtons.bind(this);
    }

    actionButtons(rowData, column) {
        return <div>
            {
                this.state.methods.indexOf('remove') >= 0 &&
                <Button type="button" icon="pi pi-times" disabled={this.state.deleteLoading} className="p-button-danger"
                        style={{marginRight: '.5em'}} onClick={() => {
                    this.handleDeleteUnit(rowData);
                }}/>
            }
        </div>;
    }

    checkPermission(permissions){
        return permissions.filter((each)=>each.route === '/units').length > 0;
    }

    componentDidMount() {
        let permissions = JSON.parse(localStorage.getItem('daym-user')).permissions;
        if(this.checkPermission(permissions)){
            this.setState({methods: permissions.filter((each)=>each.route === '/units')[0].methods});
        }else {
            window.location = '#/accessdenied';
        }
        this.unitService.getAllUnits().then(response => {
          //console.log('Data ----> ',response.data);
            this.setState({dataTableValue: response.data, loading: false})
        });
    }

    handleCreateUnit(){
        this.setState({buttonLoading: true});
        this.unitService.createUnit(this.state.tagId, this.state.type.type).then(async (response)=>{
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: 'Headquarter created Successfully!' });
            this.setState({
                dataTableValue: [response.data,...this.state.dataTableValue],
                display:false,
                buttonLoading: false,
            })
        }).catch((err)=>{
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: 'Can not create Headquarter' });
            this.setState({buttonLoading: false});
        });
    }

    handleDeleteUnit(rowData){
        this.setState({deleteLoading: true});
        this.unitService.deleteUnit(rowData.id).then( response => {
            let _dataTable = this.state.dataTableValue;
            const position = this.state.dataTableValue.indexOf(rowData);
            _dataTable.splice(position,1);
            this.growl.show({ severity: 'success', summary: this.props.t('success'), detail: 'Headquarter deleted Successfully!' });
            this.setState({
                dataTableValue: _dataTable,
                deleteLoading: false,
            })
        }).catch((err)=>{
            this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: 'Can not delete Headquarter' });
            this.setState({deleteLoading: false});
        });
    }

    render() {
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({display:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleCreateUnit} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        const tableHeader = (
            <div style={{display: 'flex',justifyContent: 'flex-end'}}>
                <Dialog header={(this.state.editData ? this.props.t('edit'): this.props.t('add'))+' '+this.props.t('units')} visible={this.state.display} modal={true} style={{width: '30vw'}} footer={dialogFooter} onHide={() => this.setState({display:false})}>
                    <div className="p-grid">
                        <div className="p-col-12 p-md-12">
                            <Dropdown options={this.state.types} value={this.state.type}
                                      placeholder={this.props.t('type')} style={{width: '100%'}}   onChange={event => {
                                this.setState({type: event.value});
                            }}  autoWidth={false} />
                        </div>
                        {
                            this.state.type.view && <div className="p-col-12 p-md-12">
                                <InputText placeholder={this.props.t('tagId')} style={{width: '100%'}} value={this.state.tagId} onChange={(e) => this.setState({tagId: e.target.value})} />
                            </div>
                        }

                    </div>
                </Dialog>
                {
                    this.state.methods.indexOf('create') >= 0 &&
                    <Button type="button" label={this.props.t('create_unit')} icon="pi pi-cog"
                            onClick={() => this.setState({display: true, hqName: '', hqMetaName: '', editData: null})}/>
                }
            </div>
        );
        let actionHeader = <Button type="button" icon="pi pi-cog"/>;
        return (
            <div className="p-grid">
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />
                <div className="p-col-12">
                    <div className="card card-w-title datatable-demo">
                        <h1>{this.props.t('units')}</h1>
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
                                    <Column field="type" header={this.props.t('type')} sortable={true}/>
                                    <Column field="tagId" header={this.props.t('tagId')} sortable={true}/>
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

export default withTranslation()(Units)
