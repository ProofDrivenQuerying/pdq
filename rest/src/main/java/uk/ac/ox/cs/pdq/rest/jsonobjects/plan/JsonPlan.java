package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

/**
 * Serializable Plan class.
 *
 * @author Camilo Ortiz
 */
public class JsonPlan {
    public JsonGraphicalPlan graphicalPlan;
    public String bestPlan;
    public boolean runnable;
    public double planTime;

    @JsonIgnore
    private RelationalTerm plan;

    public JsonPlan(JsonGraphicalPlan gp, String bp, boolean r, RelationalTerm plan, double time){
        this.graphicalPlan = gp;
        this.bestPlan = bp;
        this.runnable = r;
        this.plan = plan;
        this.planTime = time;
    }

    public RelationalTerm getPlan(){
        return this.plan;
    }

    public JsonGraphicalPlan getGraphicalPlan(){
        return this.graphicalPlan;
    }
}
