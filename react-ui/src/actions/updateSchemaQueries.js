/**
 * Adds query to the schema's queryList
 *
 */

export default function updateSchemaQueries(query, schemaID){
  return{
    type: 'UPDATE_SCHEMA',
    query: query,
    schemaID: schemaID
  };
}
