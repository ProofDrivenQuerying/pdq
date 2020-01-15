package uk.ac.ox.cs.pdq.rest.jsonobjects.run;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;

/**
 * Serializable Tuple object.
 *
 * @author Camilo Ortiz
 */
public class JsonTuple {
    public Object[] values;

    public JsonTuple(Tuple t){
        this.values = t.getValues();
    }
}
