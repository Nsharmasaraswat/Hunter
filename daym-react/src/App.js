import React, { Component } from 'react';
import classNames from 'classnames';
import AppTopbar from './AppTopbar';
import { AppFooter } from './AppFooter';
import { AppMenu } from './AppMenu';
import { AppConfig } from './AppConfig';
import { AppRightMenu } from './AppRightMenu';
import { Route } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import 'primereact/resources/primereact.min.css';
import '@fullcalendar/core/main.css';
import '@fullcalendar/daygrid/main.css';
import '@fullcalendar/timegrid/main.css';
import 'primeicons/primeicons.css';
import 'primeflex/primeflex.css';
import './App.css';
import Gates from "./components/Gates";
import Docks from "./components/Docs";
import HeadQuaters from './components/HeadQuaters';
import WareHouses from './components/WareHouses';
import UserGroup from './components/UserType';
import UserFields from './components/UserFields';
import ProductFields from './components/ProductFields';
import ProductModel from './components/ProductType';
import Products from './components/Products';
import ManageAdmin from './components/ManageAdmin';
import ManageSecurity from './components/ManageSecurity';
import Users from "./components/Users";
import MangeHoliday from "./components/ManageHoliday";
import ViewHoliday from "./components/ViewHoliday";
import UserPermission from "./components/UserPermission";
import Supplier from "./components/Supplier";
import Driver from "./components/Driver";
import AllDrivers from "./components/AllDrivers";
import {DataDemo} from "./components/DataDemo";
import {FormsDemo} from "./components/FormsDemo";
import Appointments from "./components/Appointments";
import TrucksOvertime from "./components/TrucksOvertime";
import ManageAppointments from "./components/ManageAppointments";
import {SampleDemo} from "./components/SampleDemo";
import {PanelsDemo} from "./components/PanelsDemo";
import {OverlaysDemo} from "./components/OverlaysDemo";
import {MenusDemo} from "./components/MenusDemo";
import {MessagesDemo} from "./components/MessagesDemo";
import {ChartsDemo} from "./components/ChartsDemo";
import {MiscDemo} from "./components/MiscDemo";
import {EmptyPage} from "./components/EmptyPage";
import AllAppointments from "./components/AllAppointments";
import Truck from "./components/Truck";
import TruckFields from "./components/TruckFields";
import SupplierTruck from "./components/SupplierTruck";
import AppointmentCalendar from "./components/AppointmentCalendar";

class App extends Component {

    constructor() {
        super();
        this.state = {
            layoutMode: 'slim',
            lightMenu: true,
            overlayMenuActive: false,
            staticMenuDesktopInactive: false,
            staticMenuMobileActive: false,
            isRTL: false,
            topbarColor: 'layout-topbar-blue',
            inlineUser: false,
            topbarMenuActive: false,
            activeTopbarItem: null,
            rightPanelMenuActive: null,
            inlineUserMenuActive: false,
            menuActive: false,
            themeColor: 'blue',
            configDialogActive: false,
            user: JSON.parse(localStorage.getItem('daym-user'))
        };

        this.onDocumentClick = this.onDocumentClick.bind(this);
        this.onMenuClick = this.onMenuClick.bind(this);
        this.onMenuButtonClick = this.onMenuButtonClick.bind(this);
        this.onTopbarMenuButtonClick = this.onTopbarMenuButtonClick.bind(this);
        this.onTopbarItemClick = this.onTopbarItemClick.bind(this);
        this.onMenuItemClick = this.onMenuItemClick.bind(this);
        this.onRootMenuItemClick = this.onRootMenuItemClick.bind(this);
        this.onRightMenuButtonClick = this.onRightMenuButtonClick.bind(this);
        this.onRightMenuClick = this.onRightMenuClick.bind(this);
        this.onProfileMenuClick = this.onProfileMenuClick.bind(this);
        this.changeMenuMode = this.changeMenuMode.bind(this);
        this.changeMenuColor = this.changeMenuColor.bind(this);
        this.changeProfileMode = this.changeProfileMode.bind(this);
        this.changeOrientation = this.changeOrientation.bind(this);
        this.changeTopbarColor = this.changeTopbarColor.bind(this);
        this.changeTheme = this.changeTheme.bind(this);
        this.onConfigButtonClick = this.onConfigButtonClick.bind(this);
        this.onConfigCloseClick = this.onConfigCloseClick.bind(this);
        this.onConfigClick = this.onConfigClick.bind(this);
        this.createMenu = this.createMenu.bind(this);
        this.createMenu();
    }

