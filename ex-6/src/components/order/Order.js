import React, {Component} from 'react';
import AuthenticationService from "../../services/AuthenticationService";
import App from "../../App";

class Order extends Component {

    constructor() {
        super();
        this.state = {
            order: [],
        };
    }

    componentDidMount() {
        fetch("http://localhost:9000/products-in-order/" + this.props.match.params.orderReference, {
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
                            </div>
                        </div>
                    )
                });
                const state = this.state;
                state.order = products;
                this.setState(state)
            });
    }

    render() {
        return (
            <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                <div className="pure-g">
                    {this.state.order}
                </div>
            </div>
        )
    }
}

export default Order;