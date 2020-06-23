// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

/**
 * Redux's store. This is where the state is kept.
 *
 * @author Camilo Ortiz
 */

import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';
import rootReducer from './rootReducer.js';


export const store = createStore(rootReducer, applyMiddleware(thunk));
