package uk.ac.ox.cs.pdq.rest;

public class JsonQuery{
  public String name;
  public String SQL;

  public JsonQuery(int id, String SQL){
    this.name = "query"+Integer.toString(id);
    this.SQL = SQL;
  }
}
