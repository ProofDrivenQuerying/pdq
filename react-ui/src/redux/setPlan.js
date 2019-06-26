/**
 * This reducer is in charge of setting the plan field of the state based on
 * the particular dispatched action. This plan information contains the
 * graphical plan object, the best plan as a string, the plantime, and the boolean
 * `runnable`.
 *
 * @author Camilo Ortiz
 */

const initialPlanState = {
  plan: null,
  isFetchingPlan: false,
  isErrorPlan: false,
  id: null
}

const planUpdateReducer = (state = initialPlanState, action) => {
  switch(action.type){
    case 'FETCHING_PLAN':
      return{...state, plan: null, isFetchingPlan: true, isErrorPlan: false};

    case 'RESOLVED_PLAN':
      return{...state, plan: action.plan, isFetchingPlan: false, isErrorPlan: false,
              id: action.id};

    case 'ERROR_PLAN':
      return{...state, plan: null, isFetchingPlan: false, isErrorPlan:true};

    default:
      return state;
  }
}

export default planUpdateReducer;
