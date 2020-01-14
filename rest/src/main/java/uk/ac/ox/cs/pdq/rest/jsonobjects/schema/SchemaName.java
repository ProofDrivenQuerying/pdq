package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

import uk.ac.ox.cs.pdq.db.Schema;

import java.util.ArrayList;

/**
 * Serializable basic schema information that the client will load before their components
 * mount. Includes unique identifier int `id`, and String `name`.
 *
 * @author Camilo Ortiz
 */
public class SchemaName{
  public int id;
  public String name;
  public ArrayList<JsonQuery> queries;

  public SchemaName(Schema schema, int id, ArrayList<JsonQuery> queries){
    this.name = "schema"+ Integer.toString(id);
    this.id = id;
    this.queries = queries;
  }
}
