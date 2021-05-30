import React, { Component } from 'react';
import './Documentation.css';
import {CodeHighlight} from "./CodeHighlight";

export class Documentation extends Component {

    constructor() {
        super();
        this.state = {};
    }

    render() {
        return (
            <div className="p-grid">
                <div className="p-col-12">
                    <div className="card docs no-margin">
                        <h1>Current Version</h1>
                        <p>React 16.13.0 and PrimeReact 4.x</p>

                        <h1>Getting Started</h1>
                        <p>Roma is an application template for React based on the popular <a href="https://github.com/facebookincubator/create-react-app">create-react-app</a> that allows
                            creating React apps with no configuration. To get started extract the contents of the zip bundle and install the dependencies
                            with npm or yarn.</p>
                        <pre>
{
`npm install

# OR

yarn
`}
</pre>

                        <p>Next step is running the application using the start script and navigate to <b>http://localhost:3000/</b> to view the application.
                            That is it, you may now start with the development of your application using the Roma template.</p>

                        <pre>
{
`npm start

# OR

yarn start
`}
</pre>

                        <h1>React Scripts</h1>
                        <p>Following commands are derived from create-app-app.</p>
                        <pre>
{
`"npm start" or "yarn start": Starts the development server
"npm test" or "yarn test": Runs the tests.
"npm run build" or "yarn run build": Creates a production build.
`}
</pre>

                        <h1>Dependencies</h1>
                        <p>Dependencies of Roma are listed below and needs to be added to package.json. Roma has no direct dependency, even PrimeReact components are an optional dependency.</p>

<pre>
{
`"primereact": "^3.4.0",              //optional: PrimeReact components
"primeicons": "^2.0.0",              //optional: PrimeReact component icons
"primeflex": "1.0.0",                //optional: Samples
"react-router-dom": "^4.2.2"         //optional: Router
`
}
</pre>

                        <h1>Structure</h1>
                        <p>Roma consists of 3 main parts; the application layout, layout resources and theme resources for PrimeReact components. <b>App.js</b> inside src folder is the main component containing the template for the base layout
                            whereas required resources for the layout are placed inside the <b>public/assets/layout</b> folder and similarly theme resources are inside <b>public/assets/theme</b> folder.
                        </p>

                        <h1>Template</h1>
                        <p>Main layout is the JSX of the App.js, it is divided into a couple of child components such as topbar, profile, menu and footer. Here is render method of the
                            App.js component that implements the logic such as menu state, layout modes and so on.
                        </p>

                        <CodeHighlight>
{
`
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
                                <img src="assets/layout/images/avatar.png" alt="roma-layout"/>
                                <span className="layout-profile-userinfo">
                                    <span className="layout-profile-name">Arlene Welch</span>
                                    <span className="layout-profile-role">Design Ops</span>
                                </span>
                            </button>
                            <ul className={classNames("layout-profile-menu", {'profile-menu-active':this.state.inlineUserMenuActive})}>
                                <li>
                                    <button className="p-link">
                                        <i className="pi pi-fw pi-user"></i><span>Profile</span>
                                    </button>
                                </li>
                                <li>
                                    <button className="p-link">
                                        <i className="pi pi-fw pi-cog"></i><span>Settings</span>
                                    </button>
                                </li>
                                <li>
                                    <button className="p-link">
                                        <i className="pi pi-fw pi-envelope"></i><span>Messages</span>
                                    </button>
                                </li>
                                <li>
                                    <button className="p-link">
                                        <i className="pi pi-fw pi-bell"></i><span>Notifications</span>
                                    </button>
                                </li>
                            </ul>
                        </div>
                    }
                    <AppMenu model={this.menu} onMenuItemClick={this.onMenuItemClick} onRootMenuItemClick={this.onRootMenuItemClick}
                            layoutMode={this.state.layoutMode} active={this.state.menuActive} />
                </div>
            </div>

            <AppRightMenu rightPanelMenuActive={this.state.rightPanelMenuActive} onRightMenuClick={this.onRightMenuClick}></AppRightMenu>

            <div className="layout-main">
                <div className="layout-content">
                    <Route path="/" exact component={Dashboard} />
                    <Route path="/forms" component={FormsDemo} />
                    <Route path="/sample" component={SampleDemo} />
                    <Route path="/data" component={DataDemo} />
                    <Route path="/panels" component={PanelsDemo} />
                    <Route path="/overlays" component={OverlaysDemo} />
                    <Route path="/menus" component={MenusDemo} />
                    <Route path="/messages" component={MessagesDemo} />
                    <Route path="/charts" component={ChartsDemo} />
                    <Route path="/misc" component={MiscDemo} />
                    <Route path="/empty" component={EmptyPage} />
                    <Route path="/documentation" component={Documentation} />
                </div>

                <AppConfig layoutMode={this.state.layoutMode} lightMenu={this.state.lightMenu} inlineUser={this.state.inlineUser} isRTL={this.state.isRTL}
                           themeColor={this.state.themeColor} topbarColor={this.state.topbarColor} changeMenuMode={this.changeMenuMode} changeMenuColor={this.changeMenuColor}
                           changeProfileMode={this.changeProfileMode} changeOrientation={this.changeOrientation} changeTopbarColor={this.changeTopbarColor} changeTheme={this.changeTheme}
                           onConfigButtonClick={this.onConfigButtonClick} onConfigCloseClick={this.onConfigCloseClick} onConfigClick={this.onConfigClick} configDialogActive={this.state.configDialogActive}/>
            </div>

            <AppFooter />

            <div className="layout-content-mask"></div>
        </div>
    );
}
`
}
</CodeHighlight>

                        <h1>Menu</h1>
                        <p>Menu is a separate component defined in AppMenu.js file based on PrimeReact MenuModel API. In order to define the menuitems,
                            navigate to createMenu() method App.js file and define your own model as a nested structure. Here is the menu component from the demo application.
                            Notice that menu object is bound to the model property of AppMenu component as shown above.</p>
<div style={{overflow: 'auto', height: '400px'}}>
<CodeHighlight lang="js">
{
`
createMenu() {
    this.menu = [
        { label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/'},
        {
            label: 'Layouts', icon: 'pi pi-fw pi-th-large',
            items: [
                { label: 'Static', icon: 'pi pi-fw pi-bars', command: (event) => this.changeMenuMode({ originalEvent: event, menuMode: 'static' }) },
                { label: 'Overlay', icon: 'pi pi-fw pi-bars', command: (event) => this.changeMenuMode({ originalEvent: event, menuMode: 'overlay' }) },
                { label: 'Slim', icon: 'pi pi-fw pi-bars', command: (event) => this.changeMenuMode({ originalEvent: event, menuMode: 'slim' }) },
                { label: 'Horizontal', icon: 'pi pi-fw pi-bars', command: (event) => this.changeMenuMode({ originalEvent: event, menuMode: 'horizontal' }) },
                {
                    label: 'Orientation', icon: 'pi pi-fw pi-align-right',
                    items: [
                        {label: 'LTR', icon: 'pi pi-fw pi-align-left', command: (event) => this.changeOrientation({ originalEvent: event, isRTL: false }) },
                        {label: 'RTL', icon: 'pi pi-fw pi-align-right', command: (event) => this.changeOrientation({ originalEvent: event, isRTL: true })  }
                    ]
                }
            ]
        },
        {
            label: 'Topbar Colors', icon: 'pi pi-fw pi-pencil',
            items: [
                {
                    label: 'Light', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-light', logo:'logo-roma'})
                },
                {
                    label: 'Dark', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-dark', logo:'logo-roma-white'})
                },
                {
                    label: 'Blue', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-blue', logo:'logo-roma-white'})
                },
                {
                    label: 'Green', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-green', logo:'logo-roma-white'})
                },
                {
                    label: 'Orange', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-orange', logo:'logo-roma-white'})
                },
                {
                    label: 'Magenta', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-magenta', logo:'logo-roma-white'})
                },
                {
                    label: 'Blue Grey', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-bluegrey', logo:'logo-roma-white'})
                },
                {
                    label: 'Deep Purple', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-deeppurple', logo:'logo-roma-white'})
                },
                {
                    label: 'Brown', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-brown', logo:'logo-roma-white'})
                },
                {
                    label: 'Lime', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-lime', logo:'logo-roma-white'})
                },
                {
                    label: 'Rose', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-rose', logo:'logo-roma-white'})
                },
                {
                    label: 'Cyan', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-cyan', logo:'logo-roma-white'})
                },
                {
                    label: 'Teal', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-teal', logo:'logo-roma-white'})
                },
                {
                    label: 'Deep Orange', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-deeporange', logo:'logo-roma-white'})
                },
                {
                    label: 'Indigo', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-indigo', logo:'logo-roma-white'})
                },
                {
                    label: 'Pink', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-pink', logo:'logo-roma-white'})
                },
                {
                    label: 'Purple', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTopbarColor({originalEvent: event, topbarColor:'layout-topbar-purple', logo:'logo-roma-white'})
                }
            ]
        },
        {
            label: 'Menu Colors', icon: 'pi pi-fw pi-list',
            items: [
                { label: 'Light', icon: 'pi pi-fw pi-circle-off', command: (event) => this.changeMenuColor({ originalEvent: event, lightMenu: true }) },
                { label: 'Dark', icon: 'pi pi-fw pi-circle-on', command: (event) => this.changeMenuColor({ originalEvent: event, lightMenu: false }) }
            ]
        },
        {
            label: 'User Profile', icon: 'pi pi-fw pi-user',
            items: [
                { label: 'Popup', icon: 'pi pi-fw pi-user', command: (event) => this.changeProfileMode({ originalEvent: event, inlineUser: false })},
                { label: 'Inline', icon: 'pi pi-fw pi-user', command: (event) => this.changeProfileMode({ originalEvent: event, inlineUser: true })}
            ]
        },
        {
            label: 'Theme', icon: 'pi pi-fw pi-pencil',
            items: [
                {
                    label: 'Blue', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'blue'})
                },
                {
                    label: 'Green', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'green'})
                },
                {
                    label: 'Orange', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'orange'})
                },
                {
                    label: 'Magenta', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'magenta'})
                },
                {
                    label: 'Blue Grey', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'bluegrey'})
                },
                {
                    label: 'Deep Purple', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'deeppurple'})
                },
                {
                    label: 'Brown', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'brown'})
                },
                {
                    label: 'Lime', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'lime'})
                },
                {
                    label: 'Rose', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'rose'})
                },
                {
                    label: 'Cyan', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'cyan'})
                },
                {
                    label: 'Teal', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'teal'})
                },
                {
                    label: 'Deep-Orange', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'deeporange'})
                },
                {
                    label: 'Indigo', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'indigo'})
                },
                {
                    label: 'Pink', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'pink'})
                },
                {
                    label: 'Purple', icon: 'pi pi-fw pi-pencil',
                    command: (event) => this.changeTheme({originalEvent: event, theme:'purple'})
                }
            ]
        },
        {
            label: 'Components', icon: 'pi pi-fw pi-star',
            items: [
                { label: 'Sample Page', icon: 'pi pi-fw pi-th-large', to: '/sample'  },
                { label: 'Forms', icon: 'pi pi-fw pi-file', to: '/forms' },
                { label: 'Data', icon: 'pi pi-fw pi-table', to: '/data' },
                { label: 'Panels', icon: 'pi pi-fw pi-list', to: '/panels' },
                { label: 'Overlays', icon: 'pi pi-fw pi-clone', to: '/overlays' },
                { label: 'Menus', icon: 'pi pi-fw pi-plus', to: '/menus' },
                { label: 'Messages', icon: 'pi pi-fw pi-envelope', to: '/messages' },
                { label: 'Charts', icon: 'pi pi-fw pi-chart-bar', to: '/charts' },
                { label: 'Misc', icon: 'pi pi-fw pi-spinner', to: '/misc' }
            ]
        },
        {
            label: 'Pages', icon: 'pi pi-fw pi-copy',
            items: [
                { label: 'Empty', icon: 'pi pi-fw pi-clone', to: '/empty'},
                { label: 'Login', icon: 'pi pi-fw pi-sign-in', to: '/login'},
                { label: 'Landing', icon: 'pi pi-fw pi-globe', command: ()=> window.open('assets/pages/landing.html', '_blank')},
                { label: this.props.t('error'), icon: 'pi pi-fw pi-exclamation-triangle', to: '/error'},
                { label: '404', icon: 'pi pi-fw pi-times', to: '/404'},
                {
                    label: 'Access Denied', icon: 'pi pi-fw pi-ban',
                    to: '/accessdenied'
                }
            ]
        },
        {
            label: 'Hierarchy', icon: 'pi pi-fw pi-sitemap',
            items: [
                {
                    label: 'Submenu 1', icon: 'pi pi-fw pi-sign-in',
                    items: [
                        {
                            label: 'Submenu 1.1', icon: 'pi pi-fw pi-sign-in',
                            items: [
                                { label: 'Submenu 1.1.1', icon: 'pi pi-fw pi-sign-in' },
                                { label: 'Submenu 1.1.2', icon: 'pi pi-fw pi-sign-in' },
                                { label: 'Submenu 1.1.3', icon: 'pi pi-fw pi-sign-in' },
                            ]
                        },
                        {
                            label: 'Submenu 1.2', icon: 'pi pi-fw pi-sign-in',
                            items: [
                                { label: 'Submenu 1.2.1', icon: 'pi pi-fw pi-sign-in' }
                            ]
                        },
                    ]
                },
                {
                    label: 'Submenu 2', icon: 'pi pi-fw pi-sign-in',
                    items: [
                        {
                            label: 'Submenu 2.1', icon: 'pi pi-fw pi-sign-in',
                            items: [
                                { label: 'Submenu 2.1.1', icon: 'pi pi-fw pi-sign-in' },
                                { label: 'Submenu 2.1.2', icon: 'pi pi-fw pi-sign-in' },
                            ]
                        },
                        {
                            label: 'Submenu 2.2', icon: 'pi pi-fw pi-sign-in',
                            items: [
                                { label: 'Submenu 2.2.1', icon: 'pi pi-fw pi-sign-in' },
                            ]
                        },
                    ]
                }
            ]
        },
        {
            label: 'Docs', icon: 'pi pi-fw pi-file', to: '/documentation'
        },
        {
            label: 'Buy Now', icon: 'pi pi-fw pi-money-bill',command: ()=> window.open('https://www.primefaces.org/store', '_blank')
        }
    ];
}
    
`}
</CodeHighlight>
</div>

                        <h1>Theme and Layout SASS</h1>
                        <p>Roma provides 15 PrimeReact themes out of the box, setup of a theme is simple as including the css of theme to your application. All themes are located inside are located inside public/assets/theme folder.</p>

                        <ul>
                            <li>blue</li>
                            <li>green</li>
                            <li>orange</li>
                            <li>magenta</li>
                            <li>bluegrey</li>
                            <li>deeppurple</li>
                            <li>brown</li>
                            <li>lime</li>
                            <li>rose</li>
                            <li>cyan</li>
                            <li>teal</li>
                            <li>deeporange</li>
                            <li>indigo</li>
                            <li>pink</li>
                            <li>purple</li>
                        </ul>

                        <p>A custom theme can be developed by the following steps.</p>
                        <ul>
                            <li>Choose a custom theme name such as theme-myown.</li>
                            <li>Create a file named theme-myown.scss under <i>public/assets/theme folder</i>.</li>
                            <li>Define the variables listed below and import the <i>../sass/theme/_theme_variables</i> and <i>../sass/theme_core/_core</i> file.</li>
                            <li>Build the scss to generate css and add it to your application manually
                                 or include the scss file to your App.js so that it will be compiled and included automatically.</li>
                        </ul>

                        <p>Here are the variables required to create a sample theme.</p>

<CodeHighlight lang="css">
{
`
$primaryDarkestColor:#024f9e;
$primaryDarkerColor:#0772b3;
$primaryColor:#0f97c7;
$primaryLighterColor:#1cb9d7;
$primaryLightestColor:#2ed7e4;
$primaryColorText:#ffffff;

@import '../sass/theme/_theme_variables';
@import '../sass/theme_core/_core'; 

`
}
</CodeHighlight>

                        <p> An example sass command to compile the css would be;</p>

<pre>
sass public/assets/theme/theme-myown.scss:public/assets/theme/theme-myown.css
</pre>

                        <p>Watch mode is handy to avoid compiling everytime when a change is made, instead use the following command
                            so that sass generates the file whenever you make a customization. This builds all css files whenever a change is made to any scss file.</p>
<pre>
sass -w public/assets:public/assets
</pre>

                        <p>Same can also be applied to layout itself;</p>
                        <ul>
                            <li>Choose a layout name such as layout-myown.</li>
                            <li>Create an empty file named layout-myown.scss inside <i>assets/layout/css</i> folder.</li>
                            <li>Define the variables listed below and import the <i>../../sass/layout/_layout</i> file.</li>
                            <li>Build the scss to generate css and add it to your application manually
                                 or include the scss file to your App.js so that it will be compiled and included automatically.</li>
                        </ul>

                        <p>Here are the variables required to create a layout.</p>

<CodeHighlight lang="css">
{
`
$primaryColor:#0f97c7;
$primaryTextColor:#ffffff;

@import '../../sass/layout/_layout';

`
}
</CodeHighlight>

                        <h1>Common SASS Variables</h1>
                        <p>In case you'd like to customize the shared variables, the _variables.scss files are where the options are defined for layout and theme.</p>

                        <h3>sass/_variables.scss</h3>
<CodeHighlight lang="css">
{
`
$fontFamily:'Inter UI',sans-serif;
$fontSize:13px;
$textColor:#252529;
$textSecondaryColor:#65656a;
$borderRadius:2px;
$transitionDuration:.2s;

`
}
</CodeHighlight>
                        <h3>sass/layout/_variables</h3>
<CodeHighlight lang="css">
{
`
@import '../_variables';

//main
$bodyBgColor:#f4f4f4;

$footerBgColor:#ffffff;
$footerBorderColor:#ebebef;

//light menu
$menuBgColor:#ffffff;
$menuBorderColor:#ebebef;
$menuitemTextColor:#666666;
$menuitemIconColor:#65656a;
$menuitemTextHoverColor:#252529;
$menuitemIconHoverColor:#252529;
$menuitemHoverBgColor:#eaeaea;
$menuitemSeparator:#ebebef;

//dark menu
$darkMenuBgColor:#252529;
$darkMenuBorderColor:#252529;
$darkMenuitemTextColor:#8b8b90;
$darkMenuitemIconColor:#a6a6a6;
$darkMenuitemTextHoverColor:#ebebef;
$darkMenuitemIconHoverColor:#ebebef;
$darkMenuitemHoverBgColor:#2e2e33;
$darkMenuitemSeparator:#424247;

$slimMenuTooltipBgColor:#333333;
$slimMenuTooltipTextColor:#c8c8c8;
`
}
</CodeHighlight>

                        <h3>sass/theme/_theme_variables.scss</h3>

<div style={{'height': '400px', 'overflow': 'auto'}}>
<CodeHighlight lang="css">
{
`
@import '../_variables';

//Global
$primeIconFontSize:1.25em;

//anchors
$linkColor:$primaryColor;
$linkHoverColor:$primaryDarkerColor;
$linkActiveColor:$primaryDarkestColor;

//highlight
$highlightBgColor:$primaryColor;
$highlightColorText:$primaryColorText;

//input field (e.g. inputtext, spinner, inputmask)
$inputPadding:.5em;
$inputBgColor:#ffffff;
$inputBorder:1px solid #a9a9ae;
$inputHoverBorderColor:#525257;
$inputFocusBorderColor:$primaryColor;
$inputFocusShadow:0 0 0 0.2em $primaryLightestColor;
$inputErrorBorder:1px solid #e0284f;
$inputPlaceholderTextColor:$textSecondaryColor;
$inputTransition:border-color $transitionDuration,box-shadow $transitionDuration;

//input groups
$inputGroupBorderColor:#a9a9ae;
$inputGroupBgColor:#ffffff;
$inputGroupTextColor:$textColor;

//input lists (e.g. dropdown, autocomplete, multiselect, orderlist)
$inputListMinWidth:12em;
$inputListBgColor:#ffffff;
$inputListPadding:.65em 0;
$inputListBorder:1px solid #a9a9ae;

$inputListItemPadding:.5em .75em;
$inputListItemBgColor:transparent;
$inputListItemTextColor:$textColor;
$inputListItemHoverBgColor:#eaeaea;
$inputListItemHoverTextColor:$primaryColor;
$inputListItemHighlightBgColor:$highlightBgColor;
$inputListItemHighlightTextColor:$highlightColorText;
$inputListItemBorder:0 none;
$inputListItemMargin:0;

$inputListItemDividerColor:#ffffff;
$inputListHeaderPaddingTop:.5em;
$inputListHeaderPaddingLeft:.75em;
$inputListHeaderPaddingRight:.75em;
$inputListHeaderPaddingBottom:.5em;
$inputListHeaderMargin:0;
$inputListHeaderBgColor:#ffffff;
$inputListHeaderTextColor:$textColor;
$inputListHeaderBorder:1px solid #d8d8dc;

$inputListHeaderSearchIconColor:$textSecondaryColor;
$inputListHeaderCloseIconColor:$textColor;
$inputListHeaderCloseIconHoverColor:$primaryColor;
$inputListHeaderCloseIconTransition:color $transitionDuration;

//inputs with panels (e.g. password)
$inputContentPanelPadding:.75em;
$inputContentPanelBgColor:#ffffff;
$inputContentPanelTextColor:$textColor;

//inputs with overlays (e.g. autocomplete, dropdown, multiselect)
$inputOverlayBorder:0 none;
$inputOverlayShadow:0 0 10px 0 rgba(0, 0, 0, 0.16);

//input dropdowns (e.g. multiselect, dropdown)
$inputDropdownIconColor:$textSecondaryColor;

//button
$buttonTextOnlyPadding:.5em 1em;
$buttonWithLeftIconPadding:.5em 1em .5em 2em;
$buttonWithRightIconPadding:.5em 2em .5em 1em;
$buttonIconOnlyPadding:.5em;
$buttonIconOnlyWidth:2.143em;

$buttonBgColor:$primaryColor;
$buttonBorder:1px solid $primaryColor;
$buttonTextColor:$primaryColorText;

$buttonHoverBgColor:$primaryLighterColor;
$buttonHoverTextColor:$primaryColorText;
$buttonHoverBorderColor:$primaryLighterColor;

$buttonActiveBgColor:$primaryDarkerColor;
$buttonActiveTextColor:$primaryColorText;
$buttonActiveBorderColor:$primaryDarkerColor;

$buttonFocusOutline:0 none;
$buttonFocusOutlineOffset:0px;
$buttonFocusShadow:0 0 0 0.2em $primaryLightestColor;
$buttonTransition:background-color $transitionDuration,box-shadow $transitionDuration;
$raisedButtonShadow:0 2px 4px 0 rgba(0, 0, 0, 0.10);
$roundedButtonBorderRadius:1em;

$secondaryButtonBgColor:$primaryColorText;
$secondaryButtonBorder:1px solid $primaryColor;
$secondaryButtonTextColor:$primaryColor;
$secondaryButtonHoverBgColor:$primaryLighterColor;
$secondaryButtonHoverTextColor:$primaryColorText;
$secondaryButtonHoverBorderColor:$primaryLighterColor;
$secondaryButtonActiveBgColor:$primaryDarkerColor;
$secondaryButtonActiveTextColor:$primaryColorText;
$secondaryButtonActiveBorderColor:$primaryDarkerColor;
$secondaryButtonFocusShadow:$buttonFocusShadow;

$infoButtonBgColor:#3f8efc;
$infoButtonTextColor:#ffffff;
$infoButtonBorder:1px solid #3f8efc;
$infoButtonHoverBgColor:#72ADFF;
$infoButtonHoverTextColor:#ffffff;
$infoButtonHoverBorderColor:#72ADFF;
$infoButtonActiveBgColor:#0c54b8;
$infoButtonActiveTextColor:#ffffff;
$infoButtonActiveBorderColor:#0c54b8;
$infoButtonFocusShadow:0 0 0 0.2em #6fd4ff;

$successButtonBgColor:#0fc763;
$successButtonTextColor:#ffffff;
$successButtonBorder:1px solid #0fc763;
$successButtonHoverBgColor:#1DDB76;
$successButtonHoverTextColor:#ffffff;
$successButtonHoverBorderColor:#1DDB76;
$successButtonActiveBgColor:#0a9c4d;
$successButtonActiveTextColor:#ffffff;
$successButtonActiveBorderColor:#0a9c4d;
$successButtonFocusShadow:0 0 0 0.2em #72ffb3;

$warningButtonBgColor:#f5b064;
$warningButtonTextColor:#ffffff;
$warningButtonBorder:1px solid #f5b064;
$warningButtonHoverBgColor:#ffbf3c;
$warningButtonHoverTextColor:#ffffff;
$warningButtonHoverBorderColor:#ffbf3c;
$warningButtonActiveBgColor:#cb7d27;
$warningButtonActiveTextColor:#ffffff;
$warningButtonActiveBorderColor:#cb7d27;
$warningButtonFocusShadow:0 0 0 0.2em #fad78e;

$dangerButtonBgColor:#f56f64;
$dangerButtonTextColor:#ffffff;
$dangerButtonBorder:1px solid #f56f64;
$dangerButtonHoverBgColor:#ff4545;
$dangerButtonHoverTextColor:#ffffff;
$dangerButtonHoverBorderColor:#ff4545;
$dangerButtonActiveBgColor:#a8281e;
$dangerButtonActiveTextColor:#ffffff;
$dangerButtonActiveBorderColor:#a8281e;
$dangerButtonFocusShadow:0 0 0 0.2em #ff4545;

//checkbox
$checkboxWidth:20px;
$checkboxHeight:20px;
$checkboxActiveBorderColor:$primaryColor;
$checkboxActiveBgColor:$primaryColor;
$checkboxActiveTextColor:$primaryColorText;
$checkboxActiveHoverBgColor:$primaryLighterColor;
$checkboxActiveHoverTextColor:$primaryColorText;
$checkboxActiveHoverBorderColor:$primaryColor;
$checkboxActiveFocusBgColor:$primaryColor;
$checkboxActiveFocusTextColor:$primaryColorText;
$checkboxActiveFocusBorderColor:$primaryColor;
$checkboxFocusBgColor:$inputBgColor;
$checkboxFocusTextColor:$primaryColor;
$checkboxFocusBorderColor:$inputBorder;
$checkboxFocusShadow:0 0 0 0.2em $primaryLightestColor;
$checkboxTransition:background-color $transitionDuration, border-color $transitionDuration, box-shadow $transitionDuration;

//radiobutton
$radiobuttonWidth:20px;
$radiobuttonHeight:20px;
$radiobuttonActiveBorderColor:$primaryColor;
$radiobuttonActiveBgColor:$primaryColor;
$radiobuttonActiveTextColor:$primaryColorText;
$radiobuttonActiveHoverBgColor:$primaryLighterColor;
$radiobuttonActiveHoverTextColor:$primaryColorText;
$radiobuttonActiveHoverBorderColor:$primaryColor;
$radiobuttonActiveFocusBgColor:$primaryColor;
$radiobuttonActiveFocusTextColor:$primaryColorText;
$radiobuttonActiveFocusBorderColor:$primaryColor;
$radiobuttonFocusBgColor:$inputBgColor;
$radiobuttonFocusTextColor:$primaryColor;
$radiobuttonFocusBorderColor:$inputBorder;
$radiobuttonFocusShadow:0 0 0 0.2em $primaryLightestColor;
$radiobuttonTransition:background-color $transitionDuration, border-color $transitionDuration, box-shadow $transitionDuration;

//togglebutton
$toggleButtonBgColor:#eaeaea;
$toggleButtonBorder:1px solid #eaeaea;
$toggleButtonTextColor:$textColor;
$toggleButtonIconColor:$textColor;
$toggleButtonHoverBgColor:#eaeaea;
$toggleButtonHoverBorderColor:#eaeaea;
$toggleButtonHoverTextColor:$primaryColor;
$toggleButtonHoverIconColor:$primaryColor;
$toggleButtonActiveBgColor:$primaryColor;
$toggleButtonActiveBorderColor:$primaryColor;
$toggleButtonActiveTextColor:$primaryColorText;
$toggleButtonActiveIconColor:$primaryColorText;
$toggleButtonActiveHoverBgColor:$primaryLighterColor;
$toggleButtonActiveHoverBorderColor:$primaryLighterColor;
$toggleButtonActiveHoverTextColor:$primaryColorText;
$toggleButtonActiveHoverIconColor:$primaryColorText;
$toggleButtonFocusOutline:$buttonFocusShadow;
$toggleButtonFocusBgColor:#eaeaea;
$toggleButtonFocusBorderColor:#eaeaea;
$toggleButtonFocusTextColor:$primaryColor;
$toggleButtonFocusIconColor:$primaryColor;
$toggleButtonActiveFocusBgColor:$primaryLighterColor;
$toggleButtonActiveFocusBorderColor:$primaryLighterColor;
$toggleButtonActiveFocusTextColor:$primaryColorText;
$toggleButtonActiveFocusIconColor:$primaryColorText;

//inplace
$inplacePadding:.5em;
$inplaceHoverBgColor:#ebebef;
$inplaceHoverTextColor:$textColor;
$inplaceTransition:background-color $transitionDuration;

//rating
$ratingTransition:color $transitionDuration;
$ratingCancelIconColor:#e0284f;
$ratingCancelHoverIconColor:#e0284f;
$ratingIconFontSize:1.5em;
$ratingStarIconColor:$textColor;
$ratingStarIconHoverColor:$primaryColor;

//slider
$sliderBgColor:#d8d8dc;
$sliderBorder:0 none;
$sliderHeight:.286em;
$sliderWidth:0.286em;
$sliderHandleWidth:1.5em;
$sliderHandleHeight:1.5em;
$sliderHandleBgColor:$primaryColor;
$sliderHandleBorder:2px solid $primaryColor;
$sliderHandleBorderRadius:50%;
$sliderHandleHoverBorder:2px solid $primaryColor;
$sliderHandleHoverBgColor:$primaryLighterColor;
$sliderHandleTransition:background-color $transitionDuration;
$sliderRangeBgColor:$primaryColor;

//calendar
$calendarWidth:20em;
$calendarNavIconColor:$textSecondaryColor;
$calendarNavIconHoverColor:$textColor;
$calendarNavIconTransition:color $transitionDuration;
$calendarPadding:0.857em;
$calendarTableMargin:0.857em 0 0 0;
$calendarHeaderCellPadding:.5em;
$calendarCellDatePadding:.5em;
$calendarCellDateHoverBgColor:#ebebef;
$calendarCellDateBorderRadius:$borderRadius;
$calendarCellDateSelectedBgColor:$primaryColor;
$calendarCellDateSelectedTextColor:$primaryColorText;
$calendarCellDateTodayBgColor:$primaryColor;
$calendarCellDateTodayTextColor:$primaryColorText;
$calendarTimePickerDivider: 1px solid #ebebef;
$calendarTimePickerPadding:.75em 1.25em .75em .75em;
$calendarTimePickerIconColor:$textSecondaryColor;
$calendarTimePickerIconFontSize:1.286em;
$calendarTimePickerTimeFontSize:1.286em;
$calendarTimePickerIconHoverColor:$textColor;
$calendarButtonBarDivider: 1px solid #ebebef;
$calendarMultipleMonthDivider: 1px solid #ebebef;

//spinner
$spinnerButtonWidth:1.5em;

//input switch
$inputSwitchWidth:2.615em;
$inputSwitchHeight:1.077em;
$inputSwitchBorderRadius:30px;
$inputSwitchTransition:background-color $transitionDuration;
$inputSwitchSliderOffBgColor:$inputBgColor;
$inputSwitchHandleOffBgColor:$textSecondaryColor;
$inputSwitchSliderOffHoverBgColor:$inputBgColor;
$inputSwitchSliderOffFocusBgColor:$inputBgColor;
$inputSwitchSliderOnBgColor:$primaryColor;
$inputSwitchSliderOnHoverBgColor:$primaryColor;
$inputSwitchHandleOnBgColor:$primaryDarkerColor;
$inputSwitchSliderOnFocusBgColor:$primaryColor;

//panel common (e.g. accordion, panel, tabview)
$panelHeaderBorder:1px solid #d8d8dc;
$panelHeaderBgColor:#ffffff;
$panelHeaderTextColor:$textColor;
$panelHeaderIconColor:$textSecondaryColor;
$panelHeaderIconHoverColor:$textColor;
$panelHeaderIconTransition:color $transitionDuration;
$panelHeaderFontWeight:500;
$panelHeaderPadding:.857em 1em;
$panelContentBorder:1px solid #d8d8dc;
$panelContentBgColor:#ffffff;
$panelContentTextColor:$textColor;
$panelContentPadding:0.571em 1em;
$panelContentLineHeight:1.5;
$panelFooterBorder:1px solid #d8d8dc;
$panelFooterBgColor:#ffffff;
$panelFooterTextColor:$textColor;
$panelFooterPadding:0.571em 1em;
$panelHeaderHoverBgColor:#eaeaea;
$panelHeaderHoverBorder:1px solid #d8d8dc;
$panelHeaderHoverTextColor:$textColor;
$panelHeaderHoverIconColor:$textColor;
$panelHeaderActiveBgColor:#ffffff;
$panelHeaderActiveBorder:1px solid #d8d8dc;
$panelHeaderActiveTextColor:$primaryColor;
$panelHeaderActiveIconColor:$primaryColor;
$panelHeaderActiveHoverBgColor:#eaeaea;
$panelHeaderActiveHoverBorder:1px solid #d8d8dc;
$panelHeaderActiveHoverTextColor:$primaryColor;
$panelHeaderActiveHoverIconColor:$primaryColor;
$panelHeaderTransition:background-color $transitionDuration, box-shadow $transitionDuration;

//accordion
$accordionSpacing:2px;

//tabview
$tabsNavBorder:0 none;
$tabsNavBgColor:#ffffff;
$tabHeaderSpacing:.214em;

//scrollpanel
$scrollPanelHandleBgColor:#dadada;
$scrollPanelTrackBorder:0 none;
$scrollPanelTrackBgColor:#f8f8f8;

//card
$cardShadow:0 1px 3px 0 rgba(0, 0, 0, 0.2), 0 1px 1px 0 rgba(0, 0, 0, 0.14), 0 2px 1px -1px rgba(0, 0, 0, 0.12);

//paginator
$paginatorBgColor:#ffffff;
$paginatorBorder:1px solid #d8d8dc;
$paginatorPadding:.25em 0;
$paginatorIconColor:$textSecondaryColor;
$paginatorElementWidth:2.286em;
$paginatorElementHeight:2.286em;
$paginatorElementHoverBgColor:#eaeaea;
$paginatorElementHoverIconColor:$textColor;
$paginatorElementBorderRadius:$borderRadius;
$paginatorElementMargin:0 .125em;
$paginatorElementPadding:0;
$paginatorElementBorder:1px solid transparent;

//table
$tableCaptionFontWeight:500;
$tableSummaryFontWeight:500;
$tableHeaderCellPadding:.75em 1.214em;
$tableHeaderCellBgColor:#ffffff;
$tableHeaderCellTextColor:$textColor;
$tableHeaderCellFontWeight:500;
$tableHeaderCellBorder:1px solid #d8d8dc;
$tableHeaderCellHoverBgColor:#eaeaea;
$tableHeaderCellHoverTextColor:$textColor;
$tableHeaderCellIconColor:$textColor;
$tableHeaderCellHoverIconColor:$textColor;
$tableBodyRowBgColor:#f8f8f8;
$tableBodyRowTextColor:$textColor;
$tableBodyRowEvenBgColor:#ffffff;
$tableBodyRowHoverBgColor:#eaeaea;
$tableBodyRowHoverTextColor:$textColor;
$tableBodyCellBorder:1px solid #d8d8dc;
$tableBodyCellPadding:.75em 1.214em;
$tableFooterCellPadding:.75em 1.214em;
$tableFooterCellBgColor:#ffffff;
$tableFooterCellTextColor:$textColor;
$tableFooterCellFontWeight:500;
$tableFooterCellBorder:1px solid #d8d8dc;
$tableResizerHelperBgColor:$primaryColor;

//schedule
$scheduleEventBgColor:$primaryColor;
$scheduleEventBorder:1px solid $primaryColor;
$scheduleEventTextColor:$primaryColorText;

//tree
$treeNodePadding:0.143em 0;
$treeNodeLabelPadding:0.286em;
$treeNodeContentSpacing:0.143em;

//lightbox
$lightBoxNavIconFontSize:3em;
$lightBoxNavIconColor:#ffffff;

//org chart
$organizationChartConnectorColor:#c8c8c8;

//messages
$messagesMargin:1em 0;
$messagesPadding:1em;
$messagesIconFontSize:1.714em;
$messageCloseIconFontSize:1.5em;

//message
$messagePadding:$inputPadding;
$messageMargin: 0;
$messageIconFontSize: 1.25em;
$messageTextFontSize: 1em;

//toast
$toastShadow: 0 3px .5em 0 rgba(0, 0, 0, 0.16);
$toastMessageMargin:0 0 1em 0;

//severities
$infoMessageBgColor:#1cb9d7;
$infoMessageBorder:1px solid #1cb9d7;
$infoMessageTextColor:#ffffff;
$infoMessageIconColor:#ffffff;
$successMessageBgColor:#34CE83;
$successMessageBorder:1px solid #34CE83;
$successMessageTextColor:#ffffff;
$successMessageIconColor:#ffffff;
$warnMessageBgColor:#f5b064;
$warnMessageBorder:1px solid #f5b064;
$warnMessageTextColor:#ffffff;
$warnMessageIconColor:#ffffff;
$errorMessageBgColor:#f56f64;
$errorMessageBorder:1px solid #f56f64;
$errorMessageTextColor:#ffffff;
$errorMessageIconColor:#ffffff;

//growl
$growlTopLocation:70px;
$growlIconFontSize:3.5em;
$growlMessageTextMargin: 0 0 0 4em;
$growlMargin:0 0 1em 0;
$growlPadding:1em;
$growlShadow:0 3px .5em 0 rgba(0, 0, 0, 0.16);
$growlOpacity:.9;

//overlays
$overlayContentBorderColor:#c8c8c8;
$overlayContentBorder:0 none;
$overlayContainerShadow:0 0 10px 0 rgba(0, 0, 0, 0.16);

//dialog
$dialogHeaderPadding:1.5em;
$confirmDialogPadding:1.5em;

//overlay panel
$overlayPanelCloseIconBgColor:$primaryColor;
$overlayPanelCloseIconColor:$primaryColorText;
$overlayPanelCloseIconHoverBgColor:$primaryLighterColor;
$overlayPanelCloseIconHoverColor:$primaryColorText;

//tooltip
$tooltipBgColor:#252529;
$tooltipTextColor:#ffffff;
$tooltipPadding:$inputPadding;

//steps
$stepsItemBgColor:#ffffff;
$stepsItemBorder:1px solid #d8d8dc;
$stepsItemNumberColor:$textColor;
$stepsItemTextColor:$textSecondaryColor;
$stepsItemActiveBorder:1px solid $primaryColor;
$stepsItemWidth:2em;
$stepsItemHeight:2em;

//progressbar
$progressBarHeight:1.714em;
$progressBarBorder:0 none;
$progressBarBgColor:#efefef;
$progressBarValueBgColor:$primaryColor;

//menu (e.g. menu, menubar, tieredmenu)
$menuBgColor:#ffffff;
$menuBorder:1px solid #d8d8dc;
$menuPadding:0;
$menuTextColor:$textColor;
$menuitemPadding:.571em .857em;
$menuitemMargin:0;
$menuitemTextColor:$textColor;
$menuitemIconColor:$textSecondaryColor;
$menuitemHoverTextColor:$textColor;
$menuitemHoverIconColor:$textColor;
$menuitemHoverBgColor:#eaeaea;
$menuitemActiveTextColor:$primaryColorText;
$menuitemActiveIconColor:$primaryColorText;
$menuitemActiveBgColor:$primaryColor;
$submenuHeaderMargin: 0;
$submenuPadding: 0;
$overlayMenuBorder:0 none;
$overlayMenuShadow:0 0px 10px 0 rgba(0, 0, 0, 0.16);

//misc
$maskBgColor:rgba(0, 0, 0, 0.4);        //dialog mask
$inlineSpacing:.5em;                      //spacing between inline elements
$chipsItemMargin:0 .286em 0 0;            //autocomplete and chips token spacing
$dataIconColor:$textSecondaryColor;       //icon color of a data such as treetoggler, table expander
$dataIconHoverColor:$textColor;           //hover icon color of a data such as treetoggler, table expander

//general
$disabledOpacity:.5;                      //opacity of a disabled item

//carousel
$carouselNavButtonsBgColor: #ffffff;
$carouselNavButtonsBorder: solid 1px rgba(178, 193, 205, 0.64);
$carouselNavButtonsBorderRadius: 50%;
$carouselNavButtonsMargin: .2em;
$carouselNavButtonsColor: $textColor;
$carouselNavButtonsHoverBgColor: #ffffff;
$carouselNavButtonsHoverColor: $primaryColor;
$carouselNavButtonsHoverBorderColor: solid 1px rgba(178, 193, 205, 0.64);
$carouselNavButtonsTransition: color $transitionDuration;
$carouselDotIconWidth: 20px;
$carouselDotIconHeight: 6px;
$carouselDotIconBgColor: #b2c1cd;
$carouselDotIconMargin: 0 .2em;
$carouselActiveDotIconBgColor: $primaryColor;
`
}
</CodeHighlight>
</div>

                        <p>In the demo app layout and theme css files are defined using link tags in index.html so the demo can switch them on the fly by changing the path however if this is not a requirement, you may also import them in App.js so that webpack adds them to the bundle.</p>

                        <h1>Menu Modes</h1>
                        <p>Menu has 4 modes, static, overlay, slim and horizontal. Layout container element in app.component.html is used to define which mode to use by adding specific classes. List
                        below indicates the style classes for each mode. </p>

                        <ul>
                            <li>Static: "layout-wrapper layout-static"</li>
                            <li>Overlay: "layout-wrapper layout-overlay"</li>
                            <li>Slim: "layout-wrapper layout-slim"</li>
                            <li>Horizontal: "layout-wrapper layout-horizontal"</li>
                        </ul>

                        <p>For example to create a horizontal menu, the div element should be in following form;</p>
<pre>
&lt;div class="layout-wrapper layout-horizontal"&gt;
</pre>

                        <p>It is also possible to leave the choice to the user by keeping the preference at a component and using an expression to bind it so that user can switch between modes. Sample
                            application has an example implementation of such use case. Refer to App.js for an example.</p>

                        <h1>Menu Colors</h1>
                        <p>Menu offers two color options, "light" and "dark" which is defined using the main container element.</p>
                        <ul>
                            <li>Light: "layout-wrapper layout-menu-light"</li>
                            <li>Dark: "layout-wrapper layout-menu-dark"</li>
                        </ul>

                        <h1>TopBar Colors</h1>
                        <p>Roma provides 17 built-in color alternatives for the topbar which is defined by adding a style class to the "layout-wrapper" element such as "layout-topbar-dark".</p>

<pre>
&lt;div class="layout-wrapper layout-topbar-dark"&gt;
...
&lt;/div&gt;
</pre>
                        <p>The full list of alternatives are;</p>
                        <ul>
                            <li>layout-topbar-light</li>
                            <li>layout-topbar-dark</li>
                            <li>layout-topbar-blue</li>
                            <li>layout-topbar-green</li>
                            <li>layout-topbar-orange</li>
                            <li>layout-topbar-magenta</li>
                            <li>layout-topbar-bluegrey</li>
                            <li>layout-topbar-deeppurple</li>
                            <li>layout-topbar-brown</li>
                            <li>layout-topbar-lime</li>
                            <li>layout-topbar-rose</li>
                            <li>layout-topbar-cyan</li>
                            <li>layout-topbar-teal</li>
                            <li>layout-topbar-deeporange</li>
                            <li>layout-topbar-indigo</li>
                            <li>layout-topbar-pink</li>
                            <li>layout-topbar-purple</li>
                        </ul>

                        <h1>PrimeFlex Grid System</h1>
                        <p>Roma uses PrimeFlex Grid System throughout the samples, although any Grid library can be used we suggest using PrimeFlex as your grid system as it is well tested and supported by PrimeReact. PrimeFlex is
                        available at npm and defined at package.json of Roma so that it gets installed by default.</p>

                        <h1>Customizing Styles</h1>
                        <p>It is suggested to write your customizations in <i>sass/overrides/_layout_styles.scss</i> and <i>sass/overrdies/_theme_styles.scss </i> files for seamless updates
                        as these files are empty by default and never updated.</p>

                        <h1>Migration Guide</h1>
                        <p>4.0.0 to 4.0.1</p>
                        <ul>
                            <li>Update App.js</li>
                            <li>Update layout css files</li>
                            <li>Update theme css files</li>
                        </ul>

                        <p>1.0.0 to 4.0.0</p>
                        <ul>
                            <li>Update index.jx</li>
                            <li>Update App.js</li>
                            <li>Update AppMenu.js</li>
                            <li>Update layout css files</li>
                            <li>Update theme css files</li>
                        </ul>
                    </div>
                </div>
            </div>
        )
    }
}