    onDocumentClick(event) {
        if(!this.topbarItemClick) {
            this.setState({
                activeTopbarItem: null,
                topbarMenuActive: false
            });
        }

        if (!this.rightMenuClick) {
            this.setState({rightPanelMenuActive: false});
        }

        if (!this.configClick) {
            this.setState({configDialogActive: false});
        }

        if (!this.profileClick && this.isSlim() && !this.isMobile()) {
            this.setState({inlineUserMenuActive: false})
        }

        if(!this.menuClick) {
            if(this.isHorizontal() || this.isSlim()) {
                this.setState({
                    menuActive: false
                })
            }

            if (this.state.overlayMenuActive || this.state.staticMenuMobileActive) {
                this.hideOverlayMenu();
            }

            this.setState({menuHoverActive: false});
            this.unblockBodyScroll();
        }

        this.topbarItemClick = false;
        this.menuClick = false;
        this.rightMenuClick = false;
        this.profileClick = false;
        this.configClick = false;
    }
    onMenuButtonClick(event) {
        this.menuClick = true;
        this.setState(({
            topbarMenuActive: false,
            rightPanelMenuActive: false
        }));

        if(this.isOverlay()) {
            this.setState({
                overlayMenuActive: !this.state.overlayMenuActive
            });
        }

        if(this.isDesktop())
            this.setState({staticMenuDesktopInactive: !this.state.staticMenuDesktopInactive});
        else {
            this.setState({staticMenuMobileActive: !this.state.staticMenuMobileActive});
            if (this.state.staticMenuMobileActive) {
                this.blockBodyScroll();
            } else {
                this.unblockBodyScroll();
            }
        }

        event.preventDefault();
    }

    onConfigButtonClick(event){
        this.configClick = true;
        this.setState({configDialogActive: !this.state.configDialogActive})
    }

    onConfigCloseClick(){
        this.setState({configDialogActive: false})
    }

    onConfigClick(){
        this.configClick = true;
    }

    onTopbarMenuButtonClick(event) {
        this.topbarItemClick = true;
        this.setState({topbarMenuActive: !this.state.topbarMenuActive});
        this.hideOverlayMenu();
        event.preventDefault();
    }

    onTopbarItemClick(event) {
        this.topbarItemClick = true;

        if(this.state.activeTopbarItem === event.item)
            this.setState({activeTopbarItem: null});
        else
            this.setState({activeTopbarItem: event.item});

        event.originalEvent.preventDefault();
    }
    onMenuClick(event) {
        this.menuClick = true;
    }

    blockBodyScroll() {
        if (document.body.classList) {
            document.body.classList.add('blocked-scroll');
        } else {
            document.body.className += ' blocked-scroll';
        }
    }

    unblockBodyScroll() {
        if (document.body.classList) {
            document.body.classList.remove('blocked-scroll');
        } else {
            document.body.className = document.body.className.replace(new RegExp('(^|\\b)' +
                'blocked-scroll'.split(' ').join('|') + '(\\b|$)', 'gi'), ' ');
        }
    }
    onRightMenuButtonClick(event){
        this.rightMenuClick = true;
        this.setState({rightPanelMenuActive: !this.state.rightPanelMenuActive});

        this.hideOverlayMenu();

        event.preventDefault();
    }

    onRightMenuClick(event){
        this.rightMenuClick = true;
    }

    onProfileMenuClick(event) {
        this.profileClick = true;
        this.setState({inlineUserMenuActive: !this.state.inlineUserMenuActive})
    }

    hideOverlayMenu() {
        this.setState({
            overlayMenuActive: false,
            staticMenuMobileActive: false
        })
    }
    onMenuItemClick(event) {
        if(!event.item.items) {
            this.hideOverlayMenu();
        }
        if(!event.item.items && (this.isHorizontal() || this.isSlim())) {
            this.setState({
                menuActive: false
            })
        }
    }

    onRootMenuItemClick(event) {
        this.setState({
            menuActive: !this.state.menuActive
        });
    }

    isTablet() {
        const width = window.innerWidth;
        return width <= 1024 && width > 640;
    }

    isDesktop() {
        return window.innerWidth > 896;
    }

    isMobile() {
        return window.innerWidth <= 1025;
    }

    isStatic() {
        return this.state.layoutMode === 'static';
    }

    isOverlay() {
        return this.state.layoutMode === 'overlay';
    }

