// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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
  schemaID: null,
  queryID: null
}

const runReducer = (state = initialPlanRunState, action) => {
  switch(action.type){
    case 'FETCHING_PLAN_RUN':
      return{
        ...state,
        planRun: null,
        isFetchingPlanRun: true,
        isErrorPlanRun: false,
        schemaID: action.schemaID,
        queryID: action.queryID
      };

    case 'RESOLVED_PLAN_RUN':
      return{
        ...state,
        planRun: action.planRun,
        isFetchingPlanRun: false,
        isErrorPlanRun: false,
        schemaID: action.schemaID,
        queryID: action.queryID
      };

    case 'ERROR_PLAN_RUN':
      return{
        ...state,
        planRun: null,
        isFetchingPlanRun: false,
        isErrorPlanRun:true,
        schemaID: action.schemaID,
        queryID: action.queryID
      };

    default:
      return state;
  }
}

export default runReducer;
