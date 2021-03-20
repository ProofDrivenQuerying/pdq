// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.util;

import com.fasterxml.jackson.jaxrs.json.annotation.JSONP;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.fol.*;
import java.io.File;
import java.util.Arrays;
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
import uk.ac.ox.cs.pdq.rest.jsonobjects.plan.GraphicalPlan;
import uk.ac.ox.cs.pdq.rest.jsonobjects.plan.JSONPlan;
import uk.ac.ox.cs.pdq.rest.jsonobjects.plan.Plan;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * This class has the static util for creating a plan and populating a JsonGraphicalPlan.
 *
 * @author Camilo Ortiz
 */
public class JsonPlanner{

    /**
     * Recursively populates the children and name of a JsonGraphicalPlan to create a search tree.
     * @param tree
     * @param root
     * @return
     */
    public static GraphicalPlan getPlanGraph(PlanTree tree, SearchNode root){
        if(tree.getChildren(root).isEmpty()){
            return new GraphicalPlan(root.getBestPlanFromRoot(), 0, root.getId(), root.getStatus());
        }else{
            List<SearchNode> children = tree.getChildren(root);
            GraphicalPlan plan = new GraphicalPlan(root.getBestPlanFromRoot(), children.size(), root.getId(), root.getStatus());

            for(int i = 0; i < children.size(); i++){
                plan.setChild(i, getPlanGraph(tree, children.get(i)));
            }
            return plan;
        }
    }

    /**
     * Creates an Entry<PlanTree, Cost> object from a schema, a conjunctive query, and its properties
     *
     * @param schema
     * @param query
     * @param properties
     * @return Entry<PlanTree, Cost> Plan
     */
  public static Plan plan(Schema schema, ConjunctiveQuery query, File properties, String pathToCatalog){
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

      GraphicalPlan graphicalPlan = null;

      Plan toReturn = null;
      try{
          long start = System.currentTimeMillis();

          Entry<RelationalTerm, Cost>  entry = planner.search(query); //plan first to set cache and get PlanTree

          double computationTime = (System.currentTimeMillis() - start)/1000.0; //plan time

          RelationalTerm plan = entry.getKey();

          PlanTree tree = jsonPlanner.search(query); //plan again and get the tree
          SearchNode root = tree.getRoot();
          graphicalPlan = getPlanGraph(tree, root); //our graphical plan

          ExecutablePlan runnable = JsonRunner.decoratePlan(plan, schema); //will be null if its not runnable

          // JSON plan serialization
          JSONPlan jsonPlan = new JSONPlan(plan);

          if(runnable == null){
              toReturn = new Plan(graphicalPlan, false, plan, computationTime);
          }else{
              toReturn = new Plan(graphicalPlan, true, plan, computationTime);
          }

          System.out.println("Plan as a string is " + toReturn.getPlan().toString());
          System.out.println("Plan input attributes are " + Arrays.toString(toReturn.getPlan().getInputAttributes()));
          System.out.println("Plan output attributes are " + Arrays.toString(toReturn.getPlan().getOutputAttributes()));
          System.out.println("Plan children are " + Arrays.toString(toReturn.getPlan().getChildren()));
          System.out.println("Plan accesses are " + toReturn.getPlan().getAccesses());
          System.out.println("Plan to logic is " + toReturn.getPlan().toLogic());
          System.out.println("Plan has join is " + toReturn.getPlan().hasJoin());
          System.out.println("Plan is closed is " + toReturn.getPlan().isClosed());
          System.out.println("Plan is linear is " + toReturn.getPlan().isLinear());

          System.out.println("/n/n");

          System.out.println("JSONPlan: " + jsonPlan.toString());

      }catch (Throwable e) {
          e.printStackTrace();
      }
      return toReturn;
  }

    /**
     * @param schema
     * @param query
     * @param properties
     * @param pathToCatalog
     * @return
     */
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
        }
        return toReturn;
    }

}
