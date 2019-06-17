/**
 * relationList reducer. This reducer is in charge of updating the relationList
 * states (selectedSchema and key).
 *
 * @author Camilo Ortiz
 */

const initialSchemaListState = {
  plan: [],
  isFetchingPlan: false,
  isErrorPlan: false
}

const planUpdateReducer = (state = initialSchemaListState, action) => {
  switch(action.type){
    case 'FETCHING_PLAN':
      return{...state, plan: [], isFetchingPlan: true, isErrorPlan: false};

    case 'RESOLVED_PLAN':
      return{...state, plan: action.plan, isFetchingPlan: false, isErrorPlan: false};

    case 'ERROR_PLAN':
      return{...state, plan: [], isFetchingPlan: false, isErrorPlan:true};

    default:
      return state;
  }
}

export default planUpdateReducer;
