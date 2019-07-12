import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import './bootstrap.css';
import App from './App';

// import RelationsModal from './components/body/schemas/RelationsModal';

import * as serviceWorker from './serviceWorker';
import { Provider } from 'react-redux';
import { store } from './reducers/store';

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

// const relationsElem = document.getElementById('relations');
// ReactDOM.render(
//     <RelationsModal/>,
//   relationsElem
// );

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
