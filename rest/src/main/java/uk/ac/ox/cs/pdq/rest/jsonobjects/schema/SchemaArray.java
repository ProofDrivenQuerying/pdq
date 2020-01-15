package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;


/**
 * Serializable array of schemas.
 *
 * @author Camilo Ortiz
 */
public class InitialInfo{

  public SchemaName[] schemas;

  public InitialInfo(SchemaName[] schemas){
    this.schemas = schemas;
  }
}
