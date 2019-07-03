package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

import uk.ac.ox.cs.pdq.db.Schema;

import java.util.ArrayList;

/**
 * Basic schema information that the client will load before their components
 * mount. Includes unique identifier int `id`, and String `name` that is displayed.
 *
 * The id is used in JsonChemaController as a key to the `Schema schema` value
 * in the schemaList dictionary.
 *
 * @author Camilo Ortiz
 */
public class SchemaName{
  public int id;
  public String name;
  public ArrayList<JsonQuery> queries;

  public SchemaName(Schema schema, int id, ArrayList<JsonQuery> queries){
    this.name = "schema"+ new Integer(id).toString();
    this.id = id;
    this.queries = queries;
  }
}
