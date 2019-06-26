import { store } from '../redux/store.js';

/**
 * Fetches a queryList from the backend based on the provided
 * schema's id.
 * @author Camilo Ortiz
 */


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
    return fetch("/getQueries?id="+id)
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{
      //if its ok, we keep the data
      dispatch(resolvedQueries(res))
    }).catch(err => dispatch(errorQueries()));
  }
}
