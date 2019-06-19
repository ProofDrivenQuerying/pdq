package uk.ac.ox.cs.pdq.rest;

import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import java.io.File;
import uk.ac.ox.cs.pdq.db.Schema;
import java.util.Map.Entry;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.Cost;
import java.util.Map.Entry;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;

/*
	This class has the static methods for creating a plan, and populating a JsonGraphicalPlan.

	@author Camilo Ortiz
 */

public class JsonPlanner{

  /*
  	Creates an Entry<RelationalTerm, Cost> object from a schema, a conjunctive query, and its properties
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
    try{

      entry = planner.search(query);



    }catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return entry;
  }
  /*
  	Converter from `Entry<RelationalTerm, Cost> plan` to JsonGraphicalPlan that can be easily converted to JSON
   */
  public static JsonGraphicalPlan getGraphicalPlan(Entry<RelationalTerm, Cost> plan){
	  RelationalTerm entryTerm = plan.getKey();

	  JsonGraphicalPlan planToReturn = JsonPlanner.populateJsonGraphicalPlan(entryTerm);

	  return planToReturn;
  }
  /*
  	Gets label for populating JsonGraphicalPlan fields
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
	/*
		Recursively populates the children and name of a JsonGraphicalPlan, and it's children's, etc.
	 */
	public static JsonGraphicalPlan populateJsonGraphicalPlan(RelationalTerm rt){
		if(rt.getNumberOfChildren() == 0){
			return new JsonGraphicalPlan(rt, 0);
		}else{
			JsonGraphicalPlan plan = new JsonGraphicalPlan(rt, rt.getNumberOfChildren());

			for(int i = 0; i < rt.getNumberOfChildren(); i++){
				plan.setChild(i,populateJsonGraphicalPlan(rt.getChild(i)));
			}
			return plan;
		}
	}
}
