package uk.ac.ox.cs.pdq.rest.jsonobjects.run;

/**
 * Serializable Tuple object.
 *
 * @author Camilo Ortiz
 */
public class Tuple {
    public Object[] values;

    public Tuple(uk.ac.ox.cs.pdq.db.tuple.Tuple t){
        this.values = t.getValues();
    }
}
