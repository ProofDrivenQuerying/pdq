package uk.ac.ox.cs.pdq.rest;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

public class JsonQueryList{
  public String name;
  public int id;
  public JsonQuery[] queries;

  public JsonQueryList(ConjunctiveQuery query, int id){

    this.queries = new JsonQuery[]{new JsonQuery(id)};

    this.id = id;
    this.name = "query"+Integer.toString(id);
  }
}
