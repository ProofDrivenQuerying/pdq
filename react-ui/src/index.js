// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import * as serviceWorker from './serviceWorker';
import { Provider } from 'react-redux';
import { store } from './reducers/store';
import './index.css';
import '../node_modules/bootstrap/dist/css/bootstrap.css';

/**
 * index.js uses ReactDom to render JSX code as HTML for the client.
 *
 * The Provider component provides the React store to all its child
 * components so we don't need to pass it explicitly to all the components.
 *
 * @author Camilo Ortiz
 */


const rootElement = document.getElementById('root');

ReactDOM.render(
  <Provider store = {store}>
    <App/>
  </Provider>,
  rootElement
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
