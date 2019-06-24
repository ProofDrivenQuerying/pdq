/**
 * relationList reducer. This reducer is in charge of updating the relationList
 * states (selectedSchema and key).
 *
 * @author Camilo Ortiz
 */

const initialPlanRunState = {
  planRun: null,
  isFetchingPlanRun: false,
  isErrorPlanRun: false,
  id: null
}

const planRunUpdateReducer = (state = initialPlanRunState, action) => {
  switch(action.type){
    case 'FETCHING_PLAN_RUN':
      return{...state, planRun: null, isFetchingPlanRun: true, isErrorPlanRun: false};

    case 'RESOLVED_PLAN_RUN':
      return{...state, planRun: action.planRun, isFetchingPlanRun: false, isErrorPlanRun: false,
              id: action.id};

    case 'ERROR_PLAN_RUN':
      return{...state, planRun: null, isFetchingPlanRun: false, isErrorPlanRun:true};

    default:
      return state;
  }
}

export default planRunUpdateReducer;
