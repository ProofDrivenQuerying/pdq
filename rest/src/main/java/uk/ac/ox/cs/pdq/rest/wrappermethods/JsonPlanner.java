package uk.ac.ox.cs.pdq.rest.wrappermethods;

import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.fol.*;
import java.io.File;
import java.util.List;
import uk.ac.ox.cs.pdq.db.Schema;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.planner.*;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.plantree.PlanTree;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.*;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.rest.jsonobjects.JsonGraphicalPlan;
import uk.ac.ox.cs.pdq.rest.jsonobjects.JsonPlan;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;


/**
 * This class has the static methods for creating a plan and populating a JsonGraphicalPlan.
 *
 * @author Camilo Ortiz
 */
public class JsonPlanner{

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
  public static JsonPlan plan(Schema schema, ConjunctiveQuery query, File properties, String pathToCatalog){
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

      JsonGraphicalPlan graphicalPlan = null;

      JsonPlan toReturn = null;
      try{
          long start = System.currentTimeMillis();

          Entry<RelationalTerm, Cost>  entry = planner.search(query); //plan first to set cache and get RelationalTerm

          double computationTime = (System.currentTimeMillis() - start)/1000.0; //plan time

          RelationalTerm plan = entry.getKey();

          PlanTree<SearchNode> tree = jsonPlanner.search(query); //plan again and get the tree
          SearchNode root = tree.getRoot();
          graphicalPlan = getPlanGraph(tree, root); //our graphical plan

          ExecutablePlan runnable = Runner.decoratePlan(plan, schema); //will be null if its not runnable

          //get pretty string
          final ByteArrayOutputStream baos = new ByteArrayOutputStream();

          try (PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
              PlanPrinter.printGenericPlanToStream(ps, plan, 1);
          }
          String data = new String(baos.toByteArray(), StandardCharsets.UTF_8);


          if(runnable == null){
              toReturn = new JsonPlan(graphicalPlan, data, false, plan, computationTime);
          }else{
              toReturn = new JsonPlan(graphicalPlan, data, true, plan, computationTime);
          }

      }catch (Throwable e) {
          e.printStackTrace();
          System.exit(-1);
      }
      return toReturn;
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
