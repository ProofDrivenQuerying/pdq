package uk.ac.ox.cs.pdq.rest.jsonwrappers;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

/**
 * JsonGraphicalPlan is the object that is transformed into JSON to send to front end.
 *
 * @author Camilo Ortiz
 */
public class JsonGraphicalPlan {
    public String name;
    public JsonGraphicalPlan[] children;

    public JsonGraphicalPlan(RelationalTerm rt, int size){
        this.name = JsonPlanner.getLabelFor(rt);
        if (size == 0){
            this.children = null;
        }else{
            this.children = new JsonGraphicalPlan[size];
        }
    }

    /**
     * Sets this.children[index] to as @param plan
     *
     * @param index
     * @param plan
     */
    public void setChild(int index, JsonGraphicalPlan plan){
        this.children[index] = plan;
    }



}

