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
    var data;
    return fetch("/getGraphicalPlan?id="+id)
    .then(res => res.text())
    .then(res => data = JSON.parse(res)).then((data)=>{
      console.log(data);
      //if its ok, we keep the data
      dispatch(resolvedGraphicalPlan(data))

    }).catch(err => dispatch(errorGraphicalPlan()));
  }
}
