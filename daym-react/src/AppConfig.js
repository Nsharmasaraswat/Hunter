import React, { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { TabView, TabPanel } from 'primereact/tabview';

export class AppConfig extends Component {


	static defaultProps = {
		layoutMode: 'slim',
		lightMenu: true,
		isRTL: false,
		inlineUser: false,
		topbarColor: 'layout-topbar-blue',
		themeColor: 'blue',
		configDialogActive: false
	}

	static propTypes = {
		layoutMode: PropTypes.string.isRequired,
		lightMenu: PropTypes.bool.isRequired,
		isRTL: PropTypes.bool.isRequired,
		inlineUser: PropTypes.bool.isRequired,
		topbarColor: PropTypes.string.isRequired,
		themeColor: PropTypes.string.isRequired,
		configDialogActive: PropTypes.bool.isRequired
	}

	render() {

		let topbarColors = [
			{name: "Light", topbarColor: "layout-topbar-light", logo: "logo-roma", image: "light.png"},
			{name: "Dark", topbarColor: "layout-topbar-dark", logo: "logo-roma-white", image: "dark.png"},
			{name: "Blue", topbarColor: "layout-topbar-blue", logo: "logo-roma-white", image: "blue.png"},
			{name: "Green", topbarColor: "layout-topbar-green", logo: "logo-roma-white", image: "green.png"},
			{name: "Orange", topbarColor: "layout-topbar-orange", logo: "logo-roma-white", image: "orange.png"},
			{
				name: "Magenta",
				topbarColor: "layout-topbar-magenta",
				logo: "logo-roma-white",
				image: "magenta.png"
			},
			{
				name: "Blue Grey",
				topbarColor: "layout-topbar-bluegrey",
				logo: "logo-roma-white",
				image: "bluegrey.png"
			},
			{
				name: "Deep Purple",
				topbarColor: "layout-topbar-deeppurple",
				logo: "logo-roma-white",
				image: "deeppurple.png"
			},
			{name: "Brown", topbarColor: "layout-topbar-brown", logo: "logo-roma-white", image: "brown.png"},
			{name: "Lime", topbarColor: "layout-topbar-lime", logo: "logo-roma-white", image: "lime.png"},
			{name: "Rose", topbarColor: "layout-topbar-rose", logo: "logo-roma-white", image: "rose.png"},
			{name: "Cyan", topbarColor: "layout-topbar-cyan", logo: "logo-roma-white", image: "cyan.png"},
			{name: "Teal", topbarColor: "layout-topbar-teal", logo: "logo-roma-white", image: "teal.png"},
			{
				name: "Deep Orange",
				topbarColor: "layout-topbar-deeporange",
				logo: "logo-roma-white",
				image: "deeporange.png"
			},
			{name: "Indigo", topbarColor: "layout-topbar-indigo", logo: "logo-roma-white", image: "indigo.png"},
			{name: "Pink", topbarColor: "layout-topbar-pink", logo: "logo-roma-white", image: "pink.png"},
			{name: "Purple", topbarColor: "layout-topbar-purple", logo: "logo-roma-white", image: "purple.png"}
		];

		let themeColors = [
			{name: "Blue", file: "blue", image: "blue.svg"},
			{name: "Blue Grey", file: "bluegrey", image: "bluegrey.svg"},
			{name: "Brown", file: "brown", image: "brown.svg"},
			{name: "Cyan", file: "cyan", image: "cyan.svg"},
			{name: "Deep Orange", file: "deeporange", image: "deeporange.svg"},
			{name: "Deep Purple", file: "deeppurple", image: "deeppurple.svg"},
			{name: "Green", file: "green", image: "green.svg"},
			{name: "Teal", file: "teal", image: "teal.svg"},
			{name: "Indigo", file: "indigo", image: "indigo.svg"},
			{name: "Lime", file: "lime", image: "lime.svg"},
			{name: "Magenta", file: "magenta", image: "magenta.svg"},
			{name: "Orange", file: "orange", image: "orange.svg"},
			{name: "Pink", file: "pink", image: "pink.svg"},
			{name: "Purple", file: "purple", image: "purple.svg"},
			{name: "Rose", file: "rose", image: "rose.svg"},
		];

		return (
			<div className={classNames("layout-config", {'layout-config-active': this.props.configDialogActive})} onClick={this.props.onConfigClick}>
				<div className="layout-config-content">
					<button className="layout-config-button" id="layout-config-button" onClick={this.props.onConfigButtonClick}>
						<i className="pi pi-cog"/>
					</button>

					<button className="layout-config-close" onClick={this.props.onConfigCloseClick}>
						<i className="pi pi-times"/>
					</button>

					<TabView>
						<TabPanel header="Menu" headerClassName="">
							<h1>Menu Modes</h1>
							<div className="panel-items">
								<div className="panel-item">
									<button className="p-link" onClick={event => this.props.changeMenuMode({ originalEvent: event, menuMode: 'static' })}>
										<img src="assets/layout/images/configurator/menu/roma-static.png" alt="roma"/>
										{this.props.layoutMode === 'static' && <i className="pi pi-check"/>}
									</button>
									<span>Static</span>
								</div>
								<div className="panel-item">
									<button className="p-link" onClick={event => this.props.changeMenuMode({ originalEvent: event, menuMode: 'overlay' })}>
										<img src="assets/layout/images/configurator/menu/roma-overlay.png" alt="roma"/>
										{this.props.layoutMode === 'overlay' && <i className="pi pi-check"/>}
									</button>
									<span>Overlay</span>
								</div>
								<div className="panel-item">
									<button className="p-link" onClick={event => this.props.changeMenuMode({ originalEvent: event, menuMode: 'horizontal' })}>
										<img src="assets/layout/images/configurator/menu/roma-horizontal.png" alt="roma"/>
										{this.props.layoutMode === 'horizontal' && <i className="pi pi-check"/>}
									</button>
									<span>Horizontal</span>
								</div>
								<div className="panel-item">
									<button className="p-link" onClick={event => this.props.changeMenuMode({ originalEvent: event, menuMode: 'slim' })}>
										<img src="assets/layout/images/configurator/menu/roma-slim.png" alt="roma"/>
										{this.props.layoutMode === 'slim' && <i className="pi pi-check"/>}
									</button>
									<span>Slim</span>
								</div>
							</div>

							<h1>Menu Colors</h1>
							<div className="panel-items">
								<div className="panel-item">
									<button className="p-link" onClick={event => this.props.changeMenuColor({ originalEvent: event, lightMenu: false })}>
										<img src="assets/layout/images/configurator/menu/roma-dark.png" alt="roma"/>
										{this.props.lightMenu === false && <i className="pi pi-check"/>}
									</button>
									<span>Dark</span>
								</div>
								<div className="panel-item">
									<button className="p-link" onClick={event => this.props.changeMenuColor({ originalEvent: event, lightMenu: true })}>
										<img src="assets/layout/images/configurator/menu/roma-static.png" alt="roma"/>
										{this.props.lightMenu === true && <i className="pi pi-check"/>}
									</button>
									<span>Light</span>
								</div>
							</div>
						</TabPanel>

						<TabPanel header="User Profile" headerClassName="">
							<div className="panel-items">
								<div className="panel-item">
									<button className={classNames("p-link",{'p-disabled': this.props.layoutMode === 'horizontal'})}
											onClick={event => this.props.changeProfileMode({ originalEvent: event, inlineUser: true })}>
										<img src="assets/layout/images/configurator/menu/roma-inline.png" alt="roma"/>
										{this.props.inlineUser === true && <i className="pi pi-check"/>}
									</button>
									<span>Inline</span>
								</div>
								<div className="panel-item">
									<button className={classNames("p-link",{'p-disabled': this.props.layoutMode === 'horizontal'})}
											onClick={event => this.props.changeProfileMode({ originalEvent: event, inlineUser: false })}>
										<img src="assets/layout/images/configurator/menu/roma-popup.png" alt="roma"/>
										{this.props.inlineUser === false && <i className="pi pi-check"/>}
									</button>
									<span>Overlay</span>
								</div>
							</div>
						</TabPanel>

						<TabPanel header="Orientation" headerClassName="">
							<div className="panel-items">
								<div className="panel-item">
									<button className="p-link" onClick={event => this.props.changeOrientation({ originalEvent: event, isRTL: false })}>
										<img src="assets/layout/images/configurator/menu/roma-rtl.png" alt="roma"/>
										{this.props.isRTL === false && <i className="pi pi-check"/>}
									</button>
									<span>LTR</span>
								</div>
								<div className="panel-item">
									<button className="p-link" onClick={event => this.props.changeOrientation({ originalEvent: event, isRTL: true })}>
										<img src="assets/layout/images/configurator/menu/roma-static.png" alt="roma"/>
										{this.props.isRTL === true && <i className="pi pi-check"/>}
									</button>
									<span>RTL</span>
								</div>
							</div>
						</TabPanel>

						<TabPanel header="Topbar">
							<div className="panel-items">
								{topbarColors && topbarColors.map((t, index) => {
								return <div className="panel-item colors" key={index}>
									<button className="p-link layout-config-option"
											onClick={event => this.props.changeTopbarColor({ originalEvent: event, topbarColor: t.topbarColor, logo: t.logo })}>
										<img src={"assets/layout/images/configurator/topbar/" + t.image} alt={t.name}/>
										{this.props.topbarColor === t.topbarColor && <i className="pi pi-check"/>}
									</button>
								</div>
								})
								}
							</div>
						</TabPanel>

						<TabPanel header="Themes">
							<div className="panel-items">
								{themeColors && themeColors.map((t, index) => {
									return <div className="panel-item colors" key={index}>
										<button className="p-link layout-config-option"
												onClick={event => this.props.changeTheme({ originalEvent: event, theme: t.file,})}>
											<img src={"assets/layout/images/configurator/themes/" + t.image} alt={t.name}/>
											{this.props.themeColor === t.file && <i className="pi pi-check"/>}
										</button>
									</div>
								})
								}
							</div>
						</TabPanel>
					</TabView>
				</div>
			</div>
		);
	}
}
