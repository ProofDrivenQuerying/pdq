package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 *  Serializable Attribute class.
 *
 * @author Camilo Ortiz
 */
public class JsonAttribute{
  public String name;
  public String type;

  public JsonAttribute(String name, String type){
    this.name = name;
    this.type = type;
  }
}
