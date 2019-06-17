package uk.ac.ox.cs.pdq.rest;

public class JsonQuery{
  public String name;

  public JsonQuery(int id){
    this.name = "query"+Integer.toString(id);

  }
}
