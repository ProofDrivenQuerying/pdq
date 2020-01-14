package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 *  Serializable AccessMethod class.
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
