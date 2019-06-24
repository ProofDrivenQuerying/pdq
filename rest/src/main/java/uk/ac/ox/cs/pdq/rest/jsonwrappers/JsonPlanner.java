package uk.ac.ox.cs.pdq.rest.jsonwrappers;

import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.fol.*;
import java.io.File;
import java.util.List;
import uk.ac.ox.cs.pdq.db.Schema;
import java.util.Map.Entry;
import uk.ac.ox.cs.pdq.planner.*;
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
    public static JsonGraphicalPlan search(Schema schema, ConjunctiveQuery query, File properties, String pathToCatalog){
        PlannerParameters planParams = properties != null ?
                new PlannerParameters(properties) :
                new PlannerParameters() ;

        CostParameters costParams = properties != null ?
                new CostParameters(properties) :
                new CostParameters() ;
        costParams.setCatalog(pathToCatalog);

        ReasoningParameters reasoningParams = properties != null ?
                new ReasoningParameters(properties) :
                new ReasoningParameters() ;

        DatabaseParameters dbParams = properties != null ?
                new DatabaseParameters(properties) :
                DatabaseParameters.Postgres ;


        ExplorationSetUp planner = new ExplorationSetUp(planParams, costParams, reasoningParams, dbParams, schema);

        ExplorationSetUpForJson jsonPlanner = new ExplorationSetUpForJson(planParams, costParams, reasoningParams, dbParams, schema);

        JsonGraphicalPlan toReturn = null;
        try{

            Entry<RelationalTerm, Cost>  entry = planner.search(query); //plan first to set cache

            PlanTree<SearchNode> tree = jsonPlanner.search(query); //plan again and get the tree

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
     * Creates an Entry<RelationalTerm, Cost> object from a schema, a conjunctive query, and its properties
     *
     * @param schema
     * @param query
     * @param properties
     * @return Entry<RelationalTerm, Cost> Plan
     */
  public static Entry<RelationalTerm, Cost> plan(Schema schema, ConjunctiveQuery query, File properties, String pathToCatalog){
    PlannerParameters planParams = properties != null ?
      new PlannerParameters(properties) :
      new PlannerParameters() ;

    CostParameters costParams = properties != null ?
      new CostParameters(properties) :
      new CostParameters() ;
    costParams.setCatalog(pathToCatalog);

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

    public static RelationalTerm planToObject(Schema schema, ConjunctiveQuery query, File properties, String pathToCatalog){
        PlannerParameters planParams = properties != null ?
                new PlannerParameters(properties) :
                new PlannerParameters() ;

        CostParameters costParams = properties != null ?
                new CostParameters(properties) :
                new CostParameters() ;
        costParams.setCatalog(pathToCatalog);

        ReasoningParameters reasoningParams = properties != null ?
                new ReasoningParameters(properties) :
                new ReasoningParameters() ;

        DatabaseParameters dbParams = properties != null ?
                new DatabaseParameters(properties) :
                DatabaseParameters.Postgres ;


        RelationalTerm toReturn = null;

        ExplorationSetUp planner = new ExplorationSetUp(planParams, costParams, reasoningParams, dbParams, schema);

        try{
            Entry<RelationalTerm, Cost> entry = null;

            entry = planner.search(query);

            toReturn = entry.getKey();

        }catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return toReturn;
    }

}
