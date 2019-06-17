import { store } from '../redux/store.js';


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
    var data;
    return fetch("/plan?id="+id)
    .then(res => res.text())
    .then(res => data = JSON.parse(res)).then((data)=>{
      //if its ok, we keep the data
      dispatch(resolvedPlan(data, id))
    }).catch(err => dispatch(errorPlan()));
  }
}
