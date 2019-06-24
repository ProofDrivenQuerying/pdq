import { store } from '../redux/store.js';


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

  //fetching
  store.dispatch(fetchingRelation());

  return function(dispatch, getState){
    return fetch("/getRelations?id="+id)
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{
      //if its ok, we keep the data
      dispatch(resolvedRelation(res))
    }).catch(err => dispatch(errorRelation()));
  }
}
