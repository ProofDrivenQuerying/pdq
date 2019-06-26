package uk.ac.ox.cs.pdq.rest.jsonobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

public class JsonPlan {
    public JsonGraphicalPlan graphicalPlan;
    public String bestPlan;
    public boolean runnable;
    public double planTime;

    @JsonIgnore
    public RelationalTerm plan;

    public JsonPlan(JsonGraphicalPlan gp, String bp, boolean r, RelationalTerm plan, double time){
        this.graphicalPlan = gp;
        this.bestPlan = bp;
        this.runnable = r;
        this.plan = plan;
        this.planTime = time;
    }
}
