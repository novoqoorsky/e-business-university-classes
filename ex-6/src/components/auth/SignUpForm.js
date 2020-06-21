import React, {Component} from 'react';

class SignUpForm extends Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.signUpWithFacebook = this.signUpWithFacebook.bind(this);
        this.signUpWithGoogle = this.signUpWithGoogle.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);
        var object = {}
        data.forEach((value, key) => {object[key] = value});

        var url = 'http://localhost:9000/sign-up';

        fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(object),
        });

        this.props.history.push('/#');
    }

    signUpWithFacebook() {
        this.authenticate("facebook")
    }

    signUpWithGoogle() {
        this.authenticate("google")
    }

    authenticate(provider) {
        window.location.href = 'http://localhost:9000/authenticate/' + provider;
    }

    render() {

        return (
            <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                <form onSubmit={this.handleSubmit} className="pure-form pure-form-stacked">

                    <label htmlFor="firstName">First Name</label>
                    <input id="firstName" name="firstName" type="text"/>

                    <label htmlFor="lastName">Last Name</label>
                    <input id="lastName" name="lastName" type="text"/>

                    <label htmlFor="email">Email</label>
                    <input id="mail" name="email" type="email"/>

                    <label htmlFor="password">Password</label>
                    <input id="password" name="password" type="password"/>

                    <br/>
                    <button className="pure-button pure-button-primary">Sign up</button>
                </form>

                <br/><br/>

                <div>
                    <button className="loginBtn loginBtn--facebook" onClick={() => this.signUpWithFacebook()}>
                        Sign Up with Facebook
                    </button>
                    <br/>
                    <button className="loginBtn loginBtn--google" onClick={() => this.signUpWithFacebook()}>
                        Sign Up with Google
                    </button>
                </div>
            </div>
        );
    }

}

export default SignUpForm;
