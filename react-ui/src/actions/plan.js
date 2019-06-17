import { store } from '../redux/store.js';


export const fetchingPlan = () => {
    return {
        type: 'FETCHING_PLAN',
    };
}
export const resolvedPlan = (plan) => {
  return{
    type: "RESOLVED_PLAN",
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
    var data;
    return fetch("/plan?id="+id)
    .then(res => res.text())
    .then(res =>{
      //if its ok, we keep the data
      dispatch(resolvedPlan(res))
    }).catch(err => dispatch(errorPlan()));
  }
}
