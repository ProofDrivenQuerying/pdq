package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;


/**
 * Serializable array of schemas.
 *
 * @author Camilo Ortiz
 */
public class SchemaArray {

  public SchemaName[] schemas;

  public SchemaArray(SchemaName[] schemas){
    this.schemas = schemas;
  }
}
