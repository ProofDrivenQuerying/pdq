package uk.ac.ox.cs.pdq.rest.jsonobjects.run;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;

public class JsonTuple {
    public Object[] values;

    public JsonTuple(Tuple t){
        this.values = t.getValues();
    }
}
