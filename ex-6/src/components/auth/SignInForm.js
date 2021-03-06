import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";
import App from "../../App";

class SignInForm extends Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.signInWithFacebook = this.signInWithFacebook.bind(this);
        this.signInWithGoogle = this.signInWithGoogle.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);
        var object = {};
        data.forEach((value, key) => {
            object[key] = value
        });

        const url = 'http://localhost:9000/sign-in';

        fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(object),
        })
            .then(response => App.checkError(response))
            .then(userData => {
                AuthenticationService.setUserData(userData);
                this.props.onSignIn();
            })
            .catch(error => {
                console.log(error);
                window.location.href = "/signin"
            });

        this.props.history.push('/#');
    }

    signInWithFacebook() {
        this.authenticate("facebook")
    }

    signInWithGoogle() {
        this.authenticate("google")
    }

    authenticate(provider) {
        window.location.href = 'http://localhost:9000/authenticate/' + provider;
    }

    render() {

        return (
            <div className="content" style={{position: "absolute", left: "30px", top: "50px"}}>
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

                <br/><br/>

                <div>
                    <button className="loginBtn loginBtn--facebook" onClick={() => this.signInWithFacebook()}>
                        Sign In with Facebook
                    </button>
                    <br/>
                    <button className="loginBtn loginBtn--google" onClick={() => this.signInWithGoogle()}>
                        Sign In with Google
                    </button>
                </div>
            </div>
        );
    }
}

export default SignInForm;
