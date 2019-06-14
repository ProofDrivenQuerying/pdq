/**
 * Action used by the reducers. These are what are called in the components
 * to update the state. This one handles changing the body.
 *
 * @author Camilo Ortiz
 */

export default function setBody(body_type){
  return{
    type: body_type
  }
}
