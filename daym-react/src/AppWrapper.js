import React, {Component} from 'react';
import {Route, withRouter} from 'react-router-dom';
import App from "./App";
import Login from "./pages/Login";
import Error from "./pages/Error";
import NotFound from "./pages/NotFound";
import Access from "./pages/Access";
import {LoginService} from "./service/Login";

class AppWrapper extends Component {
	constructor() {
		super();
		this.state = {
			dataTableValue:[],
			loading: true,
		};
		this.loginService = new LoginService();
	}

	componentDidUpdate(prevProps) {
		if (this.props.location !== prevProps.location) {
			window.scrollTo(0, 0)
		}
	}

	componentDidMount() {
		if(localStorage.getItem('daym-tok')){
			this.loginService.validateToken(localStorage.getItem('daym-tok')).then(({data})=>{
				// console.log('Ressss Token -----> ',data);
				localStorage.setItem('daym-tok',data.accessToken);
				localStorage.setItem('daym-user',JSON.stringify(data.user));
			}).catch((err)=>{
				localStorage.removeItem('daym-tok');
				localStorage.removeItem('daym-user');
				window.location.reload();
			}).finally(()=>{
			})
		}

	}

	render() {
		if(!localStorage.getItem('daym-tok') && this.props.location.pathname !== '/login'){
			window.location = "#/login";
			return <Route path="/login" component={Login}/>;
		}else if(localStorage.getItem('daym-tok') && this.props.location.pathname === '/login'){
			window.location = "#/";
			return <App />;
		}else{
			switch(this.props.location.pathname) {
				case "/login":
					return <Route path="/login" component={Login}/>
				case "/error":
					return <Route path="/error" component={Error}/>
				case "/404":
					return <Route path="/404" component={NotFound}/>
				case "/accessdenied":
					return <Route path="/accessdenied" component={Access}/>
				default:
					return <App/>;
			}
		}

	}
}

export default withRouter(AppWrapper);
