/**
 * This reducer is in charge of updating the state's schemaList value and
 * is called before the application mounts.
 *
 * @author Camilo Ortiz
 */

const initialSchemaListState = {
  schemaList: [],
  isFetching: false,
  isError: false
}

const schemaListReducer = (state = initialSchemaListState, action) => {
  switch(action.type){
    case 'FETCHING':
      return{...state, schemaList: [], isFetching: true, isError: false};

    case 'RESOLVED':
      return{...state, schemaList: action.schemaList, isFetching: false, isError: false};

    case 'ERROR':
      return{...state, schemaList: [], isFetching: false, isError:true};

    default:
      return state;
  }
}

export default schemaListReducer;