    isHorizontal() {
        return this.state.layoutMode === 'horizontal';
    }

    isSlim() {
        return this.state.layoutMode === 'slim';
    }

    changeMenuMode(event) {
        this.setState({
            layoutMode : event.menuMode,
            staticMenuDesktopInactive: false,
            overlayMenuActive: false
        });
        if(event.menuMode === 'slim' || event.menuMode === 'horizontal') {
            this.setState({
                inlineUser: false,
                inlineUserMenuActive: false
            })
        }
    }

    changeMenuColor(event) {
        this.setState({lightMenu : event.lightMenu})
    }

    changeProfileMode(event) {
        if(!event.inlineUser) {
            this.setState({
                inlineUser: event.inlineUser,
                inlineUserMenuActive: event.inlineUser
            })
        }
        else {
            if(!this.isHorizontal()) {
                this.setState({
                    inlineUser: event.inlineUser
                })
            }
        }
    }

    changeOrientation(event) {
        this.setState({isRTL: event.isRTL})
    }

    changeTopbarColor(event) {
        this.setState({topbarColor : event.topbarColor});
        const topbarLogoLink = document.getElementById('topbar-logo');
        topbarLogoLink.src = 'assets/layout/images/' + event.logo + '.svg';
    }

    changeTheme(event) {
        this.setState({themeColor: event.theme})
        this.changeStyleSheetUrl('layout-css',event.theme, 'layout');
        this.changeStyleSheetUrl('theme-css', event.theme, 'theme');
    }

    changeStyleSheetUrl(id, value, prefix) {
        let element = document.getElementById(id);
        let urlTokens = element.getAttribute('href').split('/');
        urlTokens[urlTokens.length - 1] = prefix + '-' + value + '.css';
        let newURL = urlTokens.join('/');

        this.replaceLink(element, newURL);
    }

    isIE() {
        return /(MSIE|Trident\/|Edge\/)/i.test(window.navigator.userAgent)
    }

    replaceLink(linkElement, href) {
        if(this.isIE()){
            linkElement.setAttribute('href', href);
        }
        else {
            const id = linkElement.getAttribute('id');
            const cloneLinkElement = linkElement.cloneNode(true);

            cloneLinkElement.setAttribute('href', href);
            cloneLinkElement.setAttribute('id', id + '-clone');

            linkElement.parentNode.insertBefore(cloneLinkElement, linkElement.nextSibling);

            cloneLinkElement.addEventListener('load', () => {
                linkElement.remove();
                cloneLinkElement.setAttribute('id', id);
            });
        }
    }


    createMenu() {
        // console.log('User ===',this.state.user);
      //
      // console.log(this.state.user.permissions);
    }

