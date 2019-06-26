package uk.ac.ox.cs.pdq.rest.jsonobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;

public class JsonRunResults {
    public long tupleCount;
    public JsonTable table;
    public double runTime;
    @JsonIgnore
    public Table results;

    public JsonRunResults(long tc, Table t, double time){
        this.tupleCount = tc;
        this.table = new JsonTable(t);
        this.runTime = time;
        this.results = t;
    }
}
