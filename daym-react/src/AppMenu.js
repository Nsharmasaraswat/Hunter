import React, { Component } from 'react';
import {NavLink} from 'react-router-dom'
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { CSSTransition } from 'react-transition-group';

class AppSubmenu extends Component {

    static defaultProps = {
        className: null,
        items: null,
        onMenuItemClick: null,
        onRootItemClick: null,
        root: false,
        layoutMode: null,
        menuActive: false,
		parentMenuItemActive: false
    }

    static propTypes = {
        className: PropTypes.string,
        items: PropTypes.array,
        onMenuItemClick: PropTypes.func,
        onRootItemClick: PropTypes.func,
        root: PropTypes.bool,
        layoutMode: PropTypes.string,
        menuActive: PropTypes.bool,
		parentMenuItemActive: PropTypes.bool
    }

    constructor(props) {
        super(props);
        this.state = {};
    }

    onMenuItemClick(event, item, index) {
        //avoid processing disabled items
        if(item.disabled) {
            event.preventDefault();
            return true;
        }

        if(this.props.root && this.props.onRootItemClick) {
            this.props.onRootItemClick({
                originalEvent: event,
                item: item
            });
        }

		if (item.items) {
			event.preventDefault();
		}

        //execute command
        if(item.command) {
            item.command({originalEvent: event, item: item});
			event.preventDefault();
        }

        if(index === this.state.activeIndex)
            this.setState({activeIndex: null});
        else
            this.setState({activeIndex: index});

        if(this.props.onMenuItemClick) {
            this.props.onMenuItemClick({
                originalEvent: event,
                item: item
            });
        }
    }

    onMenuItemMouseEnter(index) {
        if(this.props.root && this.props.menuActive && this.isHorizontalOrSlim() && !this.isMobile()) {
            this.setState({activeIndex: index});
        }
    }

	static getDerivedStateFromProps(nextProps, prevState) {
		if (nextProps.parentMenuItemActive === false) {
			return {
				activeIndex: null
			}
		}

		return null;
	}

    componentDidUpdate(prevProps, prevState, snapshot) {
        if(this.isHorizontalOrSlim() && !this.isMobile() && prevProps.menuActive && !this.props.menuActive) {
            this.setState({activeIndex: null});
        }
    }

	isMobile() {
		return window.innerWidth <= 1025;
	}

    isHorizontalOrSlim() {
        return (this.props.layoutMode === 'horizontal' || this.props.layoutMode === 'slim');
    }

	renderLinkContent(item) {
		let submenuIcon = item.items && <i className="pi pi-fw pi-angle-down layout-submenu-toggler"></i>;

		return (
			<React.Fragment>
				<i className={classNames(item.icon, "layout-menuitem-icon")}></i>
				<span className="layout-menuitem-text">{item.label}</span>
				{submenuIcon}
			</React.Fragment>
		);
	}

	renderLink(item, i) {
		let content = this.renderLinkContent(item);

		if (item.to) {
			return (
				<NavLink activeClassName="active-menuitem-routerlink" to={item.to} onClick={(e) => this.onMenuItemClick(e, item, i)} exact
						 target={item.target} onMouseEnter={(e) => this.onMenuItemMouseEnter(i)} className={item.styleClass}>{content}</NavLink>
			)
		}
		else {
			return (
				<button onClick={(e) => this.onMenuItemClick(e, item, i)}
				   onMouseEnter={(e) => this.onMenuItemMouseEnter(i)} className={classNames(item.styleClass,'p-link')}>
					{content}
				</button>
			);

		}
	}

    render() {
        var items = this.props.items && this.props.items.map((item, i) => {
            let active = this.state.activeIndex === i;
            let styleClass = classNames(item.badgeStyleClass, {'active-menuitem': active});
            let tooltip = this.props.root && <div className="layout-menu-tooltip">
                                                <div className="layout-menu-tooltip-arrow"></div>
                                                <div className="layout-menu-tooltip-text">{item.label}</div>
                                            </div>;

            return <li className={styleClass} key={i}>
                        {this.renderLink(item, i)}
                        {tooltip}
                        <CSSTransition classNames="layout-submenu" timeout={{ enter: 400, exit: 400 }} in={active}>
							<AppSubmenu items={item.items} onMenuItemClick={this.props.onMenuItemClick} layoutMode={this.props.layoutMode}
										menuActive={this.props.menuActive} parentMenuItemActive={active}/>
						</CSSTransition>
                    </li>
        });

        return items?<ul className={this.props.className}>{items}</ul>:null;
    }
}

export class AppMenu extends Component {

    static defaultProps = {
        model: null,
        onMenuItemClick: null,
        onRootMenuItemClick: null,
        layoutMode: null,
        active: false
    }

    static propTypes = {
        model: PropTypes.array,
        layoutMode: PropTypes.string,
        onMenuItemClick: PropTypes.func,
        onRootMenuItemClick: PropTypes.func,
        active: PropTypes.bool
    }

    render() {
        return <AppSubmenu items={this.props.model} className="layout-menu"
                menuActive={this.props.active} onRootItemClick={this.props.onRootMenuItemClick}
                onMenuItemClick={this.props.onMenuItemClick} root={true} layoutMode={this.props.layoutMode} parentMenuItemActive={true}/>
    }
}
