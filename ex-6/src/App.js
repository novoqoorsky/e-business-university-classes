import React, {Component} from 'react';
import {
    BrowserRouter as Router,
    Route,
    Link
} from 'react-router-dom';
import Products from './components/product/Products'
import ProductForm from './components/product/ProductForm'

import './App.css';
import SignUpForm from "./components/auth/SignUpForm";
import SignInForm from "./components/auth/SignInForm";
import SignOut from "./components/auth/SignOut";
import AuthenticationService from "./services/AuthenticationService";
import Profile from "./components/profile/Profile";
import CreateProfile from "./components/profile/CreateProfile";
import Order from "./components/order/Order";

class App extends Component {

    constructor(props) {
        super(props);
        this.state = {
            isAdmin: AuthenticationService.isAdmin(),
            isAuthenticated: AuthenticationService.isAuthenticated()
        };
        this.onSignIn = this.onSignIn.bind(this);
    }

    onSignOut = () => {
        this.setState({
            isAdmin: false,
            isAuthenticated: false
        });
    };

    onSignIn = () => {
        this.setState({
            isAdmin: AuthenticationService.isAdmin(),
            isAuthenticated: true
        });
    };

    static checkError(response) {
        if (response.ok) {
            return response.json();
        } else {
            throw Error(response.statusText);
        }
    }

    render() {
        return <Router>
            <div className="header" id="menu">
                <div className="home-menu pure-menu pure-menu-horizontal pure-menu-fixed">
                    <a className="pure-menu-heading" href="/" style={{color: "dodgerblue"}}>Strong@Home</a>

                    <ul className="pure-menu-list">
                        <li className="pure-menu-item pure-menu-selected">
                            <a href="#" className="pure-menu-link">Home</a>
                        </li>
                        <li className="pure-menu-item pure-menu-selected">
                            <Link to="/products" className="pure-menu-link">Products</Link>
                        </li>
                        {this.state.isAdmin && (
                            <li className="pure-menu-item pure-menu-selected">
                                <Link to="/productadd" className="pure-menu-link">Add Product</Link>
                            </li>
                        )}
                        {!this.state.isAuthenticated && (
                            <>
                                <li className="pure-menu-item pure-menu-selected">
                                    <Link to="/signin" className="pure-menu-link">Sign in</Link>
                                </li>
                                <li className="pure-menu-item pure-menu-selected">
                                    <Link to="/signup" className="pure-menu-link">Sign up</Link>
                                </li>
                            </>
                        )}
                        {this.state.isAuthenticated && (
                            <>
                                <li className="pure-menu-item pure-menu-selected">
                                    <Link to="/profile" className="pure-menu-link">My Profile</Link>
                                </li>
                                <li className="pure-menu-item pure-menu-selected">
                                    <Link to="/signout" className="pure-menu-link" onClick={this.onSignOut}>Sign out</Link>
                                </li>
                            </>
                        )}
                    </ul>

                    <Route path="/products" component={Products}/>
                    <Route path="/productadd" component={ProductForm}/>
                    <Route path="/signup" component={SignUpForm}/>
                    <Route path="/signin" render={(props) => <SignInForm {...props} onSignIn={this.onSignIn}/>}/>
                    <Route path="/profile" component={Profile}/>
                    <Route path="/signout" component={SignOut}/>

                    <Route path="/create-profile" component={CreateProfile}/>
                    <Route path="/order/:orderReference" component={Order}/>
                </div>
            </div>

        </Router>
    }
}

export default App;
