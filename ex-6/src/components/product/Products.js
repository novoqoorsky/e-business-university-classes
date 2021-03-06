import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";
import App from "../../App";

class Products extends Component {

    constructor() {
        super();
        this.state = {
            products: [],
        };
        this.addToCart = this.addToCart.bind(this);
    }

    componentDidMount() {
        const url = "http://localhost:9000/products";

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
                let products = data.map((prod) => {
                    return (
                        <div className="pure-u-1-3" key={prod.id}>
                            <div className="l-box">
                                <div><b>{prod.name}</b></div>
                                <div>{prod.description}</div>
                                <div>{prod.producer}</div>
                                <div>{prod.price} PLN </div>
                                <div><i>{prod.category}</i></div>
                                <br/>
                                <button className="pure-button pure-button-primary button-small" onClick={() => this.addToCart(prod.id)}>
                                    Add to cart
                                </button>
                            </div>
                        </div>
                    )
                });
                this.setState({products: products})
            }).catch(error => {
                console.log(error);
                window.location.href = "/signin"
            });
    }

    render() {
        return (
            <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                <div className="pure-g">
                    {this.state.products}
                </div>
            </div>
        )
    }

    addToCart(product) {
        const url = "http://localhost:9000/cart/" + AuthenticationService.userData.email + "/product/" + product;

        fetch(url, {
            mode: 'cors',
            headers:{
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin':'http://localhost:3000',
                'X-Auth-Token': AuthenticationService.getAuthToken()
            },
            method: 'POST',
        })
    }
}

export default Products;