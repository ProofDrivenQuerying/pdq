// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import { store } from '../reducers/store.js';

/**
 * Fetches a queryList from the backend based on the provided
 * schema's id.
 * @author Camilo Ortiz
 */


export const fetchingValidation = () => {
    return {
        type: 'FETCHING_VALIDATION',
    };
}
export const resolvedValidation = (queryValidation) => {
  return{
    type: 'RESOLVED_VALIDATION',
    queryValidation,
  }
}
export const errorValidation = () => {
    return {
        type: 'ERROR_VALIDATION',
    };
}

export const verifyQuery = (schemaID, queryID, SQL) => {

  store.dispatch(fetchingValidation());

  return function(dispatch, getState){
    let simpleSQL = SQL.replace(/\n|\r|\t/g, " ");

    return fetch("/verifyQuery/"+schemaID+"/"+queryID+"/"+simpleSQL)
    .then(res => res.text())
    .then(res => JSON.parse(res)).then((res)=>{

      dispatch(resolvedValidation(res))

    }).catch(err => dispatch(errorValidation()));
  }
}
