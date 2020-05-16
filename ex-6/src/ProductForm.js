import React, {Component} from 'react';

class Products extends Component {

    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);
        var object = {}
        data.forEach((value, key) => {object[key] = value});

        var url = 'http://localhost:9000/product';

        fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(object),
        });
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                <label htmlFor="name">Product name</label>
                <input id="name" name="name" type="text" />

                <label htmlFor="description">Description</label>
                <input id="description" name="description" type="description" />

                <label htmlFor="category">Category</label>
                <input id="category" name="category" type="category" />

                <label htmlFor="producer">Producer</label>
                <input id="producer" name="producer" type="producer" />

                <label htmlFor="price">Price</label>
                <input id="price" name="price" type="price" />

                <button>Add product</button>
            </form>
        );
    }

}


export default Products;