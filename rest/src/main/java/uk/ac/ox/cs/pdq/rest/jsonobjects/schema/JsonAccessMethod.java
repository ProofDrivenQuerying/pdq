package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 *  Defines the structure for each relation's access methods field for conversion into JSON.
 *
 * @author Camilo Ortiz
 */
public class  JsonAccessMethod{
  public String name;
  public String type;

  public JsonAccessMethod(String name, String type){
    this.name = name;
    this.type = type;
  }
}
