import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";

class SocialAuthentication extends Component {

    constructor(props) {
        super(props);
    }

    componentDidMount() {
        AuthenticationService.setUserData(JSON.parse(new URLSearchParams(window.location.search).get('body')));
        this.props.onSignIn();
    }

    render() {
        return (<div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>Signed in</div>);
    }
}

export default SocialAuthentication;
