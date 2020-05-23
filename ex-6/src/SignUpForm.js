import React, {Component} from 'react';

class SignUpForm extends Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);
        var object = {}
        data.forEach((value, key) => {object[key] = value});

        var url = 'http://localhost:9000/user';

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

    render() {

        return (
            <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                <form onSubmit={this.handleSubmit} class="pure-form pure-form-stacked">
                    <label htmlFor="userName">User Name</label>
                    <input id="userName" type="text"/>

                    <label htmlFor="email">Email</label>
                    <input id="email" type="email"/>

                    <label htmlFor="password">Password</label>
                    <input id="password" type="password"/>

                    <br/>
                    <button type="submit" className="pure-button pure-button-primary">Sign Up</button>
                </form>
            </div>
        );
    }

}

export default SignUpForm;
