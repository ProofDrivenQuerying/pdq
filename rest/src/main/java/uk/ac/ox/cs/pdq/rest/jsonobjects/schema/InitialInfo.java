package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;


public class InitialInfo{
  public SchemaName[] schemas;
  public Long userID;

  public InitialInfo(SchemaName[] schemas, Long userID){
    this.schemas = schemas;
    this.userID = userID;
  }
}
