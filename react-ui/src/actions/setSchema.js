/**
 * Set's the state's selected schema and id to be the given schema and id parameters.
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
