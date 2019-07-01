/**
 * This reducer is in charge of setting the planRun field of the state based on
 * the particular dispatched action. This information is used in the Plan's
 * run results modal.
 *
 * @author Camilo Ortiz
 */

const initialPlanRunState = {
  planRun: null,
  isFetchingPlanRun: false,
  isErrorPlanRun: false,
  id: null
}

const runReducer = (state = initialPlanRunState, action) => {
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

export default runReducer;