    render() {
        const layoutClassName = classNames('layout-wrapper', {
            'layout-horizontal': this.state.layoutMode === 'horizontal',
            'layout-overlay': this.state.layoutMode === 'overlay',
            'layout-static': this.state.layoutMode === 'static',
            'layout-slim': this.state.layoutMode === 'slim',
            'layout-menu-light': this.state.lightMenu === true,
            'layout-menu-dark': this.state.lightMenu === false,
            'layout-overlay-active': this.state.overlayMenuActive,
            'layout-mobile-active': this.state.staticMenuMobileActive,
            'layout-static-inactive': this.state.staticMenuDesktopInactive,
            'layout-rtl': this.state.isRTL
        }, this.state.topbarColor);

        return (
            <div className={layoutClassName} onClick={this.onDocumentClick}>
                <AppTopbar topbarMenuActive={this.state.topbarMenuActive} activeTopbarItem={this.state.activeTopbarItem} inlineUser={this.state.inlineUser}
                           onRightMenuButtonClick={this.onRightMenuButtonClick} onMenuButtonClick={this.onMenuButtonClick}
                           onTopbarMenuButtonClick={this.onTopbarMenuButtonClick} onTopbarItemClick={this.onTopbarItemClick} />

                <div className='layout-menu-container' onClick={this.onMenuClick}>
                    <div className="menu-scroll-content">
                        {
                            this.state.inlineUser && <div className="layout-profile">
                                <button className="p-link layout-profile-button" onClick={this.onProfileMenuClick}>
                                    <img src={this.state.user.avatar} alt="roma-layout"/>
                                    <span className="layout-profile-userinfo">
                                        <span className="layout-profile-name">{this.state.user.name}</span>
                                        <span className="layout-profile-role">{this.state.user.userType.name}</span>
                                    </span>
                                </button>
                                <ul className={classNames("layout-profile-menu", {'profile-menu-active':this.state.inlineUserMenuActive})}>
                                    <li>
                                        <button className="p-link" onClick={()=>{
                                            localStorage.removeItem('daym-user');
                                            localStorage.removeItem('daym-tok');
                                            window.location.reload();
                                        }}>
                                            <i className="pi pi-fw pi-cog"/><span>Logout</span>
                                        </button>
                                    </li>
                                </ul>
                            </div>
                        }
                        <AppMenu model={this.state.user.permissions.map((each)=>{
                          const locales = {
                            sp : 'spanish',
                            pr : 'portuguese',
                            en : 'name'
                          };
                          return { label: each[locales[localStorage.getItem('i18nextLng')]], icon: each.icon , to: each.route, drawer: each.drawer}
                        }).filter(each => each.drawer !== false)} onMenuItemClick={this.onMenuItemClick} onRootMenuItemClick={this.onRootMenuItemClick}
                                layoutMode={this.state.layoutMode} active={this.state.menuActive} />
                    </div>
                </div>

                <AppRightMenu rightPanelMenuActive={this.state.rightPanelMenuActive} onRightMenuClick={this.onRightMenuClick}/>

                <div className="layout-main">
                    <div className="layout-content">
                        <Route path="/" exact component={Dashboard} />
                        <Route path="/locations" component={HeadQuaters} />
                        <Route path="/warehouse" component={WareHouses} />
                        <Route path="/dock" component={Docks} />
                        <Route path="/gate" component={Gates} />
                        <Route path="/manage-holiday" component={MangeHoliday} />
                        <Route path="/view-holiday" component={ViewHoliday} />
                        {/*<Route path="/unit" component={Units} />*/}
                        <Route path="/user-type" component={UserGroup} />
                        <Route path="/user-field" component={UserFields} />
                        <Route path="/product-field" component={ProductFields} />
                        <Route path="/product-type" component={ProductModel} />
                        <Route path="/products" component={Products} />
                        <Route path="/users" component={Users} />
                        <Route path="/user-permission" component={UserPermission} />
                        <Route path="/supplier" component={Supplier} />
                        <Route path="/driver" component={Driver} />
                        <Route path="/all-drivers" component={AllDrivers} />

                        <Route path="/manage-admins" component={ManageAdmin} />
                        <Route path="/manage-security" component={ManageSecurity} />
                        <Route path="/appointments" component={Appointments} />
                        <Route path="/manage-appointment" component={ManageAppointments} />
                        <Route path="/today-appointments" component={AllAppointments} />

                        <Route path="/data" component={DataDemo} />
                        <Route path="/forms" component={FormsDemo} />
                        <Route path="/sample" component={SampleDemo} />
                        <Route path="/panels" component={PanelsDemo} />
                        <Route path="/overlays" component={OverlaysDemo} />
                        <Route path="/menus" component={MenusDemo} />
                        <Route path="/messages" component={MessagesDemo} />
                        <Route path="/charts" component={ChartsDemo} />
                        <Route path="/misc" component={MiscDemo} />
                        <Route path="/empty" component={EmptyPage} />

                        <Route path="/trucks" component={Truck} />
                        <Route path="/supplier-truck" component={SupplierTruck} />
                        <Route path="/truck-field" component={TruckFields} />
                        <Route path="/trucks-overtime" component={TrucksOvertime} />

                        <Route path="/appointment-calendar" component={AppointmentCalendar} />
                        {/*<Route path="/documentation" component={Documentation} />*/}
                    </div>
                    <AppConfig layoutMode={this.state.layoutMode} lightMenu={this.state.lightMenu} inlineUser={this.state.inlineUser} isRTL={this.state.isRTL}
                               themeColor={this.state.themeColor} topbarColor={this.state.topbarColor} changeMenuMode={this.changeMenuMode} changeMenuColor={this.changeMenuColor}
                               changeProfileMode={this.changeProfileMode} changeOrientation={this.changeOrientation} changeTopbarColor={this.changeTopbarColor} changeTheme={this.changeTheme}
                               onConfigButtonClick={this.onConfigButtonClick} onConfigCloseClick={this.onConfigCloseClick} onConfigClick={this.onConfigClick} configDialogActive={this.state.configDialogActive}/>
                </div>

                <AppFooter />

                <div className="layout-content-mask"/>
            </div>
        );
    }
}

export default App;
