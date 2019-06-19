package uk.ac.ox.cs.pdq.rest.jsonwrappers;

/**
 *  Defines the structure of the query in JSON form.
 *
 * @author Camilo Ortiz
 */
public class JsonQuery{
  public String name;
  public String SQL;
  public int id;

  public JsonQuery(int id, String SQL){
    this.name = "query"+Integer.toString(id);
    this.SQL = SQL;
    this.id = id;
  }
}
