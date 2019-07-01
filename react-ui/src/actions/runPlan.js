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
export const resolvedPlanRun = (planRun, id) => {
  return{
    type: "RESOLVED_PLAN_RUN",
    id,
    planRun,
  }
}
export const errorPlanRun = () => {
    return {
        type: 'ERROR_PLAN_RUN',
    };
}



export const runPlan = (id) => {
  //fetching
  store.dispatch(fetchingPlanRun());

  return function(dispatch, getState){
    return fetch("/runPlan?id="+id)
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{
      //if its ok, we keep the data
      dispatch(resolvedPlanRun(res, id))
    }).catch(err => dispatch(errorPlanRun()));
  }
}
