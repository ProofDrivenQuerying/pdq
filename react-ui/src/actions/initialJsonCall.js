import { store } from '../redux/store.js';

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
    var data;
    return fetch("/init_schemas")
    .then(res => res.text())
    .then(res => data = JSON.parse(res)).then((data)=>{  
      //if its ok, we keep the data
      dispatch(resolved(data))
    }).catch(err => dispatch(error()));
  }
}
