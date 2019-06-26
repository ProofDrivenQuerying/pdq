import { store } from '../redux/store.js';

/**
 * Fetches a simple array of schemas (id, name)
 *
 * @author Camilo Ortiz
 */

export const fetching = () => {
    return {
        type: 'FETCHING',
    };
}
export const resolved = (schemaList) => {
    return {
        type: 'RESOLVED',
        schemaList,
    };
}
export const error = () => {
    return {
        type: 'ERROR',
    };
}

export const getInitialData = () => {

  //fetching
  store.dispatch(fetching());

  return function(dispatch, getState){
    return fetch("/init_schemas")
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{
      //if its ok, we keep the data
      dispatch(resolved(res))
    }).catch(err => dispatch(error()));
  }
}
