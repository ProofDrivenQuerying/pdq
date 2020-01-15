package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 *  Serializable AccessMethod class.
 *
 * @author Camilo Ortiz
 */
public class AccessMethod {
  public String name;
  public String type;

  public AccessMethod(String name, String type){
    this.name = name;
    this.type = type;
  }
}
