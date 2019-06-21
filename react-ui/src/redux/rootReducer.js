import { combineReducers } from 'redux';
import setBodyReducer from './setBody';
import schemaUpdateReducer from './schemaUpdate';
import schemaListUpdateReducer from './setSchemaList';
import relationListUpdateReducer from './setRelationList';
import queryListUpdateReducer from './setQueryList';
import planUpdateReducer from './setPlan';
import graphicalPlanUpdateReducer from './setGraphicalPlan';

/**
 * Combines setBody and schemaUpdate reducers into a single rootReducer.
 * To access state from the components, use setBody and schemaUpdate.
 *
 * @author Camilo Ortiz
 */


const rootReducer = combineReducers({
  body: setBodyReducer,
  selectedSchema: schemaUpdateReducer,
  relationList: relationListUpdateReducer,
  schemaList: schemaListUpdateReducer,
  queryList: queryListUpdateReducer,
  plan: planUpdateReducer,
  graphicalPlan:  graphicalPlanUpdateReducer
})

export default rootReducer;
