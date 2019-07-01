/**
 * Set's the state's selected query and schemaid / queryid to be the given
 * query and id parameters.
 *
 * @author Camilo Ortiz
 */


export default function setQuery(query, queryID, schemaID){
 return{
   type:'SET_QUERY',
   selectedQuery: query,
   queryID,
   schemaID,
 }
}
