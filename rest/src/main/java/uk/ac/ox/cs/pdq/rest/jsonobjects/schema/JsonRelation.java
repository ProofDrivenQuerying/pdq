package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;


/**
 * Defines the structure of Relations in JSON form.
 *
 * @author Camilo Ortiz
 */
public class JsonRelation{
  public String name;
  public JsonAttribute[] attributes;
  public JsonAccessMethod[] accessMethods;

  public JsonRelation(String name, JsonAttribute[] attributes, JsonAccessMethod[] accessMethods){
    this.name = name;
    this.attributes = attributes;
    this.accessMethods = accessMethods;
  }
}
