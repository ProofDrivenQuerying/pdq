import { store } from '../redux/store.js';


export const fetchingGraphicalPlan = () => {
    return {
        type: 'FETCHING_GRAPHICALPLAN',
    };
}
export const resolvedGraphicalPlan = (graphicalPlan) => {
  return{
    type: 'RESOLVED_GRAPHICALPLAN',
    graphicalPlan,
  }
}
export const errorGraphicalPlan = () => {
    return {
        type: 'ERROR_GRAPHICALPLAN',
    };
}



export const getGraphicalPlan = (id) => {

  //fetching
  store.dispatch(fetchingGraphicalPlan());

  return function(dispatch, getState){
    return fetch("/getGraphicalPlan?id="+id)
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{
      console.log(res);
      //if its ok, we keep the data
      dispatch(resolvedGraphicalPlan(res))

    }).catch(err => dispatch(errorGraphicalPlan()));
  }
}
