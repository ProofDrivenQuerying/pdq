// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.run;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

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

    public RunResults(long tc, uk.ac.ox.cs.pdq.datasources.tuple.Table t, double time, ConjunctiveQuery cq){
        this.tupleCount = tc;
        this.table = new Table(t, cq);
        this.runTime = time;
        this.results = t;
    }
}
