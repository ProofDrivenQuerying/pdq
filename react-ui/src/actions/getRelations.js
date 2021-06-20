import { store } from '../reducers/store.js';
import { api } from './config.js';

/**
 * Fetches an array of relation objects from the backend based on the provided
 * schema's id.
 *
 * @author Camilo Ortiz
 */

export const fetchingRelation = () => {
    return {
        type: 'FETCHING_RELATION',
    };
}
export const resolvedRelation = (relationList) => {
  return{
    type: "RESOLVED_RELATION",
    relationList,
  }
}
export const errorRelation = () => {
    return {
        type: 'ERROR_RELATION',
    };
}



export const getRelations = (id) => {

  store.dispatch(fetchingRelation());

  return function(dispatch, getState){
    return fetch(api + "/getRelations?id="+id)
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{

      dispatch(resolvedRelation(res))

    }).catch(err => dispatch(errorRelation()));
  }
}
