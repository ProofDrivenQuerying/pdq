/**
 * relationList reducer. This reducer is in charge of updating the relationList
 * states (selectedSchema and key).
 *
 * @author Camilo Ortiz
 */

const initialSchemaListState = {
  relationList: [],
  isFetchingRelations: false,
  isErrorRelations: false
}

const relationListUpdateReducer = (state = initialSchemaListState, action) => {
  switch(action.type){
    case 'FETCHING_RELATION':
      return{...state, relationList: [], isFetchingRelations: true, isErrorRelations: false};

    case 'RESOLVED_RELATION':
      return{...state, relationList: action.relationList, isFetchingRelations: false, isErrorRelations: false};

    case 'ERROR_RELATION':
      return{...state, relationList: [], isFetchingRelations: false, isErrorRelations:true};

    default:
      return state;
  }
}

export default relationListUpdateReducer;
