import React, {Component} from 'react';

class Products extends Component {

    constructor() {
        super();
        this.state = {
            products: [],
        };
    }

    componentDidMount() {
        var url = "http://localhost:9000/products"

        fetch(url, {
            mode: 'cors',
            headers:{
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin':'http://localhost:3000',
            },
            method: 'GET',
        })
            .then(response => Products.checkError(response))
            .then(data => {
                let products = data.map((prod) => {
                    return (
                        <div key={prod.id}>
                            <div className="title">{prod.name}</div>
                            <div>{prod.description}</div>
                            <div>{prod.category}</div>
                        </div>
                    )
                })
                this.setState({products: products})
            }).catch(error => {
                console.log(error);
                window.location.href = "/signin"
        })
    }

    render() {
        return (
            <div className="products">
                {this.state.products}
            </div>
        )
    }

    static checkError(response) {
        if (response.ok) {
            return response.json();
        } else {
            throw Error(response.statusText);
        }
    }
}

export default Products;