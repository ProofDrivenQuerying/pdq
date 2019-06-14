/**
 * setBody reducer. This reducer is in charge of updating the body's content
 * depending on the state. Options are 'schema', 'query', and 'search'.
 *
 * @author Camilo Ortiz
 */

const initialBodyState = {
  bodyContent: null
}

const setBodyReducer = (state = initialBodyState, action) => {
  switch(action.type){
    case 'schema':
      return {
        bodyContent: 'schema'
      }
    case 'schema_info':
      return {
        bodyContent: 'schema_info'
      }
    case 'query':
      return {
        bodyContent: 'query'
      }
    case 'search':
    return {
      bodyContent: 'search'
    }
    default:
      return {state};
  }
}

export default setBodyReducer;
