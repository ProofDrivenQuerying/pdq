// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import { combineReducers } from 'redux';
import schemaListReducer from './schemaListReducer';
import relationListReducer from './relationListReducer';
import planReducer from './planReducer';
import runReducer from './runReducer';
import dependencyListsReducer from './dependencyListsReducer';
import queryValidation from './queryValidation';

/**
 * Combines setBody and schemaUpdate reducers into a single rootReducer.
 * To access state from the components, use setBody and schemaUpdate.
 *
 * @author Camilo Ortiz
 */

const rootReducer = combineReducers({
  relationList: relationListReducer,
  schemaList: schemaListReducer,
  plan: planReducer,
  planRun: runReducer,
  dependencyLists: dependencyListsReducer,
  queryValidation: queryValidation,
})

export default rootReducer;
