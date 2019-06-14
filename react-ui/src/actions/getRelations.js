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
    var data;
    return fetch("/getRelations?id="+id)
    .then(res => res.text())
    .then(res => data = JSON.parse(res)).then((data)=>{
      console.log(data);
      //if its ok, we keep the data
      dispatch(resolvedRelation(data))
    }).catch(err => dispatch(errorRelation()));
  }
}
