import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import { createStore } from 'redux'
import rootReducer from './reducers'
import App from './App';
import * as serviceWorker from './serviceWorker';

const store = createStore(rootReducer);

ReactDOM.render(<App />, document.getElementById('root'));

serviceWorker.unregister();