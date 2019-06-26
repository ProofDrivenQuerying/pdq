/**
 * This reducer is in charge of setting the queryList field of the state based on
 * the particular dispatched action. This information is used in the QueryBody
 * component. queryList is composed of an string SQL value, an int id, and
 * a string name value.
 *
 * @author Camilo Ortiz
 */

const initialQueryListState = {
  queryList: "",
  isFetchingQueries: false,
  isErrorQueries: false
}

const queryListUpdateReducer = (state = initialQueryListState, action) => {
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
