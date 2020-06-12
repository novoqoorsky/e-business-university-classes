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
            cartContent: [],
            cart: {},
            orders: []
        };
        this.finalizeOrder = this.finalizeOrder.bind(this);
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
                    this.setState(state);

                    this.fetchData();
                }
            });
    }

    fetchData() {
        this.fetchAddress();
        this.fetchCartContent();
        this.fetchOrderHistory();
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
                    {this.state.cartContent.length !== 0 && <div>
                        Your cart - {this.state.cart.value} PLN:
                        <div className="pure-g">
                            {this.state.cartContent}
                        </div>
                        <br/>
                        <button className="pure-button pure-button-primary" onClick={this.finalizeOrder}>
                            Finalize order
                        </button>
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

    finalizeOrder() {
        fetch("http://localhost:9000/" + this.state.cart.id + "/finalize/" + this.state.profile.id, {
            mode: 'cors',
            headers:{
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin':'http://localhost:3000',
                'X-Auth-Token': AuthenticationService.getAuthToken()
            },
            method: 'POST',
        }).then(_ => {
            const state = this.state;
            state.cart = {};
            state.cartContent = [];
            this.setState(state);
            this.fetchOrderHistory();
        });
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

    fetchCartContent() {
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
                        <div className="pure-u-1-2" key={prod.id}>
                            <div className="l-box">
                                <div><b>{prod.name}</b></div>
                                <div>{prod.description}</div>
                                <div>{prod.producer}</div>
                                <div>{prod.price} PLN </div>
                            </div>
                        </div>
                    )
                });
                const state = this.state;
                state.cartContent = products;
                this.setState(state)
            });

        this.fetchCart();
    }

    fetchCart() {
        fetch("http://localhost:9000/cart/" + this.state.profile.cart , {
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
            .then(cart => {
                const state = this.state;
                state.cart = cart;
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
                            <Link to={{
                                pathname: "/order/" + order.reference
                            }}>{order.reference}</Link>
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
