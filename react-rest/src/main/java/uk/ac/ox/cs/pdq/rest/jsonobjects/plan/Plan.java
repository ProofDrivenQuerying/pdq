// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.io.json.JSONRelationalTerm;

/**
 * Serializable Plan class.
 *
 * @author Camilo Ortiz
 */
public class Plan {
    public GraphicalPlan graphicalPlan;
    public JSONRelationalTerm jsonPlan;
    public boolean runnable;
    public double planTime;

    @JsonIgnore
    private RelationalTerm plan;

    public Plan(GraphicalPlan gp, boolean r, RelationalTerm plan, double time) throws JsonProcessingException {
        this.graphicalPlan = gp;
        this.jsonPlan = JSONRelationalTerm.fromRelationalTerm(plan);
        this.runnable = r;
        this.plan = plan;
        this.planTime = time;
    }

    public RelationalTerm getPlan(){
        return this.plan;
    }

    public GraphicalPlan getGraphicalPlan(){
        return this.graphicalPlan;
    }
}
