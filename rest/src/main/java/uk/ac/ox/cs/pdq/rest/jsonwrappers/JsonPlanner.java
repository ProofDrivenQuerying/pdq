package uk.ac.ox.cs.pdq.rest.jsonwrappers;

import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.fol.*;
import java.io.File;
import java.util.List;
import uk.ac.ox.cs.pdq.db.Schema;
import java.util.Map.Entry;
import uk.ac.ox.cs.pdq.planner.*;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearExplorer;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.plantree.PlanTree;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.*;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.Cost;



/**
 * This class has the static methods for creating a plan and populating a JsonGraphicalPlan.
 *
 * @author Camilo Ortiz
 */
public class JsonPlanner{
    public static JsonGraphicalPlan search(Schema schema, ConjunctiveQuery query, File properties){
        PlannerParameters planParams = properties != null ?
                new PlannerParameters(properties) :
                new PlannerParameters() ;
        CostParameters costParams = properties != null ?
                new CostParameters(properties) :
                new CostParameters() ;

        ReasoningParameters reasoningParams = properties != null ?
                new ReasoningParameters(properties) :
                new ReasoningParameters() ;

        DatabaseParameters dbParams = properties != null ?
                new DatabaseParameters(properties) :
                DatabaseParameters.Postgres ;


        ExplorationSetUp planner = new ExplorationSetUp(planParams, costParams, reasoningParams, dbParams, schema);

        ExplorationSetUpForJson planner2 = new ExplorationSetUpForJson(planParams, costParams, reasoningParams, dbParams, schema);

        JsonGraphicalPlan toReturn = null;
        try{

            Entry<RelationalTerm, Cost>  entry = planner.search(query); //plan first to set cache

            PlanTree<SearchNode> tree = planner2.search(query); //plan again and get the tree

            SearchNode root = tree.getRoot();

            toReturn = getPlanGraph(tree, root);


        }catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return toReturn;
    }

    /**
     * Recursively populates the children and name of a JsonGraphicalPlan, and it's children's, etc.
     * @param tree
     * @param root
     * @return
     */
    public static JsonGraphicalPlan getPlanGraph(PlanTree<SearchNode> tree, SearchNode root){
        if(tree.getChildren(root).isEmpty()){
            return new JsonGraphicalPlan(root.getBestPlanFromRoot(), 0, root.getId(), root.getStatus());
        }else{
            List<SearchNode> children = tree.getChildren(root);
            JsonGraphicalPlan plan = new JsonGraphicalPlan(root.getBestPlanFromRoot(), children.size(), root.getId(), root.getStatus());

            for(int i = 0; i < children.size(); i++){
                plan.setChild(i, getPlanGraph(tree, children.get(i)));
            }
            return plan;
        }
    }

    /**
     * Recursively populates the children and name of a JsonGraphicalPlan, and it's children's, etc.
     *
     * @param rt
     * @return Populated JsonGraphicalPlan
     */
//    public static JsonGraphicalPlan populateJsonGraphicalPlan(RelationalTerm rt){
//        if(rt.getNumberOfChildren() == 0){
//            return new JsonGraphicalPlan(rt, 0, 1);
//        }else{
//            JsonGraphicalPlan plan = new JsonGraphicalPlan(rt, rt.getNumberOfChildren(),1 );
//
//            for(int i = 0; i < rt.getNumberOfChildren(); i++){
//                plan.setChild(i,populateJsonGraphicalPlan(rt.getChild(i)));
//            }
//            return plan;
//        }
//    }

    /**
     * Creates an Entry<RelationalTerm, Cost> object from a schema, a conjunctive query, and its properties
     *
     * @param schema
     * @param query
     * @param properties
     * @return Entry<RelationalTerm, Cost> Plan
     */
  public static Entry<RelationalTerm, Cost> plan(Schema schema, ConjunctiveQuery query, File properties){
    PlannerParameters planParams = properties != null ?
      new PlannerParameters(properties) :
      new PlannerParameters() ;
    CostParameters costParams = properties != null ?
      new CostParameters(properties) :
      new CostParameters() ;

    ReasoningParameters reasoningParams = properties != null ?
      new ReasoningParameters(properties) :
      new ReasoningParameters() ;

    DatabaseParameters dbParams = properties != null ?
      new DatabaseParameters(properties) :
      DatabaseParameters.Postgres ;

    Entry<RelationalTerm, Cost> entry = null;

    ExplorationSetUp planner = new ExplorationSetUp(planParams, costParams, reasoningParams, dbParams, schema);

    ExplorationSetUpForJson planner2 = new ExplorationSetUpForJson(planParams, costParams, reasoningParams, dbParams, schema);
    try{

      entry = planner.search(query);

    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return entry;
  }

    /**
     * Converter from `Entry<RelationalTerm, Cost> plan` to JsonGraphicalPlan that can be easily converted to JSON
     *
     * @param plan
     * @return JsonGraphicalPlan for use in JsonController
     */
//  public static JsonGraphicalPlan getGraphicalPlan(Entry<RelationalTerm, Cost> plan){
//	  RelationalTerm entryTerm = plan.getKey();
//
//	  JsonGraphicalPlan planToReturn = JsonPlanner.populateJsonGraphicalPlan(entryTerm);
//
//	  return planToReturn;
//  }


    /**
     * Gets label for populating JsonGraphicalPlan fields
     * @param t
     * @return String
     */
  public static String getLabelFor(RelationalTerm t) {
  	String ret = t.getClass().getSimpleName();
//		String ret = "label=\"" + t.getClass().getSimpleName() + "\n";
////		ret += "In :" + Joiner.on(",\n").join(Arrays.asList(t.getInputAttributes())) + "\n";
////		ret += "Out:" + Joiner.on(",\n").join(Arrays.asList(t.getOutputAttributes())) + "\n";
//		if (t instanceof SelectionTerm) {
//			ret += "SelectionCondition:" + ((SelectionTerm) t).getSelectionCondition() + "\"\n";
//			ret += "shape=polygon,sides=4,distortion=-.3\n";
//		} else if (t instanceof AccessTerm) {
//			ret += "Relation:" + ((AccessTerm) t).getRelation() + "\n";
//			ret += "AccessMethod:" + ((AccessTerm) t).getAccessMethod() + "\n";
//			ret += "InputConstants:" + ((AccessTerm) t).getInputConstants() + "\"\n";
//		} else if (t instanceof RenameTerm) {
//			ret += /*"Renamings:" + Arrays.asList(((RenameTerm) t).getRenamings()) + */ "\"\n";
//			ret += "shape=polygon,sides=4\n";
//		} else if (t instanceof JoinTerm) {
//			ret += "Conditions:" + ((JoinTerm) t).getJoinConditions() + "\"\n";
//			ret += "shape=invtriangle\n";
//		} else if (t instanceof DependentJoinTerm) {
//			ret += "Conditions:" + ((DependentJoinTerm) t).getJoinConditions() + "\n";
//			ret += "LeftRight positions:" + ((DependentJoinTerm) t).getPositionsInLeftChildThatAreInputToRightChild()
//					+ "\n";
//			ret += "\",shape=polygon,sides=5\n";
//		} else {
//			ret += "\",shape=polygon,sides=7\n";
//		}
		return ret;
	}


}
