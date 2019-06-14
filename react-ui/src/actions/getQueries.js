import { store } from '../redux/store.js';


export const fetchingQueries = () => {
    return {
        type: 'FETCHING_QUERIES',
    };
}
export const resolvedQueries = (queryList) => {
  return{
    type: "RESOLVED_QUERIES",
    queryList,
  }
}
export const errorQueries = () => {
    return {
        type: 'ERROR_QUERIES',
    };
}



export const getQueries = (id) => {

  //fetching
  store.dispatch(fetchingQueries());

  return function(dispatch, getState){
    var data;
    return fetch("/getQueries?id="+id)
    .then(res => res.text())
    .then(res => {
      console.log(res);
      //if its ok, we keep the data
      dispatch(resolvedQueries(res))
    }).catch(err => dispatch(errorQueries()));
  }
}
