/**
 * This reducer is in charge of updating the state's schemaList value and
 * is called before the application mounts. It also handles adding/removing
 * queries to the schema objects.
 *
 * @author Camilo Ortiz
 */

const initialSchemaListState = {
  schemas: [],
  selectedSID: 0,
  selectedQID: 0,
  isFetching: false,
  isError: false
}

function updateObjectInArray(array, action) {
  return array.map((item, index) => {
    if (index !== action.id) {
      return item
    }
    return {
      ...item,
      queries:[
        ...item.queries,
        action.query
      ]
    }
  })
}

function removeQuery(array, action) {
  return array.map((item, index) => {
    if(index !== action.schemaID) {
      return item
    }

    let newArray = array.slice()
    newArray.splice(action.queryID, 1)
    return{
      ...item,
      queries: newArray
    }
  })
}

const schemaListReducer = (state = initialSchemaListState, action) => {
  console.log(state);
  switch(action.type){
    case 'FETCHING':
      return{...state, schemas: [], isFetching: true, isError: false};

    case 'RESOLVED':
      return{...state, schemas: action.schemaList, isFetching: false, isError: false};

    case 'ERROR':
      return{...state, schemas: [], isFetching: false, isError:true};

    case 'SET_S_ID':
      return { ...state, selectedSID: action.id};

    case 'SET_Q_ID':
      return { ...state, selectedQID: action.id};

    case 'UPDATE_SCHEMALIST_QUERY':
      return {
        ...state,
        schemas: updateObjectInArray(state.schemas, action)
      };
    case 'REMOVE_SCHEMALISTQUERY':
    return{
      ...state,
      schemas: removeQuery(state.schemas, action)
    }

    default:
      return state;
  }
}

export default schemaListReducer;
