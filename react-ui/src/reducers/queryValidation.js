// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

/**
 * This reducer is in charge of setting the queryList field of the state based on
 * the particular dispatched action. This information is used in the QueryBody
 * component. queryList is composed of an string SQL value, an int id, and
 * a string name value.
 *
 * @author Camilo Ortiz
 */

const initialQueryValidation = {
  validQuery: false,
  isFetchingValidation: false,
  isErrorValidation: false,
}

const queryValidation = (state = initialQueryValidation, action) => {
  switch(action.type){
    case 'FETCHING_VALIDATION':
      return{...state, isFetchingValidation: true, isErrorValidation: false};

    case 'RESOLVED_VALIDATION':
      return{...state,
            validQuery: action.queryValidation,
            isFetchingValidation: false,
            isErrorValidation: false};

    case 'ERROR_VALIDATION':
      return{...state, isFetchingValidation: false, isErrorValidation:true};

    default:
      return state;
  }
}

export default queryValidation;
