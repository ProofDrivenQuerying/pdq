package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 * Serializable EGD class
 *
 * @author Camilo Ortiz
 */
public class EGD {
    public String name;
    public String definition;

    public EGD(String n, String d){
        this.name = n;
        this.definition = d;
    }
}
