package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 * Serializable TGD class
 *
 * @author Camilo Ortiz
 */
public class JsonTGD {
    public String name;
    public String definition;

    public JsonTGD(String n, String d){
        this.name = n;
        this.definition = d;
    }
}
