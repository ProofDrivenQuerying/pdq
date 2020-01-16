package uk.ac.ox.cs.pdq.rest.jsonobjects.run;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Serializable class that contains run results.
 *
 * @author Camilo Ortiz
 */
public class RunResults {
    public long tupleCount;
    public Table table;
    public double runTime;
    @JsonIgnore
    public uk.ac.ox.cs.pdq.datasources.tuple.Table results;

    public RunResults(long tc, uk.ac.ox.cs.pdq.datasources.tuple.Table t, double time){
        this.tupleCount = tc;
        this.table = new Table(t);
        this.runTime = time;
        this.results = t;
    }
}
