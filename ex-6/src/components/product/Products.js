import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";
import App from "../../App";

class Products extends Component {

    constructor() {
        super();
        this.state = {
            products: [],
        };
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
                        <div key={prod.id}>
                            <div className="title">{prod.name}</div>
                            <div>{prod.description}</div>
                            <div>{prod.category}</div>
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
            <div className="products">
                {this.state.products}
            </div>
        )
    }
}

export default Products;