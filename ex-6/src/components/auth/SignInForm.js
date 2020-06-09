import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";

class SignInForm extends Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);
        var object = {};
        data.forEach((value, key) => {object[key] = value});

        const url = 'http://localhost:9000/sign-in';

        fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(object),
        }).then(results => {
            return results.json();
        }).then(userData => {
            AuthenticationService.setUserData(userData);
            this.props.onSignIn();
        });

        this.props.history.push('/#');
    }

    render() {

        return (
            <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                <form onSubmit={this.handleSubmit} className="pure-form pure-form-stacked">

                    <label htmlFor="email">Email</label>
                    <input id="mail" name="email" type="email"/>

                    <label htmlFor="password">Password</label>
                    <input id="password" name="password" type="password"/>

                    <label htmlFor="rememberMe">Remember me</label>
                    <input id="rememberMe" name="rememberMe" type="checkbox" value="true" defaultChecked/>

                    <br/><br/>
                    <button className="pure-button pure-button-primary">Sign in</button>
                </form>
            </div>
        );
    }

}

export default SignInForm;
