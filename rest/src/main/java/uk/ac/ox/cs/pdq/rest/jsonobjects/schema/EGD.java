package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 * Serializable EGD class
 *
 * @author Camilo Ortiz
 */
public class JsonEGD {
    public String name;
    public String definition;

    public JsonEGD(String n, String d){
        this.name = n;
        this.definition = d;
    }
}
