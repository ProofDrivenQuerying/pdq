import { store } from '../reducers/store.js';

/**
 * Fetches two arrays of dependency objects (EGD, TGD) from the backend based
 * on the provided schema's id.
 *
 * @author Camilo Ortiz
 */

 export const fetchingDependencies = () => {
     return {
         type: 'FETCHING_DEPENDENCIES',
     };
 }
 export const resolvedDependencies = (dependencyLists) => {
   return{
     type: "RESOLVED_DEPENDENCIES",
     dependencyLists,
   };
 }
 export const errorDependencies = () => {
   return{
     type: "ERROR_DEPENDENCIES"
   };
 }

export const getDependencies = (id) => {
  //fetching
  store.dispatch(fetchingDependencies());

  return function (dispatch, getState){
    return fetch("/getDependencies?id="+id)
      .then(res => res.text())
      .then(res => res = JSON.parse(res)).then((res)=>{
        console.log(res);
        dispatch(resolvedDependencies(res));
      }).catch(err => dispatch(errorDependencies));
  }
}
