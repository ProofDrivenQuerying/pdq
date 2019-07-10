import { store } from '../reducers/store.js';

/**
 * Fetches a plan's run object from the backend based on the provided selected schema's id.
 *
 * @author Camilo Ortiz
 */


export const fetchingPlanRun = () => {
    return {
        type: 'FETCHING_PLAN_RUN',
    };
}
export const resolvedPlanRun = (planRun, schemaID, queryID) => {
  return{
    type: "RESOLVED_PLAN_RUN",
    schemaID,
    queryID,
    planRun
  }
}
export const errorPlanRun = () => {
    return {
        type: 'ERROR_PLAN_RUN',
    };
}



export const run = (schemaID, queryID, SQL) => {
  store.dispatch(fetchingPlanRun());

  return function(dispatch, getState){
    let simpleSQL = SQL.replace(/\n|\r|\t/g, " ");

    return fetch("/run/"+schemaID+"/"+queryID+"/"+simpleSQL)
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{
      dispatch(resolvedPlanRun(res, schemaID, queryID))
    }).catch(err => dispatch(errorPlanRun()));
  }
}
