/**
 * queryList reducer. This reducer is in charge of updating the relationList
 * states (selectedSchema and key).
 *
 * @author Camilo Ortiz
 */

const initialSchemaListState = {
  queryList: "",
  isFetchingQueries: false,
  isErrorQueries: false
}

const queryListUpdateReducer = (state = initialSchemaListState, action) => {
  switch(action.type){
    case 'FETCHING_QUERIES':
      return{...state, queryList: [], isFetchingQueries: true, isErrorQueries: false};

    case 'RESOLVED_QUERIES':
      return{...state, queryList: action.queryList, isFetchingQueriesQueries: false, isErrorQueries: false};

    case 'ERROR_QUERIES':
      return{...state, queryList: [], isFetchingQueries: false, isErrorQueries:true};

    default:
      return state;
  }
}

export default queryListUpdateReducer;
