// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

/**
 * This reducer is in charge of setting the dependencyLists field of the state based on
 * the particular dispatched action. This information is used in the SchemaDropdown
 * component to visualize the dependency lists and their definitions.
 *
 * @author Camilo Ortiz
 */

 const initialDependencyListsState = {
   dependencyLists: null,
   isFetchingDependencies: false,
   isErrorDependencies: false
 }

 const dependencyListsReducer = (state = initialDependencyListsState, action) => {
   switch(action.type){
     case "FETCHING_DEPENDENCIES":
        return{
           ...state,
           dependencyLists: null,
           isFetchingDependencies: true,
           isErrorDependencies: false
        }
     case "RESOLVED_DEPENDENCIES":
          return{
            ...state,
            dependencyLists: action.dependencyLists,
            isFetchingDependencies: false,
            isErrorDependencies: false
          }
     case "ERROR_DEPENDENCIES":
          return{
            ...state,
            dependencyLists: null,
            isFetchingDependencies: false,
            isErrorDependencies: true
          }
      default:
        return{
          ...state
        }
   }
 }

 export default dependencyListsReducer;
