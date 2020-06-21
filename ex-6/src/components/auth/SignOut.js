import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";

class SignOut extends Component {

    componentDidMount() {
        const url = "http://localhost:9000/sign-out";

        fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'X-Auth-Token': AuthenticationService.getAuthToken()
            }
        });

        AuthenticationService.setUserData(undefined);
    }

    render() {
        return (<div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>Signed out</div>);
    }
}

export default SignOut;
