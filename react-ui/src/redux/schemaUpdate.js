/**
 * schemaUpdate reducer. This reducer is in charge of updating the schemaUpdate
 * states (selectedSchema and key).
 *
 * @author Camilo Ortiz
 */

const initialSchemaState = {
  selectedSchema: null,
  id: null
}

const schemaUpdateReducer = (state = initialSchemaState, action) => {
  switch(action.type){
    case 'SET':
      return { ...state, selectedSchema: action.schema, id: action.id };
    default:
      return state;
  }
}

export default schemaUpdateReducer;
