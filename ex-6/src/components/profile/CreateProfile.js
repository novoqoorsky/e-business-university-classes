import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";

class CreateProfile extends Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);
        var object = {};
        data.forEach((value, key) => {object[key] = value});

        var url = 'http://localhost:9000/client';

        fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'X-Auth-Token': AuthenticationService.getAuthToken()
            },
            body: JSON.stringify(object),
        });

        this.props.history.push('/profile/');
    }

    render() {

        return (
            <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                <form onSubmit={this.handleSubmit} class="pure-form pure-form-stacked">

                    <div className="pure-g">
                        <div className="pure-u-1-2">
                            <label htmlFor="firstName">First Name</label>
                            <input id="firstName" name="firstName" type="text" value={AuthenticationService.userData.firstName} readOnly/>

                            <label htmlFor="lastName">Last Name</label>
                            <input id="lastName" name="lastName" type="text" value={AuthenticationService.userData.lastName} readOnly/>

                            <label htmlFor="email">Email</label>
                            <input id="mail" name="email" type="email" value={AuthenticationService.userData.email} readOnly/>
                        </div>
                        <div className="pure-u-1-2">
                            <label htmlFor="city">City</label>
                            <input id="city" name="city" type="text" required/>

                            <label htmlFor="streetName">Street Name</label>
                            <input id="streetName" name="streetName" type="text" required/>

                            <label htmlFor="houseNumber">House Number</label>
                            <input id="houseNumber" name="houseNumber" type="number" required/>

                            <label htmlFor="postalCode">Postal Code</label>
                            <input id="postalCode" name="postalCode" type="text" required/>

                        </div>
                    </div>

                    <br/>
                    <button class="pure-button pure-button-primary">Create profile</button>
                </form>
            </div>
        );
    }

}

export default CreateProfile;