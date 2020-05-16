import React, {Component} from 'react';

class Products extends Component {

    constructor(props) {
        super(props);
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

        this.props.history.push('/products/');
    }

    render() {

        return (
            <div className="content" style={{position:"absolute", left:"30px", top:"50px"}}>
                <form onSubmit={this.handleSubmit} class="pure-form pure-form-stacked">

                    <label htmlFor="name">Name</label>
                    <input id="name" name="name" type="text" />

                    <label htmlFor="description">Description</label>
                    <input id="description" name="description" type="text" />

                    <label htmlFor="category">Category</label>
                    <input id="category" name="category" type="text" />

                    <label htmlFor="producer">Producer</label>
                    <input id="producer" name="producer" type="text" />

                    <label htmlFor="price">Price</label>
                    <input id="price" name="price" type="number" />

                    <br/>
                    <button class="pure-button pure-button-primary">Add product</button>
                </form>
            </div>
        );
    }

}

export default Products;