import React, {useRef, useState} from 'react';
import {InputText} from 'primereact/inputtext';
import {Button} from "primereact/button";
import {LoginService} from "../service/Login";
import {Growl} from "primereact/growl";
import {ProgressSpinner} from "primereact/progressspinner";

export default function Login() {
	const growl = useRef(null);

	const [userName,setUserName] = useState('');
	const [password,setPassword] = useState('');
	const [loading,setLoading] = useState(false);

	const loginService = new LoginService();

	const handleLogin = () => {
		if(userName !== '' && password !== ''){
				setLoading(true);
				loginService.requestToken(userName,password).then(({data})=>{
					console.log('Ressss-----> ',data);
					growl.current.show({ severity: 'success', summary: "Success", detail: 'Login Successful!' });
					localStorage.setItem('daym-tok',data.accessToken);
					localStorage.setItem('daym-user',JSON.stringify(data.user));
					if(data.user.role ===  4){
						window.location = "/#/appointments";
					}else {
						window.location = "/#";
					}
				}).catch((err)=>{
					console.log('Errrr',err);
					const error = err.response && err.response.data ? err.response.data.message : 'Something went wrong';
					growl.current.show({ severity: 'error', summary: "Error", detail: error });
				}).finally(()=>{
					setLoading(false);
				});

		}else{
			growl.current.show({ severity: 'error', summary: "Error", detail: 'Username or Password cannot be blank!' });
		}
	};

	return <div className="login-body">
		<Growl ref={growl} style={{marginTop: '75px'}} />
		<div className="card login-panel ui-fluid">
			<div className="login-panel-content">
				<div className="p-grid">
					<div className="p-col-12 p-sm-6 p-md-6 logo-container">
						<img src="assets/layout/images/logo-roma.svg" alt="roma" style={{width: 100, height: 'auto', marginTop: 50}}/>
						<span className="guest-sign-in">Welcome, please use the form to sign-in to DAYM</span>
					</div>
					<div className="p-col-12 username-container">
						<label>Username</label>
						<div className="login-input">
							<InputText id="input" type="text"  value={userName} onChange={(e) => setUserName(e.target.value)}/>
						</div>
					</div>
					<div className="p-col-12 password-container">
						<label>Password</label>
						<div className="login-input">
							<InputText type="password" value={password} onChange={(e) => setPassword(e.target.value)}/>
						</div>
					</div>

					<div className="p-col-12 p-sm-12 p-md-12">
					{
						!loading ?
							<Button label="Sign In" icon="pi pi-user" onClick={handleLogin} /> :
							<ProgressSpinner style={{width: '30px', height: '30px'}} strokeWidth="3" animationDuration=".5s"/>
					}
					</div>
				</div>
			</div>
		</div>
	</div>

}
