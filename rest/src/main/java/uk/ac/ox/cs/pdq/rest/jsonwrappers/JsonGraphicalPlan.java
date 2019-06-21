package uk.ac.ox.cs.pdq.rest.jsonwrappers;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;

/**
 * JsonGraphicalPlan is the object that is transformed into JSON to send to front end.
 *
 * @author Camilo Ortiz
 */
public class JsonGraphicalPlan {
    public int id;
    public String termName;
    public String accessTerm;
    public String type;
    public JsonGraphicalPlan[] children;


    public JsonGraphicalPlan(RelationalTerm rt, int size, int id, NodeStatus status){
        if(rt != null) this.termName = rt.getClass().getSimpleName();


        if(rt != null){
            Set<AccessTerm> accessTerms = rt.getAccesses();

            List<AccessTerm> accessList = new ArrayList<AccessTerm>(accessTerms);

            this.accessTerm = accessList.get(accessList.size()-1).getRelation().getName();
        }


//        this.accessTerms = rt.getAccesses();

        this.id = id;

        if (size == 0){
            this.children = null;
        }else{
            this.children = new JsonGraphicalPlan[size];
        }

        if(children != null){
            this.type = "ONGOING";
        }else{
            switch(status){
                case SUCCESSFUL:
                    this.type = "SUCCESSFUL";
                    break;
                case TERMINAL:
                    this.type = "TERMINAL";
                default:
                    this.type = "ONGOING";
            }
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

