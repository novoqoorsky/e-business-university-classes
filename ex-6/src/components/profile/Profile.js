import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";
import App from "../../App";
import {Link} from "react-router-dom";

class Profile extends Component {

    constructor(props) {
        super(props);
        this.state = {
            profile: {},
            address: {},
            cart: [],
            orders: []
        };
    }

    componentDidMount() {
        const url = "http://localhost:9000/client-by-email/" + AuthenticationService.userData.email;

        fetch(url, {
            mode: 'cors',
            headers:{
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin':'http://localhost:3000',
                'X-Auth-Token': AuthenticationService.getAuthToken()
            },
            method: 'GET',
        })
            .then(response => App.checkError(response))
            .then(data => {
                if (data !== null) {
                    const state = this.state;
                    state.profile = data;
                    this.setState(state)

                    this.fetchAddress();
                    this.fetchCart();
                    this.fetchOrderHistory();
                }
            });
    }

    render() {
        if (this.state.profile === null || Object.keys(this.state.profile).length === 0) {
            return (
                <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                    Looks like you haven't created your profile yet - <Link to="/create-profile"> Let's do now!</Link>
                </div>
            );
        } else {
            return (
                <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                    <b>{this.state.profile.name} {this.state.profile.lastName} ({this.state.profile.email})</b>
                    <div>
                        Your delivery address: {this.state.address.streetName} {this.state.address.houseNumber}, {this.state.address.postalCode} {this.state.address.city}
                    </div>
                    <br/>
                    {this.state.cart.length !== 0 && <div>
                        Your cart:
                        {this.state.cart}
                    </div>}
                    <br/>
                    {this.state.orders.length !== 0 && <div>
                        Your past orders:
                        {this.state.orders}
                    </div>}
                </div>
            );
        }
    }

    fetchAddress() {
        fetch("http://localhost:9000/address/" + this.state.profile.address , {
            mode: 'cors',
            headers:{
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin':'http://localhost:3000',
                'X-Auth-Token': AuthenticationService.getAuthToken()
            },
            method: 'GET',
        })
            .then(response => App.checkError(response))
            .then(data => {
                const state = this.state;
                state.address = data;
                this.setState(state)
            });
    }

    fetchCart() {
        fetch("http://localhost:9000/products-in-cart/" + this.state.profile.cart , {
            mode: 'cors',
            headers:{
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin':'http://localhost:3000',
                'X-Auth-Token': AuthenticationService.getAuthToken()
            },
            method: 'GET',
        })
            .then(response => App.checkError(response))
            .then(data => {
                let products = data.map((prod) => {
                    return (
                        <div key={prod.id}>
                            <div className="title">{prod.name}</div>
                            <div>{prod.description}</div>
                            <div>{prod.category}</div>
                        </div>
                    )
                });
                const state = this.state;
                state.cart = products;
                this.setState(state)
            });
    }

    fetchOrderHistory() {
        fetch("http://localhost:9000/orders-by-client/" + this.state.profile.id , {
            mode: 'cors',
            headers:{
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin':'http://localhost:3000',
                'X-Auth-Token': AuthenticationService.getAuthToken()
            },
            method: 'GET',
        })
            .then(response => App.checkError(response))
            .then(data => {
                let orders = data.map((order) => {
                    return (
                        <div key={order.id}>
                            <div className="title">{order.reference}</div>
                        </div>
                    )
                });
                const state = this.state;
                state.orders = orders;
                this.setState(state)
            });
    }
}

export default Profile;
