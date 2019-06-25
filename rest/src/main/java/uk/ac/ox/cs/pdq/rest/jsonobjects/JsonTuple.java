package uk.ac.ox.cs.pdq.rest.jsonobjects;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import java.lang.reflect.Type;

public class JsonTuple {
    public int size;
    public Object[] values;

    public JsonTuple(Tuple t){
        this.size = t.size();
        this.values = t.getValues();
    }
}
