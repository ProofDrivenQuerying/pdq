package uk.ac.ox.cs.pdq.rest.jsonobjects;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import java.lang.reflect.Type;

public class JsonTuple {
    public Object[] values;

    public JsonTuple(Tuple t){
        this.values = t.getValues();
    }
}
