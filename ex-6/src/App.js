import React from 'react';
import {
    BrowserRouter as Router,
    Route,
    Link
} from 'react-router-dom';
import Products from './Products'
import ProductForm from './ProductForm'

import './App.css';
import SignUp from "./SignUpForm";

function App() {
    return <Router>

        <div className="header">
            <div className="home-menu pure-menu pure-menu-horizontal pure-menu-fixed">
                <a className="pure-menu-heading" href="/" style={{color:"dodgerblue"}}>Strong@Home</a>

                <ul className="pure-menu-list">
                    <li className="pure-menu-item pure-menu-selected">
                        <a href="#" className="pure-menu-link">Home</a>
                    </li>
                    <li className="pure-menu-item pure-menu-selected">
                        <Link to="/products" className="pure-menu-link">Products</Link>
                    </li>
                    <li className="pure-menu-item pure-menu-selected">
                        <Link to="/productadd" className="pure-menu-link">Add Product</Link>
                    </li>
                    <li className="pure-menu-item"><a href="#" className="pure-menu-link">Sign in</a></li>

                    <li className="pure-menu-item pure-menu-selected">
                        <Link to="/signup" className="pure-menu-link">Sign up</Link>
                    </li>
                </ul>

                <Route path="/products" component={Products}/>
                <Route path="/productadd" component={ProductForm}/>
                <Route path="/signup" component={SignUp}/>
            </div>
        </div>

    </Router>
}

export default App;
