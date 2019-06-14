/**
 * Action used by the reducers. These are what are called in the components
 * to update the state. This one handles setting the schema.
 *
 * @author Camilo Ortiz
 */


export default function setSchema(schema, id){
 return{
   type:'SET',
   schema: schema,
   id: id,
 }
}
