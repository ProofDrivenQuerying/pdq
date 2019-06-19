package uk.ac.ox.cs.pdq.rest;

/*
  Defines the structure for each relation's access methods field for conversion into JSON.
 */

public class  JsonAccessMethod{
  public String name;
  public String type;
  public JsonAccessMethod(String name, String type){
    this.name = name;
    this.type = type;
  }
}
