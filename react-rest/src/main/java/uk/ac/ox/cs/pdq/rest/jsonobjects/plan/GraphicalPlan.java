package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;

/**
 * Serializable Graphical Plan object. Used to display the search tree by the front end.
 *
 * @author Camilo Ortiz
 */
public class GraphicalPlan {
    public int id;
    public String termName;
    public String accessTerm;
    public String type;
    public GraphicalPlan[] children;


    public GraphicalPlan(RelationalTerm rt, int size, int id, NodeStatus status){
        if(rt != null) this.termName = rt.getClass().getSimpleName();


        if(rt != null){
            Set<AccessTerm> accessTerms = rt.getAccesses();

            List<AccessTerm> accessList = new ArrayList<AccessTerm>(accessTerms);

            this.accessTerm = accessList.get(accessList.size()-1).getRelation().getName();
        }

        this.id = id;

        if (size == 0){
            this.children = null;
        }else{
            this.children = new GraphicalPlan[size];
        }

        if(children != null){
            this.type = "ONGOING";
        }else{
            switch(status){
                case SUCCESSFUL:
                    this.type = "SUCCESSFUL";
                    break;
                default:
                    this.type = "TERMINAL";
            }
        }


    }

    public void setChild(int index, GraphicalPlan plan){
        this.children[index] = plan;
    }

    public void setType(String t){
        this.type = t;
    }


}

