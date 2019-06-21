/**
 * graphicalPlan reducer. This reducer is in charge of updating the graphicalPlan
 * state.
 *
 * @author Camilo Ortiz
 */

const initialGraphicalPlanState = {
  graphicalPlan: null,
  isFetchingGraphicalPlan: false,
  isErrorGraphicalPlan: false
}

const graphicalPlanUpdateReducer = (state = initialGraphicalPlanState, action) => {
  switch(action.type){
    case 'FETCHING_GRAPHICALPLAN':
      return{...state, graphicalPlan: null, isFetchingGraphicalPlan: true, isErrorGraphicalPlan: false};

    case 'RESOLVED_GRAPHICALPLAN':
      return{...state, graphicalPlan: action.graphicalPlan, isFetchingGraphicalPlan: false, isErrorGraphicalPlan: false};

    case 'ERROR_GRAPHICALPLAN':
      return{...state, graphicalPlan: null, isFetchingGraphicalPlan: false, isErrorGraphicalPlan:true};

    default:
      return state;
  }
}

export default graphicalPlanUpdateReducer;
