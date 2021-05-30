import React, { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import {Dropdown} from "primereact/dropdown";
import i18next from "i18next";
import {InputText} from "primereact/inputtext";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {ProgressSpinner} from "primereact/progressspinner";
import {UsersService} from "./service/UsersService";
import {withTranslation} from "react-i18next";
import {Growl} from "primereact/growl";

class AppTopbar extends Component {

    static defaultProps = {
        onMenuButtonClick: null,
        onTopbarMenuButtonClick: null,
        onTopbarItemClick: null,
        onRightMenuButtonClick: null,
        topbarMenuActive: false,
        activeTopbarItem: null,
        inlineUser: null,
        onThemeChange: null
    }

    static propTypes = {
        onMenuButtonClick: PropTypes.func.isRequired,
        onTopbarMenuButtonClick: PropTypes.func.isRequired,
        onTopbarItemClick: PropTypes.func.isRequired,
        onRightMenuButtonClick: PropTypes.func.isRequired,
        topbarMenuActive: PropTypes.bool.isRequired,
        activeTopbarItem: PropTypes.string,
        inlineUser: PropTypes.bool,
        onThemeChange: PropTypes.func
    }

    constructor() {
        super();
        this.state = {
            languageList: [
                {label: 'English', value: 'en'},
                {label: 'Portuguese', value: 'pr'},
                {label: 'Spanish', value: 'sp'},
            ],
            language: localStorage.getItem('i18nextLng') ?localStorage.getItem('i18nextLng') : 'pr',
            user: JSON.parse(localStorage.getItem('daym-user'))
        };
        this.handleChangePassword = this.handleChangePassword.bind(this);
        this.userService = new UsersService();
    }

    onTopbarItemClick(event, item) {
        if(this.props.onTopbarItemClick) {
            this.props.onTopbarItemClick({
                originalEvent: event,
                item: item
            });
        }
    }

    handleChangePassword() {
        if(this.state.password === this.state.passwordConf) {
            this.setState({buttonLoading: true});
            this.userService.changePassword(this.state.password,this.state.user._id).then(res => {
                this.growl.show({ severity: 'success', summary: this.props.t('error'), detail: this.props.t('password_changed')});
                this.setState({loading: false,passwordOpen: false});
            }).catch((err)=>{
                // console.log('Errrr----> ',err.response.data);
                let error = err && err.response && err.response.data && err.response.data.message ;
                this.growl.show({ severity: 'error', summary: this.props.t('error'), detail: error || this.props.t('some_error')});
            }).finally(()=>{
                this.setState({buttonLoading: false});
            });
        }else {
            this.growl.show({ severity: 'warn', summary: this.props.t('error'), detail: this.props.t('password_mismatch')});
        }
    }

    render() {
        const dialogFooter = (
            <div>
                <Button icon="pi pi-times" onClick={() => this.setState({passwordOpen:false})} label={this.props.t('cancel')} className="p-button-secondary" />
                {
                    !this.state.buttonLoading ?
                        <Button icon="pi pi-check" onClick={this.handleChangePassword} label={this.props.t('save')} /> :
                        <Button label={<ProgressSpinner style={{width: '43px', height: '12px'}} strokeWidth="3" animationDuration=".5s"/>} />
                }
            </div>
        );
        return <div className="layout-topbar">
            <div style={{padding:'12px 20px'}}>
                <Growl ref={(el) => this.growl = el} style={{marginTop: '15px'}} />

                <button className="p-link layout-menu-button layout-topbar-icon" onClick={this.props.onMenuButtonClick}>
                <i className="pi pi-bars"></i>
            </button>

            <button className="p-link layout-topbar-logo" onClick={()=>window.location = "#/"}>
                <img id="topbar-logo" src="assets/layout/images/logo-roma-white.svg" alt="roma-react"/>
            </button>

            <ul className="topbar-menu">
                <li className="user-profile">
                    <div >
                        <p style={{fontSize:10, color: 'white', margin: 'unset'}}>Language</p>
                        <Dropdown style={{marginTop: 4}} options={this.state.languageList} value={this.state.language} onChange={event => {
                            this.setState({language: event.value});
                            i18next.changeLanguage(event.value);
                        }} autoWidth={false} />
                    </div>

                </li>
                <li className={classNames('user-profile', {'active-topmenuitem fadeInDown': this.props.activeTopbarItem === 'profile'})}>
                    { !this.props.inlineUser && <button className="p-link" onClick={(e) => this.onTopbarItemClick(e, 'profile')}>
                            <img src={this.state.user.avatar} alt="roma-layout"/>
                            <div className="layout-profile-userinfo">
                                <span className="layout-profile-name">{this.state.user.name}</span>
                                <span className="layout-profile-role">{this.state.user.userType.name}</span>
                            </div>
                        </button>}

                    <ul className="fadeInDown">
                        <li>
                            <button className="p-link" onClick={()=>{
                                this.setState({passwordOpen: true});
                            }}>
                                <i className="pi pi-fw pi-lock"></i><span>{this.props.t('changePass')}</span>
                            </button>
                        </li>
                        <li>
                            <button className="p-link" onClick={()=>{
                                localStorage.removeItem('daym-user');
                                localStorage.removeItem('daym-tok');
                                window.location.reload();
                            }}>
                                <i className="pi pi-fw pi-cog"></i><span>{this.props.t('logout')}</span>
                            </button>
                        </li>
                    </ul>
                    <Dialog header={this.props.t('changePass')} visible={this.state.passwordOpen} modal={true} style={{width: '30vw'}} footer={dialogFooter} onHide={() => this.setState({passwordOpen:false})}>
                        <div className="p-grid p-fluid">
                            <div className="p-grid p-col-12 p-md-12">
                                <div className="p-col-12 p-md-12">
                                    <InputText type={'password'} placeholder={this.props.t('password')} value={this.state.password} onChange={(e) => this.setState({password: e.target.value})} />
                                </div>
                                <div className="p-col-12 p-md-12">
                                    <InputText type={'password'} placeholder={this.props.t('confirmPass')} value={this.state.passwordConf} onChange={(e) => this.setState({passwordConf: e.target.value})} />
                                </div>
                            </div>
                        </div>
                    </Dialog>
                </li>
            </ul>
            </div>
        </div>;
    }
}

export default withTranslation()(AppTopbar)
