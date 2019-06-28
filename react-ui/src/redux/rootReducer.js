import { combineReducers } from 'redux';
import schemaUpdateReducer from './schemaUpdateReducer';
import schemaListReducer from './schemaListReducer';
import relationListReducer from './relationListReducer';
import queryListReducer from './queryListReducer';
import planReducer from './planReducer';
import runReducer from './runReducer';
import dependencyListsReducer from './dependencyListsReducer';

/**
 * Combines setBody and schemaUpdate reducers into a single rootReducer.
 * To access state from the components, use setBody and schemaUpdate.
 *
 * @author Camilo Ortiz
 */

const rootReducer = combineReducers({
  selectedSchema: schemaUpdateReducer,
  relationList: relationListReducer,
  schemaList: schemaListReducer,
  queryList: queryListReducer,
  plan: planReducer,
  planRun: runReducer,
  dependencyLists: dependencyListsReducer
})

export default rootReducer;
