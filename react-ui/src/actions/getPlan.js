import { store } from '../reducers/store.js';

/**
 * Fetches plan object from the backend based on the provided selected schema's id.
 *
 * @author Camilo Ortiz
 */


export const fetchingPlan = () => {
    return {
        type: 'FETCHING_PLAN',
    };
}
export const resolvedPlan = (plan, id) => {
  return{
    type: "RESOLVED_PLAN",
    id,
    plan,
  }
}
export const errorPlan = () => {
    return {
        type: 'ERROR_PLAN',
    };
}



export const plan = (id) => {

  //fetching
  store.dispatch(fetchingPlan());

  return function(dispatch, getState){
    return fetch("/plan?id="+id)
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{
      //if its ok, we keep the data
      dispatch(resolvedPlan(res, id))
    }).catch(err => dispatch(errorPlan()));
  }
}
